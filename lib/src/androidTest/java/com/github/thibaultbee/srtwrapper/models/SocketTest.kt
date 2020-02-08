package com.github.thibaultbee.srtwrapper.models

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.thibaultbee.srtwrapper.Srt
import com.github.thibaultbee.srtwrapper.enums.ErrorType
import com.github.thibaultbee.srtwrapper.enums.SockOpt
import com.github.thibaultbee.srtwrapper.enums.SockStatus
import com.github.thibaultbee.srtwrapper.enums.Transtype
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileWriter
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
        assertEquals(0, socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE))
        assertEquals(0, socket.bind("127.0.3.1", 1234))
    }

    @Test
    fun sockStatusTest() {
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertEquals(SockStatus.INIT, socket.getSockState())
        assertEquals(0, socket.bind("127.0.3.1", 1234))
        assertEquals(SockStatus.OPENED, socket.getSockState())
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
        assertEquals(Error.getLastError(), ErrorType.EUNBOUNDSOCK)
        assertEquals(0, socket.bind("127.0.3.1", 1234))
        assertEquals(0, socket.listen(3))
    }

    @Test
    fun acceptTest() {
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        val pair = socket.accept()
        assertFalse(pair.first.isValid())
        assertEquals(Error.getLastError(), ErrorType.ENOLISTEN)
    }

    @Test
    fun connectTest() {
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertEquals(-1, socket.connect("127.0.3.1", 1234))
        assertEquals(Error.getLastError(), ErrorType.ENOSERVER)
    }

    @Test
    fun rendezVousTest() {
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertEquals(-1, socket.rendezVous("0.0.0.0", "127.0.3.1", 1234))
        assertEquals(Error.getLastError(), ErrorType.ENOSERVER)
    }

    @Test
    fun getPeerNameTest() {
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertNull(socket.getPeerName())
    }

    @Test
    fun getSockNameTest() {
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertNull(socket.getSockName())
        assertEquals(0, socket.bind("127.0.0.1", 1234))
        val sockAddr = socket.getSockName()
        assertEquals(sockAddr!!.port, 1234)
        assertEquals(sockAddr!!.hostString, "127.0.0.1")
    }

    @Test
    fun setSockOptTest() {
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertEquals(0, socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE))
        assertEquals(0, socket.setSockFlag(SockOpt.RCVSYN, true))
        assertEquals(0, socket.setSockFlag(SockOpt.SNDTIMEO, 100))
        assertEquals(0, socket.setSockFlag(SockOpt.MAXBW, 100L))
    }

    @Test
    fun sendMsg1Test() {
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertEquals(-1, socket.send("Hello World !"))
        assertEquals(Error.getLastError(), ErrorType.ENOCONN)
    }

    @Test
    fun sendMsg2Test() {
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertEquals(-1, socket.sendMsg("Hello World !", -1, false))
        assertEquals(Error.getLastError(), ErrorType.ENOCONN)
    }

    @Test
    fun sendMsg3Test() {
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertEquals(-1, socket.sendMsg2("Hello World !", null))
        assertEquals(Error.getLastError(), ErrorType.ENOCONN)
        assertEquals(
            -1,
            socket.sendMsg2("Hello World !", MsgCtrl(flags = 0, boundary = 0, pktSeq = 0, no = 10))
        )
        assertEquals(Error.getLastError(), ErrorType.ENOCONN)
    }

    @Test
    fun recvTest() {
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertNull(socket.recv(4 /*Int nb bytes*/))
    }

    @Test
    fun recvMsg2Test() {
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertNull(
            socket.recvMsg2(
                4 /*Int nb bytes*/,
                MsgCtrl(flags = 0, boundary = 0, pktSeq = 0, no = 10)
            )
        )
    }

    private fun createTestFile(): File {
        val myFile = File(
            InstrumentationRegistry.getInstrumentation().context.externalCacheDir,
            "FileToSend"
        )
        val fw = FileWriter(myFile)
        fw.write("Hello ! Did someone receive this message?")
        fw.close()
        return myFile
    }

    @Test
    fun sendFileTest() {
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertEquals(-1, socket.sendFile(createTestFile()))
        assertEquals(Error.getLastError(), ErrorType.ENOCONN)
    }

    @Test
    fun recvFileTest() {
        val myFile = File(
            InstrumentationRegistry.getInstrumentation().context.externalCacheDir,
            "FileToRecv"
        )
        socket = Socket(StandardProtocolFamily.INET)
        assertTrue(socket.isValid())
        assertEquals(-1, socket.recvFile(myFile, 0, 1024))
        assertEquals(Error.getLastError(), ErrorType.ENOCONN)
    }
}