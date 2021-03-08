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
import androidx.test.platform.app.InstrumentationRegistry
import com.github.thibaultbee.srtdroid.Srt
import com.github.thibaultbee.srtdroid.enums.SockOpt
import com.github.thibaultbee.srtdroid.enums.Transtype
import com.github.thibaultbee.srtdroid.models.Socket
import com.github.thibaultbee.srtdroid.utils.Utils
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.net.InetAddress
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future


/*
 * Theses tests are written to check if SRT API can be called from the Kotlin part.
 */

@RunWith(AndroidJUnit4::class)
class FileTest {
    private val srt = Srt()
    private lateinit var socket: Socket

    @Before
    fun setUp() {
        assert(srt.startUp() >= 0)
        socket = Socket()
        socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
        assertTrue(socket.isValid)
    }

    @After
    fun tearDown() {
        socket.close()
        assertEquals(srt.cleanUp(), 0)
    }

    @Test
    fun recvFileTest() {
        val server = MockSendServer()
        val message = UUID.randomUUID().toString()
        val sendFile = Utils.createTestFile(message = message)

        server.enqueue(sendFile)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val recvFile = File(
            InstrumentationRegistry.getInstrumentation().context.externalCacheDir,
            UUID.randomUUID().toString()
        )
        assertEquals(sendFile.length(), socket.recvFile(recvFile, size = sendFile.length()))
    }

    internal class MockSendServer {
        private val executor = Executors.newCachedThreadPool()
        private val serverSocket = Socket()
        val port: Int
        private var socket: Socket? = null

        init {
            serverSocket.reuseAddress = true
            serverSocket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
            serverSocket.bind(InetAddress.getLoopbackAddress(), 0)
            port = serverSocket.localPort
        }

        fun enqueue(file: File): Future<Long?> {
            return executor.submit(Callable {
                serverSocket.listen(1)
                val pair = serverSocket.accept()
                assertNotNull(pair.second)
                socket = pair.first
                socket?.sendFile(file)
            })
        }

        fun shutdown() {
            socket?.close()
            serverSocket.close()
            executor.shutdown()
        }
    }
}