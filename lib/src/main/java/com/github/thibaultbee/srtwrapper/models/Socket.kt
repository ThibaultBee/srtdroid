package com.github.thibaultbee.srtwrapper.models

import android.util.Pair
import com.github.thibaultbee.srtwrapper.enums.SockOpt
import com.github.thibaultbee.srtwrapper.enums.SockStatus
import com.github.thibaultbee.srtwrapper.interfaces.SocketInterface
import java.io.File
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.StandardProtocolFamily

class Socket {
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

    external fun isValid(): Boolean

    external fun bind(address: InetSocketAddress): Int
    fun bind(address: String, port: Int) = bind(InetSocketAddress(address, port))
    fun bind(address: InetAddress, port: Int) = bind(InetSocketAddress(address, port))

    external fun getSockState(): SockStatus

    external fun close(): Int

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
    external fun listen(backlog: Int): Int

    external fun accept(): Pair<Socket, InetSocketAddress?>

    external fun connect(address: InetSocketAddress): Int
    fun connect(address: String, port: Int) = connect(InetSocketAddress(address, port))
    fun connect(address: InetAddress, port: Int) = connect(InetSocketAddress(address, port))

    external fun rendezVous(
        localAddress: InetSocketAddress,
        remoteAddress: InetSocketAddress
    ): Int

    fun rendezVous(localAddress: String, remoteAddress: String, port: Int) = rendezVous(
        InetSocketAddress(localAddress, port),
        InetSocketAddress(remoteAddress, port)
    )

    // Options and properties
    external fun getPeerName(): InetSocketAddress?

    external fun getSockName(): InetSocketAddress?

    external fun getSockFlag(opt: SockOpt): Any

    external fun setSockFlag(opt: SockOpt, value: Any): Int

    // Transmission
    external fun send(msg: ByteArray): Int
    fun send(msg: String) = send(msg.toByteArray())
    fun send(msg: ByteArray, offset: Int, size: Int): Int {
        val buffer = ByteArray(size - offset)
        msg.copyInto(buffer, 0, offset, offset + size)
        return send(buffer)
    }

    external fun sendMsg(msg: ByteArray, ttl: Int = -1, inOrder: Boolean = false): Int
    fun sendMsg(msg: String, ttl: Int = -1, inOrder: Boolean = false) =
        sendMsg(msg.toByteArray(), ttl, inOrder)

    external fun sendMsg2(msg: ByteArray, msgCtrl: MsgCtrl?): Int
    fun sendMsg2(msg: String, msgCtrl: MsgCtrl?) = sendMsg2(msg.toByteArray(), msgCtrl)

    external fun recv(size: Int): Pair<Int, ByteArray>

    external fun recvMsg2(size: Int, msgCtrl: MsgCtrl?): Pair<Int, ByteArray>

    external fun sendFile(path: String, offset: Long, size: Long, block: Int = 364000): Long
    fun sendFile(file: File, offset: Long, size: Long, block: Int = 364000) =
        sendFile(file.path, offset, size, block)
    fun sendFile(file: File, block: Int = 364000) =
        sendFile(file.path, 0, file.length(), block)

    external fun recvFile(path: String, offset: Long, size: Long, block: Int = 7280000): Long
    fun recvFile(file: File, offset: Long, size: Long, block: Int = 7280000) =
        recvFile(file.path, offset, size, block)

    // Reject reason
    external fun getRejectReason(): Int

    external fun setRejectReason(reason: Int): Int

    // Performance tracking
    external fun bstats(clear: Boolean): Stats

    external fun bistats(clear: Boolean, instantaneous: Boolean): Stats

    // Time access
    external fun connectionTime(): Long
}