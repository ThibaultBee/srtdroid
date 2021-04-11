/*
 * Copyright (C) 2021 Thibault B.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.thibaultbee.srtdroid.chat.singleton

import com.github.thibaultbee.srtdroid.chat.interfaces.SocketHandlerListener
import com.github.thibaultbee.srtdroid.chat.utils.ErrorUtils
import com.github.thibaultbee.srtdroid.enums.SockOpt
import com.github.thibaultbee.srtdroid.models.Socket
import java.io.IOException
import java.net.InetSocketAddress


object SocketHandler {
    private var serverSocket: Socket? = null
    private lateinit var clientSocket: Socket
    private lateinit var recvThread: RecvThread

    var socketHandlerListener: SocketHandlerListener? = null

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
            socketHandlerListener?.onConnectionClose(
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

    private class RecvThread : Thread() {
        var isRunning: Boolean = false
        override fun run() {
            isRunning = true
            while (isRunning) {
                try {
                    val message =
                        recvMessage()
                    socketHandlerListener?.onRecvMsg(message)
                } catch (e: Exception) {
                    socketHandlerListener?.onConnectionClose(e.message ?: "")
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

    val peerName: InetSocketAddress
        get() = clientSocket.peerName

    fun close() {
        stopRecvMessage()
        clientSocket.close()
        serverSocket?.close()
        serverSocket = null
    }
}