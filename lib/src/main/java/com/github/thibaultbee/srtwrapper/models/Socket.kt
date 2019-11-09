package com.github.thibaultbee.srtwrapper.models

import com.github.thibaultbee.srtwrapper.enums.SockOpt
import java.net.InetSocketAddress
import java.net.StandardProtocolFamily

class Socket {
    private external fun nativeSocket(af: StandardProtocolFamily, type: Int, protocol:  Int) : Int
    private external fun nativeCreateSocket() : Int
    private external fun nativeBind(u: Int, address: InetSocketAddress) : Int
    private external fun nativeClose() : Int

    private external fun nativeListen(u: Int, backlog: Int) : Int
    private external fun nativeConnect(u: Int, address: InetSocketAddress) : Int

    private external fun nativeSetSockOpt(u: Int, level: Int /*ignored*/, opt: SockOpt, value: Any) : Int

    private external fun nativeSendMsg2(u: Int, msg: String) : Int

    private var srtsocket: Int

    constructor() {
        srtsocket = nativeCreateSocket()
    }

    constructor(af: StandardProtocolFamily) {
        srtsocket = nativeSocket(af, 0, 0)
    }

    fun isValid() = srtsocket >= 0

    fun bind(address: InetSocketAddress) : Int {
        return nativeBind(srtsocket, address)
    }

    fun bind(address: String, port: Int) : Int {
        return nativeBind(srtsocket, InetSocketAddress(address, port))
    }

    fun close(): Int {
        return nativeClose()
    }

    // Connecting
    fun listen(backlog: Int) : Int {
        return nativeListen(srtsocket, backlog)
    }

    fun connect(address: InetSocketAddress) : Int {
        return nativeConnect(srtsocket, address)
    }

    fun connect(address: String, port: Int) : Int {
        return nativeConnect(srtsocket, InetSocketAddress(address, port))
    }

    // Options and properties
    fun setSockOpt(opt: SockOpt, value: Any) : Int {
        return nativeSetSockOpt(srtsocket, 0, opt, value)
    }

    // Transmission
    fun sendMsg2(msg: String): Int {
        return nativeSendMsg2(srtsocket, msg)
    }

}