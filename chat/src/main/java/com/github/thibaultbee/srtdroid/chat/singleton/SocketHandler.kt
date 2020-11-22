package com.github.thibaultbee.srtdroid.chat.singleton

import com.github.thibaultbee.srtdroid.chat.interfaces.SocketManagerInterface
import com.github.thibaultbee.srtdroid.chat.utils.ErrorUtils
import com.github.thibaultbee.srtdroid.enums.SockOpt
import com.github.thibaultbee.srtdroid.models.Socket
import java.io.IOException

object SocketHandler {
    private var serverSocket: Socket? = null
    private lateinit var clientSocket: Socket
    private lateinit var recvThread: RecvThread

    var socketManagerInterface: SocketManagerInterface? = null

    fun createServer(ip: String, port: Int) {
        serverSocket = Socket()
        serverSocket?.let {
            if (!it.isValid) {
                throw Exception("Failed to create a Socket")
            }
            try {
                it.setSockFlag(SockOpt.RCVSYN, true)
                it.bind(ip, port)
                it.listen(1)

                val peer = it.accept()
                clientSocket = peer.first
                startRecvMessage()
            } catch (e: IOException) {
                it.close()
                throw e
            }
        }
    }

    fun createClient(ip: String, port: Int) {
        clientSocket = Socket()
        if (!clientSocket.isValid) {
            throw Exception("Failed to create a Socket")
        }
        try {
            clientSocket.setSockFlag(SockOpt.RCVSYN, true)
            clientSocket.connect(ip, port)
            startRecvMessage()
        } catch (e: IOException) {
            clientSocket.close()
            throw e
        }
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
                } catch (e: Exception) {
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