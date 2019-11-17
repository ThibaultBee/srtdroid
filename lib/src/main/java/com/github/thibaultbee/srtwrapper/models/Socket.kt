package com.github.thibaultbee.srtwrapper.models

import com.github.thibaultbee.srtwrapper.enums.SockOpt
import java.net.InetSocketAddress
import java.net.StandardProtocolFamily

class Socket {
    private external fun nativeSocket(af: StandardProtocolFamily, type: Int, protocol:  Int) : Int
    private external fun nativeCreateSocket() : Int
    private external fun nativeBind(address: InetSocketAddress) : Int
    private external fun nativeClose() : Int

    private external fun nativeListen(backlog: Int) : Int
    private external fun nativeConnect(address: InetSocketAddress) : Int

    private external fun nativeSetSockOpt(level: Int /*ignored*/, opt: SockOpt, value: Any) : Int

    private external fun nativeSendMsg2(msg: String) : Int

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

    fun isValid() = srtsocket > INVALID_SOCK

    fun bind(address: InetSocketAddress) : Int {
        return nativeBind(address)
    }

    fun bind(address: String, port: Int) : Int {
        return nativeBind(InetSocketAddress(address, port))
    }

    fun close(): Int {
        val res = nativeClose()
        srtsocket = INVALID_SOCK
        return res
    }

    // Connecting
    fun listen(backlog: Int) : Int {
        return nativeListen(backlog)
    }

    fun connect(address: InetSocketAddress) : Int {
        return nativeConnect(address)
    }

    fun connect(address: String, port: Int) : Int {
        return nativeConnect(InetSocketAddress(address, port))
    }

    // Options and properties
    fun setSockOpt(opt: SockOpt, value: Any) : Int {
        return nativeSetSockOpt(0, opt, value)
    }

    // Transmission
    fun sendMsg2(msg: String): Int {
        return nativeSendMsg2(msg)
    }

}