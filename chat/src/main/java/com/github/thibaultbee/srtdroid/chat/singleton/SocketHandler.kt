package com.github.thibaultbee.srtdroid.chat.singleton

import com.github.thibaultbee.srtdroid.chat.interfaces.SocketManagerInterface
import com.github.thibaultbee.srtdroid.chat.utils.ErrorUtils
import com.github.thibaultbee.srtdroid.enums.SockOpt
import com.github.thibaultbee.srtdroid.models.Socket

object SocketHandler {
    private var serverSocket: Socket? = null
    private lateinit var clientSocket: Socket
    private lateinit var recvThread: RecvThread

    var socketManagerInterface: SocketManagerInterface? = null

    fun createServer(ip: String, port: Int) {
        serverSocket = Socket()
        serverSocket?.let {
            if (!it.isValid()) {
                throw Exception("Failed to create a Socket")
            }
            if (it.setSockFlag(SockOpt.RCVSYN, true) != 0) {
                it.close()
                throw Exception(ErrorUtils.getMessage())
            }
            if (it.bind(ip, port) != 0) {
                it.close()
                throw Exception(ErrorUtils.getMessage())
            }
            if (it.listen(1) != 0) {
                it.close()
                throw Exception(ErrorUtils.getMessage())
            }

            val peer = it.accept()
            clientSocket = peer.first
            if (!clientSocket.isValid()) {
                it.close()
                throw Exception(ErrorUtils.getMessage())
            }
            startRecvMessage()
        }
    }

    fun createClient(ip: String, port: Int) {
        clientSocket = Socket()
        if (!clientSocket.isValid()) {
            throw Exception("Failed to create a Socket")
        }
        if (clientSocket.setSockFlag(SockOpt.RCVSYN, true) != 0) {
            clientSocket.close()
            throw Exception(ErrorUtils.getMessage())
        }
        if (clientSocket.connect(ip, port) != 0) {
            clientSocket.close()
            throw Exception(ErrorUtils.getMessage())
        }
        startRecvMessage()
    }

    fun sendMessage(message: String) {
        if (clientSocket.send(message) == -1) {
            socketManagerInterface?.onConnectionClose(
                ErrorUtils.getMessage()
            )
            close()
        }
    }

    fun recvMessage(): String {
        val pair = clientSocket.recv(2048)
        val res = pair.first
        val message = pair.second
        when {
            res > 0 -> return String(message)
            res == 0 -> {
                close()
                throw Exception("Connection has been closed")
            }
            else -> {
                close()
                throw Exception(ErrorUtils.getMessage())
            }
        }
    }

    private class RecvThread() : Thread() {
        var isRunning: Boolean = false
        override fun run() {
            isRunning = true
            while (isRunning) {
                try {
                    val message =
                        recvMessage()
                    socketManagerInterface?.onRecvMsg(message)
                } catch (e: java.lang.Exception) {
                    socketManagerInterface?.onConnectionClose(e.message ?: "")
                    close()
                    isRunning = false
                }
            }
        }
    }

    private fun startRecvMessage() {
        recvThread =
            RecvThread()
        recvThread.start()
    }

    private fun stopRecvMessage() {
        recvThread.isRunning = false
    }

    val peerName = clientSocket.peerName

    fun close() {
        stopRecvMessage()
        clientSocket.close()
        serverSocket?.close()
        serverSocket = null
    }
}