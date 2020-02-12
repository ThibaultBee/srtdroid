package com.github.thibaultbee.srtwrapper.chat.interfaces

interface SocketManagerInterface {
    fun onRecvMsg(message: String)
    fun onConnectionClose(reason: String)
}