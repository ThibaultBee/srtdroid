package com.github.thibaultbee.srtdroid.models

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.thibaultbee.srtdroid.Srt
import com.github.thibaultbee.srtdroid.enums.*
import com.github.thibaultbee.srtdroid.models.rejectreason.InternalRejectReason
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.SocketException
import java.net.StandardProtocolFamily


/*
 * Theses tests are written to check if SRT API can be called from the Kotlin part.
 */

@RunWith(AndroidJUnit4::class)
class Socket6Test {
    private val srt = Srt()
    private lateinit var socket: Socket

    @Before
    fun setUp() {
        assert(srt.startUp() >= 0)
        socket = Socket(StandardProtocolFamily.INET6)
        assertTrue(socket.isValid)
    }

    @After
    fun tearDown() {
        if (socket.isValid)
            socket.close()
        assertEquals(srt.cleanUp(), 0)
    }

    @Test
    fun bindTest() {
        socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
        socket.bind("::1", 1111)
        assertTrue(socket.isBound)
    }

    @Test
    fun sockStatusTest() {
        assertEquals(SockStatus.INIT, socket.sockState)
        socket.bind("::1", 2222)
        assertEquals(SockStatus.OPENED, socket.sockState)
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
        socket.bind("::1", 3333)
        socket.listen(3)
    }

    @Test
    fun acceptTest() {
        try {
            socket.accept()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.ENOLISTEN.toString())
        }
    }

    @Test
    fun connectTest() {
        try {
            socket.connect("::1", 4444)
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.ENOSERVER.toString())
        }
        assertEquals(InternalRejectReason(RejectReasonCode.TIMEOUT), socket.rejectReason)
    }

    @Test
    fun rendezVousTest() {
        try {
            socket.rendezVous("::", "2001:0db8:0000:85a3:0000:0000:ac1f:8001", 5555)
            fail()
        } catch (e: SocketException) {
            assertEquals(e.message, ErrorType.ENOSERVER.toString())
        }
    }

    @Test
    fun getPeerNameTest() {
        assertNull(socket.peerName)
    }

    @Test
    fun getSockNameTest() {
        assertNull(socket.sockName)
        socket.bind("::1", 6666)
        assertEquals(socket.sockName!!.address.hostAddress, "::1")
        assertEquals(socket.sockName!!.port, 6666)
    }
}