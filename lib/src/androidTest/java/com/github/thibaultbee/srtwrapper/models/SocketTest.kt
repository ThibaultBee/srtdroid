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
class SocketTest {
    private val srt = Srt()
    private lateinit var socket: Socket

    @Before
    fun setUp() {
        assertEquals(srt.startUp(), 0)
    }

    @After
    fun tearDown() {
        if (socket.isValid())
            socket.close()
        assertEquals(srt.cleanUp(), 0)
    }

    @Test
    fun defaultConstructorTest() {
        socket = Socket()
        assertTrue(socket.isValid())
    }

    @Test
    fun inetConstructorTest() {
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
    }

    @Test
    fun inet6ConstructorTest() {
        socket = Socket(StandardProtocolFamily.INET6)
        assertTrue(socket.isValid())
    }

    @Test
    fun bindTest() {
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertEquals(0, socket.setSockOpt(SockOpt.TRANSTYPE, Transtype.FILE))
        assertEquals(0, socket.bind("127.0.3.1", 1234))
    }

    @Test
    fun closeTest() {
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertEquals(0, socket.close())
        assertFalse(socket.isValid())
    }

    @Test
    fun listenTest() {
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertEquals(-1, socket.listen(3))
        assertEquals(Error.getLastErrorMessage(), "Operation not supported: Cannot do this operation on an UNBOUND socket.")
        assertEquals(0, socket.bind("127.0.3.1", 1234))
        assertEquals(0, socket.listen(3))
    }

    @Test
    fun connectTest() {
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertEquals(-1, socket.connect("127.0.3.1", 1234))
        assertEquals(Error.getLastErrorMessage(), "Connection setup failure: connection time out.")
    }

    @Test
    fun setSockOptTest() {
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertEquals(0, socket.setSockOpt(SockOpt.TRANSTYPE, Transtype.FILE))
    }

    @Test
    fun sendMsg2Test() {
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertEquals(-1, socket.sendMsg2("Hello World !"))
        assertEquals(Error.getLastErrorMessage(), "Connection does not exist.")
    }
}