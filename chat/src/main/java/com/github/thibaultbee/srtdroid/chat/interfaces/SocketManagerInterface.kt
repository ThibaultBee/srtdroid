package com.github.thibaultbee.srtdroid.chat.interfaces

interface SocketManagerInterface {
    fun onRecvMsg(message: String)
    fun onConnectionClose(reason: String)
}