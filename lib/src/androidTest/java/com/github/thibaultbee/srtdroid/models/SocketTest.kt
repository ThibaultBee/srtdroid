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
package com.github.thibaultbee.srtdroid.models

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.thibaultbee.srtdroid.Srt
import com.github.thibaultbee.srtdroid.enums.*
import com.github.thibaultbee.srtdroid.models.rejectreason.InternalRejectReason
import com.github.thibaultbee.srtdroid.models.rejectreason.PredefinedRejectReason
import com.github.thibaultbee.srtdroid.models.rejectreason.UserDefinedRejectReason
import com.github.thibaultbee.srtdroid.utils.Utils.Companion.createTestFile
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.net.SocketException


/*
 * Theses tests are written to check if SRT API can be called from the Kotlin part.
 */

@RunWith(AndroidJUnit4::class)
class SocketTest {
    private lateinit var socket: Socket

    @Before
    fun setUp() {
        assert(Srt.startUp() >= 0)
        socket = Socket()
        assertTrue(socket.isValid)
    }

    @After
    fun tearDown() {
        socket.close()
        assertEquals(Srt.cleanUp(), 0)
    }

    @Test
    fun bindTest() {
        socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
        socket.bind("127.0.3.1", 1234)
        assertTrue(socket.isBound)
    }

    @Test
    fun sockStatusTest() {
        assertEquals(SockStatus.INIT, socket.sockState)
        assertFalse(socket.isBound)
        socket.bind("127.0.3.1", 1235)
        assertEquals(SockStatus.OPENED, socket.sockState)
        assertTrue(socket.isBound)
    }

    @Test
    fun closeTest() {
        socket.close()
        assertTrue(socket.isClose)
    }

    @Test
    fun listenTest() {
        try {
            socket.listen(3)
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.EUNBOUNDSOCK.toString())
        }
        socket.bind("127.0.3.1", 1236)
        socket.listen(3)
    }

    @Test
    fun acceptTest() {
        try {
            socket.accept()
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.ENOLISTEN.toString())
        }
    }

    @Test
    fun connectTest() {
        try {
            socket.connect("127.0.3.1", 1237)
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.ENOSERVER.toString())
        }
        assertEquals(InternalRejectReason(RejectReasonCode.TIMEOUT), socket.rejectReason)
    }

    @Test
    fun rendezVousTest() {
        try {
            socket.rendezVous("0.0.0.0", "127.0.3.1", 1238)
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.ENOSERVER.toString())
        }
    }

    @Test
    fun getPeerNameTest() {
        try {
            assertNull(socket.sockName)
            fail()
        } catch (e: Exception) {
        }
    }

    @Test
    fun getSockNameTest() {
        try {
            assertNull(socket.sockName)
            fail()
        } catch (e: Exception) {
        }
        socket.bind("127.0.3.1", 1239)
        assertEquals("127.0.3.1", socket.sockName.address.hostAddress)
        assertEquals(1239, socket.sockName.port)
    }

    @Test
    fun getSockOptTest() {
        try {
            socket.getSockFlag(SockOpt.TRANSTYPE)  // Write only property
            fail()
        } catch (e: IOException) {
        }
        assertEquals(true, socket.getSockFlag(SockOpt.RCVSYN))
        assertEquals(-1, socket.getSockFlag(SockOpt.SNDTIMEO))
        assertEquals(-1L, socket.getSockFlag(SockOpt.MAXBW))
        assertEquals(KMState.KM_S_UNSECURED, socket.getSockFlag(SockOpt.RCVKMSTATE))
        assertEquals("", socket.getSockFlag(SockOpt.STREAMID))
    }

    @Test
    fun setSockOptTest() {
        socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
        socket.setSockFlag(SockOpt.RCVSYN, true)
        socket.setSockFlag(SockOpt.SNDTIMEO, 100)
        socket.setSockFlag(SockOpt.MAXBW, 100L)
        socket.setSockFlag(SockOpt.STREAMID, "Hello")
    }

    @Test
    fun sendMsg1Test() {
        try {
            socket.send("Hello World !")
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.ENOCONN.toString())
        }
    }

    @Test
    fun sendMsg2Test() {
        try {
            socket.send("Hello World !", -1, false)
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.ENOCONN.toString())
        }
    }

    @Test
    fun sendMsg3Test() {
        val msgCtrl = MsgCtrl(boundary = Boundary.SUBSEQUENT, pktSeq = 1, no = 1)
        try {
            socket.send("Hello World !", msgCtrl)
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.ENOCONN.toString())
        }
    }

    @Test
    fun recvTest() {
        try {
            socket.recv(
                4 /*Int nb bytes*/
            )
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.ENOCONN.toString())
        }
    }

    @Test
    fun recvMsg2Test() {
        try {
            socket.recv(
                4 /*Int nb bytes*/,
                MsgCtrl(flags = 0, boundary = Boundary.FIRST, pktSeq = 0, no = 10)
            )
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.ENOCONN.toString())
        }
    }

    @Test
    fun sendFileTest() {
        try {
            socket.sendFile(createTestFile())
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.ENOCONN.toString())
        }
    }

    @Test
    fun recvFileTest() {
        val myFile = File(
            InstrumentationRegistry.getInstrumentation().context.externalCacheDir,
            "FileToRecv"
        )
        try {
            socket.recvFile(myFile, 0, 1024)
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.ENOCONN.toString())
        }
    }

    @Test
    fun getRejectReasonTest() {
        assertEquals(InternalRejectReason(RejectReasonCode.UNKNOWN), socket.rejectReason)
    }

    @Test
    fun setRejectReasonTest() {
        socket.rejectReason = UserDefinedRejectReason(2)
        socket.rejectReason = PredefinedRejectReason(1)
    }

    @Test
    fun bstatsTest() {
        socket.bstats(true)
    }

    @Test
    fun bistatsTest() {
        socket.bistats(clear = true, instantaneous = false)
    }

    @Test
    fun connectionTimeTest() {
        assertEquals(0, socket.connectionTime)
    }

    @Test
    fun receiveBufferSizeTest() {
        assertNotEquals(0, socket.receiveBufferSize)
        socket.receiveBufferSize = 101568
        assertEquals(101568, socket.receiveBufferSize)
    }

    @Test
    fun reuseAddrTest() {
        socket.reuseAddress = true
        assertTrue(socket.reuseAddress)
        socket.reuseAddress = false
        assertFalse(socket.reuseAddress)
    }

    @Test
    fun sendBufferSizeTest() {
        assertNotEquals(0, socket.sendBufferSize)
        socket.sendBufferSize = 101568
        assertEquals(101568, socket.sendBufferSize)
    }
}