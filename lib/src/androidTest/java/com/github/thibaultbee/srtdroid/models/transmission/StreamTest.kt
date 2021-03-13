/*
 * Copyright (C) 2021 Thibault Beyou
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
package com.github.thibaultbee.srtdroid.models.transmission

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.thibaultbee.srtdroid.Srt
import com.github.thibaultbee.srtdroid.enums.SockOpt
import com.github.thibaultbee.srtdroid.enums.Transtype
import com.github.thibaultbee.srtdroid.models.Socket
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.net.InetAddress
import java.net.SocketException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.random.Random


/*
 * Theses tests are written to check if SRT API can be called from the Kotlin part.
 */

@RunWith(AndroidJUnit4::class)
class StreamTest {
    @Before
    fun setUp() {
        assert(Srt.startUp() >= 0)
    }

    @After
    fun tearDown() {
        assertEquals(Srt.cleanUp(), 0)
    }

    @Test
    fun inputStreamLiveTest() {
        val server = StreamMockServer(Transtype.LIVE)
        val socket = Socket()
        val arraySize = socket.getSockFlag(SockOpt.PAYLOADSIZE) as Int
        val serverByteArray = ByteArray(arraySize)
        Random.Default.nextBytes(serverByteArray)
        server.enqueue(serverByteArray, 0)
        socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.LIVE)
        socket.setSockFlag(SockOpt.RCVTIMEO, 1000)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)
        val inputStream = socket.getInputStream()
        val byteArray = ByteArray(arraySize)
        assertEquals(arraySize, inputStream.read(byteArray))
        assertArrayEquals(serverByteArray, byteArray)
        try {
            inputStream.read()
            fail()
        } catch (e: SocketException) {
        }
        socket.close()
        inputStream.close()
        server.shutdown()
    }

    @Test
    fun outputStreamLiveTest() {
        val server = StreamMockServer(Transtype.LIVE)
        val socket = Socket()
        val arraySize = socket.getSockFlag(SockOpt.PAYLOADSIZE) as Int
        server.enqueue(ByteArray(arraySize), arraySize)
        socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.LIVE)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)
        val outputStream = socket.getOutputStream()
        outputStream.write(ByteArray(arraySize))
        socket.close()
        outputStream.close()
        try {
            outputStream.write(ByteArray(arraySize))
            fail()
        } catch (expected: IOException) {
        }
        server.shutdown()
    }

    @Test
    fun inputStreamFileTest() {
        val server = StreamMockServer(Transtype.FILE)
        server.enqueue(byteArrayOf(5, 3), 0)
        val socket = Socket()
        socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
        socket.setSockFlag(SockOpt.RCVTIMEO, 1000)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)
        val inputStream = socket.getInputStream()
        assertEquals(5, inputStream.read())
        assertEquals(3, inputStream.read())
        try {
            inputStream.read()
            fail()
        } catch (e: SocketException) {
        }
        try {
            inputStream.read()
            fail()
        } catch (e: SocketException) {
        }
        socket.close()
        inputStream.close()

        try {
            inputStream.read()
            fail()
        } catch (e: SocketException) {
        }
        try {
            inputStream.read()
            fail()
        } catch (e: SocketException) {
        }
        server.shutdown()
    }

    @Test
    fun outputStreamFileTest() {
        val server = StreamMockServer(Transtype.FILE)
        server.enqueue(ByteArray(0), 3)
        val socket = Socket()
        socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)
        val outputStream = socket.getOutputStream()
        outputStream.write(5)
        outputStream.write(3)
        socket.close()
        outputStream.close()
        try {
            outputStream.write(9)
            fail()
        } catch (expected: IOException) {
        }
        server.shutdown()
    }

    internal class StreamMockServer(transtype: Transtype) {
        private val executor = Executors.newCachedThreadPool()
        private val serverSocket = Socket()
        private var socket: Socket? = null
        val port: Int

        init {
            serverSocket.reuseAddress = true
            serverSocket.setSockFlag(SockOpt.TRANSTYPE, transtype)
            serverSocket.bind(InetAddress.getLoopbackAddress(), 0)
            port = serverSocket.localPort
        }

        fun enqueue(sendBytes: ByteArray, receiveByteCount: Int): Future<ByteArray?> {
            return executor.submit(Callable<ByteArray?> {
                serverSocket.listen(1)
                val pair = serverSocket.accept()
                assertNotNull(pair.second)
                socket = pair.first
                val outputStream = socket?.getOutputStream()
                if (outputStream != null) {
                    assertEquals(sendBytes.size, outputStream.write(sendBytes))
                    val inputStream = socket?.getInputStream()
                    if (inputStream != null) {
                        val result = ByteArray(receiveByteCount)
                        var total = 0
                        while (total < receiveByteCount) {
                            total += inputStream.read(result, total, result.size - total)
                        }
                        result
                    }
                }
                ByteArray(0)

            })
        }

        fun shutdown() {
            socket?.close()
            serverSocket.close()
            executor.shutdown()
        }
    }

}