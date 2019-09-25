package com.github.thibaultbee.srtwrapper.models

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.thibaultbee.srtwrapper.Srt
import com.github.thibaultbee.srtwrapper.enums.SockOpt
import com.github.thibaultbee.srtwrapper.enums.Transtype
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.net.StandardProtocolFamily


/*
 * Theses tests are written to check if SRT API can be called from the Kotlin part.
 */

@RunWith(AndroidJUnit4::class)
class SocketAndroidTest {
    private val srt = Srt()

    @Before
    fun setUp() {
        assertEquals(srt.startUp(), 0)
    }

    @After
    fun tearDown() {
        assertEquals(srt.cleanUp(), 0)
    }

    @Test
    fun defaultConstructorTest() {
        val socket = Socket()
        assertTrue(socket.isValid())
    }

    @Test
    fun inetConstructorTest() {
        val socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
    }

    @Test
    fun inet6ConstructorTest() {
        val socket = Socket(StandardProtocolFamily.INET6)
        assertTrue(socket.isValid())
    }

    @Test
    fun bindTest() {
        val socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertEquals(0, socket.setSockOpt(SockOpt.TRANSTYPE, Transtype.FILE))
        assertEquals(-1, socket.bind("127.0.3.1", 1234))
        assertEquals(Error.getLastErrorMessage(), "Connection setup failure: Operation not permitted.")
    }

    @Test
    fun closeTest() {
        val socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertEquals(0, socket.close())
    }

    @Test
    fun listenTest() {
        val socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertEquals(-1, socket.listen(3))
        assertEquals(Error.getLastErrorMessage(), "Operation not supported: Cannot do this operation on an UNBOUND socket.")
    }

    @Test
    fun connectTest() {
        val socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertEquals(-1, socket.connect("127.0.3.1", 1234))
        assertEquals(Error.getLastErrorMessage(), "Connection setup failure: Operation not permitted.")
    }

    @Test
    fun setSockOptTest() {
        val socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertEquals(0, socket.setSockOpt(SockOpt.TRANSTYPE, Transtype.FILE))
    }
}