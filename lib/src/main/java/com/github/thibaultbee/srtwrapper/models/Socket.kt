package com.github.thibaultbee.srtwrapper.models

import android.util.Log
import android.util.Pair
import com.github.thibaultbee.srtwrapper.enums.RejectReasonCode
import com.github.thibaultbee.srtwrapper.enums.SockOpt
import com.github.thibaultbee.srtwrapper.enums.SockStatus
import com.github.thibaultbee.srtwrapper.interfaces.SocketInterface
import com.github.thibaultbee.srtwrapper.models.rejectreason.InternalRejectReason
import com.github.thibaultbee.srtwrapper.models.rejectreason.PredefinedRejectReason
import com.github.thibaultbee.srtwrapper.models.rejectreason.RejectReason
import com.github.thibaultbee.srtwrapper.models.rejectreason.UserDefinedRejectReason
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

    external fun nativeGetSockState(): SockStatus
    val sockState: SockStatus
        get() = nativeGetSockState()

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
    external fun nativeGetConnectionTime(): Long
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
            setSockFlag(SockOpt.SNDBUF, value)
        }

    val isBound: Boolean
        get() = sockState == SockStatus.OPENED

    val isClose: Boolean
        get() = (sockState == SockStatus.CLOSED) || (sockState == SockStatus.NONEXIST)
    
    val isConnected: Boolean
        get() = sockState == SockStatus.CONNECTED
}