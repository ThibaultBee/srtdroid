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
package com.github.thibaultbee.srtdroid.models.transmission

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.thibaultbee.srtdroid.Srt
import com.github.thibaultbee.srtdroid.models.Socket
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.InetAddress
import java.net.SocketException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future


/*
 * Theses tests are written to check if SRT API can be called from the Kotlin part.
 */

@RunWith(AndroidJUnit4::class)
class SimpleStreamTest {
    private lateinit var socket: Socket

    @Before
    fun setUp() {
        socket = Socket()
        assertTrue(socket.isValid)
    }

    @After
    fun tearDown() {
        socket.close()
        assertEquals(Srt.cleanUp(), 0)
    }

    @Test
    fun inputStreamTest() {
        val inputStream = socket.getInputStream()
        assertEquals(0, inputStream.read(ByteArray(0)))
        try {
            inputStream.read()
            fail()
        } catch (e: SocketException) {
        }
        try {
            inputStream.read(ByteArray(10))
            fail()
        } catch (e: SocketException) {
        }
        val server = MockServer()
        server.enqueue()
        socket.connect(InetAddress.getLoopbackAddress(), server.port)
        socket.close()
        assertEquals(0, inputStream.read(ByteArray(0)))
        try {
            inputStream.read()
            fail()
        } catch (e: SocketException) {
        }
        try {
            inputStream.read(ByteArray(10))
            fail()
        } catch (e: SocketException) {
        }
    }

    @Test
    fun outputStreamTest() {
        val outputStream = socket.getOutputStream()
        outputStream.write(ByteArray(0))
        try {
            outputStream.write(255)
            outputStream.write(ByteArray(10))
            fail()
        } catch (expected: SocketException) {
        }
    }


    internal class MockServer {
        private val executor = Executors.newCachedThreadPool()
        private val serverSocket = Socket()
        val port: Int
        private var socket: Socket? = null

        init {
            serverSocket.reuseAddress = true
            serverSocket.bind(InetAddress.getLoopbackAddress(), 0)
            port = serverSocket.localPort
        }

        fun enqueue(): Future<Unit> {
            return executor.submit(Callable {
                serverSocket.listen(1)
                val pair = serverSocket.accept()
                assertNotNull(pair.second)
                socket = pair.first
            })
        }

        fun shutdown() {
            socket?.close()
            serverSocket.close()
            executor.shutdown()
        }
    }
}