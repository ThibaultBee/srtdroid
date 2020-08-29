package com.github.thibaultbee.srtwrapper.impl

import com.github.thibaultbee.srtwrapper.enums.SockOpt
import com.github.thibaultbee.srtwrapper.enums.Transtype
import com.github.thibaultbee.srtwrapper.models.Socket
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.*


class SrtSocketImpl(af: StandardProtocolFamily) : SocketImpl() {
    private val TAG = this.javaClass.simpleName
    private val srtSocket: Socket = Socket(af)

    override fun listen(backlog: Int) {
        srtSocket.listen(backlog)
    }

    override fun create(streaming: Boolean) {
        srtSocket.setSockFlag(SockOpt.MESSAGEAPI, !streaming)
    }

    override fun getOption(option: Int): Any {
        when (option) {
            SocketOptions.SO_TIMEOUT -> {
                return srtSocket.getSockFlag(SockOpt.RCVTIMEO)
            }
            SocketOptions.SO_BINDADDR -> {
                val bindSocketAddr = srtSocket.getSockName()
                return bindSocketAddr?.address as Any
            }
            else -> {
                val sockOpt = SockOpt.fromSocketOption(option)
                if (sockOpt != SockOpt.UNKNOWN) {
                    return srtSocket.getSockFlag(sockOpt)
                } else {
                    throw UnsupportedOperationException("unsupported option")
                }
            }
        }
    }

    override fun setOption(option: Int, value: Any) {
        when (option) {
            SocketOptions.SO_TIMEOUT -> {
                srtSocket.setSockFlag(SockOpt.CONNTIMEO, value)
                srtSocket.setSockFlag(SockOpt.RCVTIMEO, value)
                srtSocket.setSockFlag(SockOpt.SNDTIMEO, value)
            }
            else -> {
                val sockOpt = SockOpt.fromSocketOption(option)
                if (sockOpt != SockOpt.UNKNOWN) {
                    srtSocket.setSockFlag(sockOpt, value)
                } else {
                    throw UnsupportedOperationException("unsupported option")
                }
            }
        }
    }

    override fun connect(aHost: String, aPort: Int) {
        if (srtSocket.connect(aHost, aPort) != 0) {
            throw IOException("Connection failed")
        }
    }

    override fun connect(anAddr: InetAddress, aPort: Int) {
        if (srtSocket.connect(anAddr, aPort) != 0) {
            throw IOException("Connection failed")
        }
    }

    override fun connect(remoteAddr: SocketAddress, timeout: Int) {
        val inetAddr: InetSocketAddress = remoteAddr as InetSocketAddress
        srtSocket.setSockFlag(SockOpt.CONNTIMEO, timeout)
        if (srtSocket.connect(inetAddr) != 0) {
            throw IOException("Connection failed")
        }
    }

    override fun bind(address: InetAddress, port: Int) {
        srtSocket.bind(address, port)
    }

    override fun accept(newImpl: SocketImpl) {
        srtSocket.accept()
    }

    override fun getOutputStream(): OutputStream {
        return SrtSocketOutputStream(this)
    }

    private class SrtSocketOutputStream(private val socketImpl: SrtSocketImpl) :
        OutputStream() {

        @Throws(IOException::class)
        override fun close() {
            socketImpl.close()
        }

        @Throws(IOException::class)
        override fun write(oneByte: Int) {
            val buffer = byteArrayOf(oneByte.toByte())
            socketImpl.write(buffer, 0, 1)
        }

        override fun write(
            buffer: ByteArray,
            offset: Int,
            byteCount: Int
        ) {
            socketImpl.write(buffer, offset, byteCount)
        }
    }

    @Throws(IOException::class)
    private fun write(buffer: ByteArray, defaultOffset: Int, defaultByteCount: Int) {
        var byteCount = defaultByteCount
        var offset = defaultOffset
        if (srtSocket.getSockFlag(SockOpt.TRANSTYPE) as Transtype == Transtype.LIVE) {
            val payloadSize = srtSocket.getSockFlag(SockOpt.PAYLOADSIZE) as Int
            while (byteCount > 0) {
                val bytesWritten: Int = srtSocket.send(buffer, offset, Math.min(byteCount, payloadSize))
                byteCount -= bytesWritten
                offset += bytesWritten
            }
        } else {
            srtSocket.send(buffer)
        }

    }

    override fun available(): Int {
        return srtSocket.getSockFlag(SockOpt.RCVDATA) as Int
    }

    override fun sendUrgentData(value: Int) {
        val buffer = byteArrayOf(value.toByte())
        write(buffer, 0, 1)
    }

    override fun getInputStream(): InputStream {
        return SrtSocketInputStream(this);
    }

    private class SrtSocketInputStream(private val socketImpl: SrtSocketImpl) :
        InputStream() {

        @Throws(IOException::class)
        override fun available(): Int {
            return socketImpl.available()
        }

        @Throws(IOException::class)
        override fun close() {
            socketImpl.close()
        }

        @Throws(IOException::class)
        override fun read(): Int {
            val buffer = ByteArray(1)
            return socketImpl.read(buffer, 0, 1)
        }

        @Throws(IOException::class)
        override fun read(buffer: ByteArray, byteOffset: Int, byteCount: Int): Int {
            return socketImpl.read(buffer, byteOffset, byteCount)
        }
    }

    @Throws(IOException::class)
    private fun read(buffer: ByteArray, offset: Int, byteCount: Int): Int {
        if (byteCount == 0) {
            return 0
        }

        val byteArray = srtSocket.recv(byteCount).second
        // Return of zero bytes for a blocking socket means a timeout occurred
        if (byteArray.isEmpty()) {
            throw SocketTimeoutException()
        }
        byteArray.copyInto(buffer, offset, 0, byteArray.size)

        return byteArray.size
    }

    override fun close() {
        srtSocket.close()
    }
}