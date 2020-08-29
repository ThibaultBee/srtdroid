package com.github.thibaultbee.srtwrapper.impl

import java.net.SocketImpl
import java.net.SocketImplFactory
import java.net.StandardProtocolFamily

class SrtSocketImplFactory: SocketImplFactory {
    override fun createSocketImpl(): SocketImpl {
        return SrtSocketImpl(StandardProtocolFamily.INET)
    }
}