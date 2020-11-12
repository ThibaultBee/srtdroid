package com.github.thibaultbee.srtwrapper.models

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.thibaultbee.srtwrapper.Srt
import com.github.thibaultbee.srtwrapper.enums.*
import com.github.thibaultbee.srtwrapper.models.rejectreason.InternalRejectReason
import com.github.thibaultbee.srtwrapper.models.rejectreason.PredefinedRejectReason
import com.github.thibaultbee.srtwrapper.models.rejectreason.UserDefinedRejectReason
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
        assert(srt.startUp() >= 0)
        socket = Socket()
        assertTrue(socket.isValid())
    }

    @After
    fun tearDown() {
        if (socket.isValid())
            assertEquals(socket.close(), 0)
        assertEquals(srt.cleanUp(), 0)
    }

    @Test
    fun inetConstructorTest() {
        assertTrue(Socket(StandardProtocolFamily.INET).isValid())
    }

    @Test
    fun inet6ConstructorTest() {
        assertTrue(Socket(StandardProtocolFamily.INET6).isValid())
    }

    @Test
    fun bindTest() {
        assertEquals(0, socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE))
        assertEquals(0, socket.bind("127.0.3.1", 1234))
    }

    @Test
    fun sockStatusTest() {
        assertEquals(SockStatus.INIT, socket.sockState)
        assertFalse(socket.isBound)
        assertEquals(0, socket.bind("127.0.3.1", 1234))
        assertEquals(SockStatus.OPENED, socket.sockState)
        assertTrue(socket.isBound)
    }

    @Test
    fun closeTest() {
        assertEquals(0, socket.close())
        assertTrue(socket.isClose)
        assertFalse(socket.isValid())
    }

    @Test
    fun listenTest() {
        assertEquals(-1, socket.listen(3))
        assertEquals(Error.getLastError(), ErrorType.EUNBOUNDSOCK)
        assertEquals(0, socket.bind("127.0.3.1", 1234))
        assertEquals(0, socket.listen(3))
    }

    @Test
    fun acceptTest() {
        val pair = socket.accept()
        assertFalse(pair.first.isValid())
        assertNull(pair.second)
        assertEquals(Error.getLastError(), ErrorType.ENOLISTEN)
    }

    @Test
    fun connectTest() {
        assertEquals(-1, socket.connect("127.0.3.1", 1234))
        assertEquals(Error.getLastError(), ErrorType.ENOSERVER)
        assertEquals(InternalRejectReason(RejectReasonCode.TIMEOUT), socket.rejectReason)
    }

    @Test
    fun rendezVousTest() {
        assertEquals(-1, socket.rendezVous("0.0.0.0", "127.0.3.1", 1234))
        assertEquals(Error.getLastError(), ErrorType.ENOSERVER)
    }

    @Test
    fun getPeerNameTest() {
        assertNull(socket.peerName)
    }

    @Test
    fun getSockNameTest() {
        assertNull(socket.sockName)
        assertEquals(0, socket.bind("127.0.0.1", 1234))
        assertNull(socket.sockName) // sockAddr is null if no connection
    }

    @Test
    fun getSockOptTest() {
        assertNull(socket.getSockFlag(SockOpt.TRANSTYPE))
        assertEquals(true, socket.getSockFlag(SockOpt.RCVSYN))
        assertEquals(-1, socket.getSockFlag(SockOpt.SNDTIMEO))
        assertEquals(-1L, socket.getSockFlag(SockOpt.MAXBW))
        assertEquals(KMState.KM_S_UNSECURED, socket.getSockFlag(SockOpt.RCVKMSTATE))
        assertEquals("", socket.getSockFlag(SockOpt.STREAMID))
    }

    @Test
    fun setSockOptTest() {
        assertEquals(0, socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE))
        assertEquals(0, socket.setSockFlag(SockOpt.RCVSYN, true))
        assertEquals(0, socket.setSockFlag(SockOpt.SNDTIMEO, 100))
        assertEquals(0, socket.setSockFlag(SockOpt.MAXBW, 100L))
        assertEquals(0, socket.setSockFlag(SockOpt.STREAMID, "Hello"))
    }

    @Test
    fun sendMsg1Test() {
        assertEquals(-1, socket.send("Hello World !"))
        assertEquals(Error.getLastError(), ErrorType.ENOCONN)
    }

    @Test
    fun sendMsg2Test() {
        assertEquals(-1, socket.sendMsg("Hello World !", -1, false))
        assertEquals(Error.getLastError(), ErrorType.ENOCONN)
    }

    @Test
    fun sendMsg3Test() {
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
        assertNull(socket.recv(4 /*Int nb bytes*/).second)
    }

    @Test
    fun recvMsg2Test() {
        assertNull(
            socket.recvMsg2(
                4 /*Int nb bytes*/,
                MsgCtrl(flags = 0, boundary = 0, pktSeq = 0, no = 10)
            ).second
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
        assertEquals(-1, socket.sendFile(createTestFile()))
        assertEquals(Error.getLastError(), ErrorType.ENOCONN)
    }

    @Test
    fun recvFileTest() {
        val myFile = File(
            InstrumentationRegistry.getInstrumentation().context.externalCacheDir,
            "FileToRecv"
        )
        assertEquals(-1, socket.recvFile(myFile, 0, 1024))
        assertEquals(Error.getLastError(), ErrorType.ENOCONN)
    }

    @Test
    fun getRejectReasonTest() {
        assertEquals(InternalRejectReason(RejectReasonCode.UNKNOWN), socket.rejectReason)
    }

    @Test
    fun setRejectReasonTest() {
        socket.rejectReason = InternalRejectReason(RejectReasonCode.BADSECRET) // Generate an error
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