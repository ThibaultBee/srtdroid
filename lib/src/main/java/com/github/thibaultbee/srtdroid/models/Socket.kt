/*
 * Copyright (C) 2021 Thibault Beyou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.thibaultbee.srtdroid.models

import android.util.Log
import android.util.Pair
import com.github.thibaultbee.srtdroid.enums.ErrorType
import com.github.thibaultbee.srtdroid.enums.RejectReasonCode
import com.github.thibaultbee.srtdroid.enums.SockOpt
import com.github.thibaultbee.srtdroid.enums.SockStatus
import com.github.thibaultbee.srtdroid.interfaces.SocketInterface
import com.github.thibaultbee.srtdroid.models.rejectreason.InternalRejectReason
import com.github.thibaultbee.srtdroid.models.rejectreason.PredefinedRejectReason
import com.github.thibaultbee.srtdroid.models.rejectreason.RejectReason
import com.github.thibaultbee.srtdroid.models.rejectreason.UserDefinedRejectReason
import java.io.*
import java.net.*

class Socket: Closeable {
    var socketInterface: SocketInterface? = null
    private var srtsocket: Int

    private external fun socket(af: StandardProtocolFamily, type: Int, protocol: Int): Int

    @Deprecated(message = "Use Socket() instead", replaceWith = ReplaceWith("Socket()"))
    constructor(af: StandardProtocolFamily) {
        srtsocket = socket(af, 0, 0)
    }

    private external fun createSocket(): Int

    constructor() {
        srtsocket = createSocket()
    }

    private constructor(socket: Int) {
        srtsocket = socket
    }

    private external fun nativeIsValid(): Boolean
    val isValid: Boolean
        get() = nativeIsValid()

    private external fun nativeBind(address: InetSocketAddress): Int
    fun bind(address: InetSocketAddress) {
        if (nativeBind(address) != 0) {
            throw BindException(Error.lastErrorMessage)
        }
    }

    fun bind(address: String, port: Int) = bind(InetSocketAddress(address, port))
    fun bind(address: InetAddress, port: Int) = bind(InetSocketAddress(address, port))

    private external fun nativeGetSockState(): SockStatus
    val sockState: SockStatus
        get() = nativeGetSockState()

    private external fun nativeClose(): Int
    override fun close() {
        if (nativeClose() != 0) {
            throw SocketException(Error.lastErrorMessage)
        }
    }

    // Connecting
    fun onListen(
        ns: Socket,
        hsVersion: Int,
        peerAddress: InetSocketAddress,
        streamId: String
    ): Int {
        return socketInterface?.onListen(ns, hsVersion, peerAddress, streamId)
            ?: 0 // By default, accept incoming connection
    }

    private external fun nativeListen(backlog: Int): Int
    fun listen(backlog: Int) {
        if (nativeListen(backlog) != 0) {
            throw SocketException(Error.lastErrorMessage)
        }
    }

    private external fun nativeAccept(): Pair<Socket, InetSocketAddress?>
    fun accept(): Pair<Socket, InetSocketAddress?> {
        val pair = nativeAccept()
        if (!pair.first.isValid) {
            throw SocketException(Error.lastErrorMessage)
        }
        return pair
    }

    private external fun nativeConnect(address: InetSocketAddress): Int
    fun connect(address: InetSocketAddress) {
        if (nativeConnect(address) != 0) {
            throw ConnectException(Error.lastErrorMessage)
        }
    }

    fun connect(address: String, port: Int) = connect(InetSocketAddress(address, port))
    fun connect(address: InetAddress, port: Int) = connect(InetSocketAddress(address, port))

    private external fun nativeRendezVous(
        localAddress: InetSocketAddress,
        remoteAddress: InetSocketAddress
    ): Int

    fun rendezVous(
        localAddress: InetSocketAddress,
        remoteAddress: InetSocketAddress
    ) {
        if (nativeRendezVous(localAddress, remoteAddress) != 0) {
            throw SocketException(Error.lastErrorMessage)
        }
    }

    fun rendezVous(localAddress: String, remoteAddress: String, port: Int) = rendezVous(
        InetSocketAddress(localAddress, port),
        InetSocketAddress(remoteAddress, port)
    )

    // Options and properties
    private external fun nativeGetPeerName(): InetSocketAddress?
    val peerName: InetSocketAddress?
        get() = nativeGetPeerName()
    val inetAddress: InetAddress?
        get() = peerName?.address
    val port: Int
        get() = peerName?.port ?: 0

    private external fun nativeGetSockName(): InetSocketAddress?
    val sockName: InetSocketAddress?
        get() = nativeGetSockName()
    val localAddress: InetAddress?
        get() = sockName?.address
    val localPort: Int
        get() = sockName?.port ?: 0

    private external fun nativeGetSockFlag(opt: SockOpt): Any?
    fun getSockFlag(opt: SockOpt): Any {
        return nativeGetSockFlag(opt) ?: throw IOException(Error.lastErrorMessage)
    }

    private external fun nativeSetSockFlag(opt: SockOpt, value: Any): Int
    fun setSockFlag(opt: SockOpt, value: Any) {
        if (nativeSetSockFlag(opt, value) != 0) {
            throw IOException(Error.lastErrorMessage)
        }
    }

    // Transmission
    // Send
    private external fun nativeSend(msg: ByteArray, offset: Int, size: Int): Int
    fun send(msg: ByteArray, offset: Int, size: Int): Int {
        val byteSent = nativeSend(msg, offset, size)
        when {
            byteSent < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }
            byteSent == 0 -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }
            else -> return byteSent
        }
    }

    fun send(msg: ByteArray) = send(msg, 0, msg.size)
    fun send(msg: String) = send(msg.toByteArray())

    private external fun nativeSend(
        msg: ByteArray,
        offset: Int,
        size: Int,
        ttl: Int = -1,
        inOrder: Boolean = false
    ): Int

    fun send(
        msg: ByteArray,
        offset: Int,
        size: Int,
        ttl: Int = -1,
        inOrder: Boolean = false
    ): Int {
        val byteSent = nativeSend(msg, offset, size, ttl, inOrder)
        when {
            byteSent < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }
            byteSent == 0 -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }
            else -> return byteSent
        }
    }

    fun send(msg: ByteArray, ttl: Int = -1, inOrder: Boolean = false) =
        send(msg, 0, msg.size, ttl, inOrder)

    fun send(msg: String, ttl: Int = -1, inOrder: Boolean = false) =
        send(msg.toByteArray(), ttl, inOrder)

    private external fun nativeSend(msg: ByteArray, offset: Int, size: Int, msgCtrl: MsgCtrl): Int
    fun send(msg: ByteArray, offset: Int, size: Int, msgCtrl: MsgCtrl): Int {
        val byteSent = nativeSend(msg, offset, size, msgCtrl)
        when {
            byteSent < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }
            byteSent == 0 -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }
            else -> return byteSent
        }
    }

    fun send(msg: ByteArray, msgCtrl: MsgCtrl) = send(msg, 0, msg.size, msgCtrl)
    fun send(msg: String, msgCtrl: MsgCtrl) = send(msg.toByteArray(), msgCtrl)

    fun getOutputStream(msgCtrl: MsgCtrl? = null) =
        SrtSocketOutputStream(this, msgCtrl) as OutputStream

    private class SrtSocketOutputStream(private val socket: Socket, private val msgCtrl: MsgCtrl?) :
        OutputStream() {

        override fun close() {
            socket.close()
        }

        override fun write(oneByte: Int) {
            val buffer = byteArrayOf(oneByte.toByte())
            write(buffer, 0, 1)
        }

        override fun write(
            buffer: ByteArray,
            defaultOffset: Int,
            defaultByteCount: Int
        ) {
            var byteCount = defaultByteCount
            var offset = defaultOffset
            if (socket.isClose) {
                throw SocketException(ErrorType.ESCLOSED.toString())
            }
            if (socket.getSockFlag(SockOpt.MESSAGEAPI) as Boolean) {
                // In case, message API is true, split buffer in payload size buffer.
                val payloadSize = socket.getSockFlag(SockOpt.PAYLOADSIZE) as Int
                while (byteCount > 0) {
                    val bytesWritten = if (msgCtrl != null) {
                        socket.send(buffer, offset, byteCount.coerceAtMost(payloadSize), msgCtrl)
                    } else {
                        socket.send(buffer, offset, byteCount.coerceAtMost(payloadSize))
                    }
                    byteCount -= bytesWritten
                    offset += bytesWritten
                }
            } else {
                if (msgCtrl != null) {
                    socket.send(buffer, offset, byteCount, msgCtrl)
                } else {
                    socket.send(buffer, offset, byteCount)
                }
            }
        }
    }

    // Recv
    private external fun nativeRecv(size: Int): Pair<Int, ByteArray>
    fun recv(size: Int): Pair<Int, ByteArray> {
        val pair = nativeRecv(size)
        val byteReceived = pair.first
        when {
            byteReceived < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }
            byteReceived == 0 -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }
            else -> return pair
        }
    }

    private external fun nativeRecv(
        buffer: ByteArray,
        offset: Int,
        byteCount: Int
    ): Pair<Int, ByteArray>

    fun recv(buffer: ByteArray, offset: Int, byteCount: Int): Pair<Int, ByteArray> {
        val pair = nativeRecv(buffer, offset, byteCount)
        val byteReceived = pair.first
        when {
            byteReceived < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }
            byteReceived == 0 -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }
            else -> return pair
        }
    }

    private external fun nativeRecv(size: Int, msgCtrl: MsgCtrl): Pair<Int, ByteArray>
    fun recv(size: Int, msgCtrl: MsgCtrl): Pair<Int, ByteArray> {
        val pair = nativeRecv(size, msgCtrl)
        val byteReceived = pair.first
        when {
            byteReceived < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }
            byteReceived == 0 -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }
            else -> return pair
        }
    }

    private external fun nativeRecv(
        buffer: ByteArray,
        offset: Int,
        byteCount: Int,
        msgCtrl: MsgCtrl?
    ): Pair<Int, ByteArray>

    fun recv(
        buffer: ByteArray,
        offset: Int,
        byteCount: Int,
        msgCtrl: MsgCtrl?
    ): Pair<Int, ByteArray> {
        val pair = nativeRecv(buffer, offset, byteCount, msgCtrl)
        val byteReceived = pair.first
        when {
            byteReceived < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }
            byteReceived == 0 -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }
            else -> return pair
        }
    }

    fun getInputStream(msgCtrl: MsgCtrl? = null) =
        SrtSocketInputStream(this, msgCtrl) as InputStream

    private class SrtSocketInputStream(private val socket: Socket, private val msgCtrl: MsgCtrl?) :
        InputStream() {

        override fun available(): Int {
            return socket.available()
        }

        override fun close() {
            socket.close()
        }

        override fun read(): Int {
            val pair = if (msgCtrl != null) {
                socket.recv(1, msgCtrl)
            } else {
                socket.recv(1)
            }
            val byteReceived = pair.first
            val byteArray = pair.second
            return if (byteReceived > 0) {
                byteArray[0].toInt()
            } else {
                -1
            }
        }

        override fun read(buffer: ByteArray, offset: Int, byteCount: Int): Int {
            if (byteCount == 0) {
                return 0
            }

            val pair = if (msgCtrl != null) {
                socket.recv(buffer, offset, byteCount, msgCtrl)
            } else {
                socket.recv(buffer, offset, byteCount)
            }

            return pair.first
        }
    }

    // File
    private external fun nativeSendFile(
        path: String,
        offset: Long,
        size: Long,
        block: Int = 364000
    ): Long

    fun sendFile(path: String, offset: Long = 0, size: Long, block: Int = 364000): Long {
        val byteSent = nativeSendFile(path, offset, size, block)
        when {
            byteSent < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }
            byteSent == 0L -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }
            else -> return byteSent
        }
    }

    fun sendFile(file: File, offset: Long = 0, size: Long, block: Int = 364000) =
        sendFile(file.path, offset, size, block)

    fun sendFile(file: File, block: Int = 364000) =
        sendFile(file.path, 0, file.length(), block)

    private external fun nativeRecvFile(
        path: String,
        offset: Long,
        size: Long,
        block: Int = 7280000
    ): Long

    fun recvFile(path: String, offset: Long = 0, size: Long, block: Int = 7280000): Long {
        val byteReceived = nativeRecvFile(path, offset, size, block)
        when {
            byteReceived < 0 -> {
                throw SocketException(Error.lastErrorMessage)
            }
            byteReceived == 0L -> {
                throw SocketTimeoutException(ErrorType.ESCLOSED.toString())
            }
            else -> return byteReceived
        }
    }

    fun recvFile(file: File, offset: Long = 0, size: Long, block: Int = 7280000) =
        recvFile(file.path, offset, size, block)

    // Reject reason
    private external fun nativeGetRejectReason(): Int
    private external fun nativeSetRejectReason(rejectReason: Int): Int
    var rejectReason: RejectReason
        get() {
            val code = nativeGetRejectReason()
            return when {
                code < RejectReasonCode.PREDEFINED_OFFSET -> InternalRejectReason(RejectReasonCode.values()[code])
                code < RejectReasonCode.USERDEFINED_OFFSET -> PredefinedRejectReason(code - RejectReasonCode.PREDEFINED_OFFSET)
                else -> UserDefinedRejectReason(code - RejectReasonCode.USERDEFINED_OFFSET)
            }
        }
        set(value) {
            val code = when (value) {
                is InternalRejectReason -> { // Forbidden by SRT
                    value.code.ordinal
                }
                is PredefinedRejectReason -> {
                    value.code + RejectReasonCode.PREDEFINED_OFFSET
                }
                is UserDefinedRejectReason -> {
                    value.code + RejectReasonCode.USERDEFINED_OFFSET
                }
                else -> RejectReasonCode.UNKNOWN.ordinal
            }
            if (nativeSetRejectReason(code) != 0) {
                Log.e(this.javaClass.canonicalName, "Failed to set reject reason")
            }
        }

    // Performance tracking
    external fun bstats(clear: Boolean): Stats

    external fun bistats(clear: Boolean, instantaneous: Boolean): Stats

    // Time access
    private external fun nativeGetConnectionTime(): Long
    val connectionTime: Long
        get() = nativeGetConnectionTime()

    // Android Socket like API
    var receiveBufferSize: Int
        get() = getSockFlag(SockOpt.RCVBUF) as Int
        set(value) {
            setSockFlag(SockOpt.RCVBUF, value)
        }

    var reuseAddress: Boolean
        get() = getSockFlag(SockOpt.REUSEADDR) as Boolean
        set(value) {
            setSockFlag(SockOpt.REUSEADDR, value)
        }

    var sendBufferSize: Int
        get() = getSockFlag(SockOpt.SNDBUF) as Int
        set(value) {
            setSockFlag(SockOpt.SNDBUF, value)
        }

    var soLinger: Int
        get() = getSockFlag(SockOpt.LINGER) as Int
        set(value) {
            setSockFlag(SockOpt.LINGER, value)
        }

    val isBound: Boolean
        get() = sockState == SockStatus.OPENED

    val isClose: Boolean
        get() = (sockState == SockStatus.CLOSED) || (sockState == SockStatus.NONEXIST)

    val isConnected: Boolean
        get() = sockState == SockStatus.CONNECTED

    fun available(): Int = getSockFlag(SockOpt.RCVDATA) as Int
}