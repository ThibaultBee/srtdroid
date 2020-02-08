package com.github.thibaultbee.srtwrapper.models

import android.util.Pair
import com.github.thibaultbee.srtwrapper.enums.SockOpt
import com.github.thibaultbee.srtwrapper.enums.SockStatus
import java.io.File
import java.net.InetSocketAddress
import java.net.StandardProtocolFamily

class Socket {
    private external fun nativeSocket(af: StandardProtocolFamily, type: Int, protocol: Int): Int
    private external fun nativeCreateSocket(): Int
    private external fun nativeBind(address: InetSocketAddress): Int
    private external fun nativeGetSockState(): SockStatus
    private external fun nativeClose(): Int

    private external fun nativeListen(backlog: Int): Int
    private external fun nativeAccept(): Pair<Socket, InetSocketAddress?>
    private external fun nativeConnect(address: InetSocketAddress): Int
    private external fun nativeRendezVous(
        localAddress: InetSocketAddress,
        remoteAddress: InetSocketAddress
    ): Int

    private external fun nativeGetPeerName(): InetSocketAddress?
    private external fun nativeGetSockName(): InetSocketAddress?
    private external fun nativeGetSockOpt(level: Int /*ignored*/, opt: SockOpt): Any
    private external fun nativeSetSockOpt(level: Int /*ignored*/, opt: SockOpt, value: Any): Int

    private external fun nativeSend(msg: ByteArray): Int
    private external fun nativeSendMsg(msg: ByteArray, ttl: Int, inOrder: Boolean): Int
    private external fun nativeSendMsg2(msg: ByteArray, msgCtrl: MsgCtrl?): Int
    private external fun nativeSendFile(path: String, offset: Long, size: Long, block: Int): Long

    private external fun nativeRecv(size: Int): ByteArray
    private external fun nativeRecvMsg2(size: Int, msgCtrl: MsgCtrl?): ByteArray
    private external fun nativeRecvFile(path: String, offset: Long, size: Long, block: Int): Long

    private var srtsocket: Int

    companion object {
        const val INVALID_SOCK = -1
    }

    constructor() {
        srtsocket = nativeCreateSocket()
    }

    constructor(af: StandardProtocolFamily) {
        srtsocket = nativeSocket(af, 0, 0)
    }

    private constructor(socket: Int) {
        srtsocket = socket
    }

    fun isValid() = srtsocket > INVALID_SOCK

    fun bind(address: InetSocketAddress) = nativeBind(address)

    fun bind(address: String, port: Int) = nativeBind(InetSocketAddress(address, port))

    fun getSockState() = nativeGetSockState()

    fun close(): Int {
        val res = nativeClose()
        srtsocket = INVALID_SOCK
        return res
    }

    // Connecting
    fun listen(backlog: Int) = nativeListen(backlog)

    fun accept() = nativeAccept()

    fun connect(address: InetSocketAddress) = nativeConnect(address)

    fun connect(address: String, port: Int) = nativeConnect(InetSocketAddress(address, port))

    fun rendezVous(localAddress: InetSocketAddress, remoteAddress: InetSocketAddress) =
        nativeRendezVous(localAddress, remoteAddress)

    fun rendezVous(localAddress: String, remoteAddress: String, port: Int) = nativeRendezVous(
        InetSocketAddress(localAddress, port),
        InetSocketAddress(remoteAddress, port)
    )

    // Options and properties
    fun getPeerName() = nativeGetPeerName()

    fun getSockName() = nativeGetSockName()

    fun getSockFlag(opt: SockOpt) = nativeGetSockOpt(0, opt)

    fun setSockFlag(opt: SockOpt, value: Any) = nativeSetSockOpt(0, opt, value)

    // Transmission
    fun send(msg: ByteArray) = nativeSend(msg)

    fun send(msg: String) = nativeSend(msg.toByteArray())

    fun sendMsg(msg: ByteArray, ttl: Int = -1, inOrder: Boolean = false) =
        nativeSendMsg(msg, ttl, inOrder)

    fun sendMsg(msg: String, ttl: Int = -1, inOrder: Boolean = false) =
        nativeSendMsg(msg.toByteArray(), ttl, inOrder)

    fun sendMsg2(msg: ByteArray, msgCtrl: MsgCtrl?) = nativeSendMsg2(msg, msgCtrl)

    fun sendMsg2(msg: String, msgCtrl: MsgCtrl?) = nativeSendMsg2(msg.toByteArray(), msgCtrl)

    fun recv(size: Int) = nativeRecv(size)

    fun recvMsg2(size: Int, msgCtrl: MsgCtrl?) = nativeRecvMsg2(size, msgCtrl)

    fun sendFile(file: File, offset: Long, size: Long, block: Int = 364000) =
        nativeSendFile(file.path, offset, size, block)

    fun sendFile(file: File, block: Int = 364000) =
        nativeSendFile(file.path, 0, file.totalSpace, block)

    fun recvFile(file: File, offset: Long, size: Long, block: Int = 7280000) =
        nativeRecvFile(file.path, offset, size, block)
}