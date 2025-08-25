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
package io.github.thibaultbee.srtdroid.core.models

import androidx.test.platform.app.InstrumentationRegistry
import io.github.thibaultbee.srtdroid.core.Srt
import io.github.thibaultbee.srtdroid.core.enums.Boundary
import io.github.thibaultbee.srtdroid.core.enums.ErrorType
import io.github.thibaultbee.srtdroid.core.enums.KMState
import io.github.thibaultbee.srtdroid.core.enums.RejectReasonCode
import io.github.thibaultbee.srtdroid.core.enums.SockOpt
import io.github.thibaultbee.srtdroid.core.enums.SockStatus
import io.github.thibaultbee.srtdroid.core.enums.Transtype
import io.github.thibaultbee.srtdroid.core.extensions.connect
import io.github.thibaultbee.srtdroid.core.models.rejectreason.InternalRejectReason
import io.github.thibaultbee.srtdroid.core.models.rejectreason.PredefinedRejectReason
import io.github.thibaultbee.srtdroid.core.utils.Utils.createTestFile
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.net.SocketException
import java.nio.ByteBuffer


/*
 * Theses tests are written to check if SRT API can be called from the Kotlin part.
 */

class SrtSocketTest {
    private lateinit var socket: SrtSocket

    @Before
    fun setUp() {
        socket = SrtSocket()
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
    fun connectUriTest() {
        try {
            socket.connect(SrtUrl("srt://192.168.1.12:9998"))
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
        } catch (_: Exception) {
        }
    }

    @Test
    fun getSockNameTest() {
        try {
            assertNull(socket.sockName)
            fail()
        } catch (_: Exception) {
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
        } catch (_: IOException) {
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
    fun sendByteBufferTest() {
        try {
            socket.send(ByteBuffer.allocateDirect(10))
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.ENOCONN.toString())
        }
    }

    @Test
    fun sendByteArrayTest() {
        try {
            socket.send("Hello World !")
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.ENOCONN.toString())
        }
    }

    @Test
    fun sendByteBuffer2Test() {
        try {
            socket.send(ByteBuffer.allocateDirect(10), -1, false)
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.ENOCONN.toString())
        }
    }

    @Test
    fun sendByteArray2Test() {
        try {
            socket.send("Hello World !", -1, false)
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.ENOCONN.toString())
        }
    }

    @Test
    fun sendByteBuffer3Test() {
        val msgCtrl = MsgCtrl(
            boundary = Boundary.SUBSEQUENT,
            pktSeq = 1,
            no = 1
        )
        try {
            socket.send(ByteBuffer.allocateDirect(10), msgCtrl)
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.ENOCONN.toString())
        }
    }

    @Test
    fun sendByteArray3Test() {
        val msgCtrl = MsgCtrl(
            boundary = Boundary.SUBSEQUENT,
            pktSeq = 1,
            no = 1
        )
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
                MsgCtrl(
                    flags = 0,
                    boundary = Boundary.FIRST,
                    pktSeq = 0,
                    no = 10
                )
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
        socket.rejectReason =
            io.github.thibaultbee.srtdroid.core.models.rejectreason.UserDefinedRejectReason(2)
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