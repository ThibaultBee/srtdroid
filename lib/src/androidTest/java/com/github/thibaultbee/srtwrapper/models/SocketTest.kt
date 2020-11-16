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
import java.io.IOException
import java.net.InetAddress
import java.net.SocketException
import java.net.StandardProtocolFamily
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.random.Random


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
        socket.close()
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
        assertEquals(0, socket.bind("127.0.3.1", 1235))
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
        assertEquals(-1, socket.listen(3))
        assertEquals(Error.lastError, ErrorType.EUNBOUNDSOCK)
        assertEquals(0, socket.bind("127.0.3.1", 1236))
        assertEquals(0, socket.listen(3))
    }

    @Test
    fun acceptTest() {
        val pair = socket.accept()
        assertFalse(pair.first.isValid())
        assertNull(pair.second)
        assertEquals(Error.lastError, ErrorType.ENOLISTEN)
    }

    @Test
    fun connectTest() {
        assertEquals(-1, socket.connect("127.0.3.1", 1237))
        assertEquals(Error.lastError, ErrorType.ENOSERVER)
        assertEquals(InternalRejectReason(RejectReasonCode.TIMEOUT), socket.rejectReason)
    }

    @Test
    fun rendezVousTest() {
        assertEquals(-1, socket.rendezVous("0.0.0.0", "127.0.3.1", 1238))
        assertEquals(Error.lastError, ErrorType.ENOSERVER)
    }

    @Test
    fun getPeerNameTest() {
        assertNull(socket.peerName)
    }

    @Test
    fun getSockNameTest() {
        assertNull(socket.sockName)
        assertEquals(0, socket.bind("127.0.3.1", 1239))
        assertEquals("127.0.3.1", socket.sockName!!.address.hostAddress)
        assertEquals(1239, socket.sockName!!.port)
    }

    @Test
    fun getSockOptTest() {
        assertNull(socket.getSockFlag(SockOpt.TRANSTYPE)) // Write only property
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
        assertEquals(Error.lastError, ErrorType.ENOCONN)
    }

    @Test
    fun sendMsg2Test() {
        assertEquals(-1, socket.send("Hello World !", -1, false))
        assertEquals(Error.lastError, ErrorType.ENOCONN)
    }

    @Test
    fun sendMsg3Test() {
        assertEquals(-1, socket.send("Hello World !", null))
        assertEquals(Error.lastError, ErrorType.ENOCONN)
        assertEquals(
            -1,
            socket.send("Hello World !", MsgCtrl(flags = 0, boundary = 0, pktSeq = 0, no = 10))
        )
        assertEquals(Error.lastError, ErrorType.ENOCONN)
    }

    @Test
    fun recvTest() {
        assert(socket.recv(4 /*Int nb bytes*/).second.isEmpty())
    }

    @Test
    fun recvMsg2Test() {
        assert(
            socket.recv(
                4 /*Int nb bytes*/,
                MsgCtrl(flags = 0, boundary = 0, pktSeq = 0, no = 10)
            ).second.isEmpty()
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
        assertEquals(Error.lastError, ErrorType.ENOCONN)
    }

    @Test
    fun recvFileTest() {
        val myFile = File(
            InstrumentationRegistry.getInstrumentation().context.externalCacheDir,
            "FileToRecv"
        )
        assertEquals(-1, socket.recvFile(myFile, 0, 1024))
        assertEquals(Error.lastError, ErrorType.ENOCONN)
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

    @Test
    fun inputStreamTest() {
        val inputStream = socket.getInputStream()
        assertEquals(0, inputStream.read(ByteArray(0)))
        assertEquals(-1, inputStream.read())
        assertEquals(-1, inputStream.read(ByteArray(10)))
        val server = MockServer()
        server.enqueue()
        assertEquals(0, socket.connect(InetAddress.getLoopbackAddress(), server.port))
        socket.close()
        assertEquals(0, inputStream.read(ByteArray(0)))
        assertEquals(-1, inputStream.read())
        assertEquals(-1, inputStream.read(ByteArray(10)))
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

    @Test
    fun inputStreamLiveTest() {
        val server = InOutMockServer(Transtype.LIVE)
        val socket = Socket()
        val arraySize = socket.getSockFlag(SockOpt.PAYLOADSIZE) as Int
        val serverByteArray = ByteArray(arraySize)
        Random.Default.nextBytes(serverByteArray)
        server.enqueue(serverByteArray, 0)
        assertEquals(0, socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.LIVE))
        assertEquals(0, socket.connect(InetAddress.getLoopbackAddress(), server.port))
        val inputStream = socket.getInputStream()
        val byteArray = ByteArray(arraySize)
        assertEquals(arraySize, inputStream.read(byteArray))
        assertArrayEquals(serverByteArray, byteArray)
        socket.close()
        inputStream.close()
        server.shutdown()
    }

    @Test
    fun outputStreamLiveTest() {
        val server = InOutMockServer(Transtype.LIVE)
        val socket = Socket()
        val arraySize = socket.getSockFlag(SockOpt.PAYLOADSIZE) as Int
        server.enqueue(ByteArray(arraySize), arraySize)
        assertEquals(0, socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.LIVE))
        assertEquals(0, socket.connect(InetAddress.getLoopbackAddress(), server.port))
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
        val server = InOutMockServer(Transtype.FILE)
        server.enqueue(byteArrayOf(5, 3), 0)
        val socket = Socket()
        assertEquals(0, socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE))
        assertEquals(0, socket.setSockFlag(SockOpt.RCVTIMEO, 1000))
        assertEquals(0, socket.connect(InetAddress.getLoopbackAddress(), server.port))
        val inputStream = socket.getInputStream()
        assertEquals(5, inputStream.read())
        assertEquals(3, inputStream.read())
        assertEquals(-1, inputStream.read())
        assertEquals(-1, inputStream.read())
        socket.close()
        inputStream.close()

        assertEquals(-1, inputStream.read())
        assertEquals(-1, inputStream.read())
        server.shutdown()
    }

    @Test
    fun outputStreamFileTest() {
        val server = InOutMockServer(Transtype.FILE)
        server.enqueue(ByteArray(0), 3)
        val socket = Socket()
        assertEquals(0, socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE))
        assertEquals(0, socket.connect(InetAddress.getLoopbackAddress(), server.port))
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

    internal class MockServer {
        private val executor = Executors.newCachedThreadPool()
        private val serverSocket = Socket()
        val port: Int
        private var socket: Socket? = null

        init {
            serverSocket.reuseAddress = true
            assertEquals(0, serverSocket.bind(InetAddress.getLoopbackAddress(), 0))
            port = serverSocket.localPort
        }

        fun enqueue(): Future<Unit> {
            return executor.submit(Callable {
                assertEquals(0, serverSocket.listen(1))
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

    internal class InOutMockServer(transtype: Transtype) {
        private val executor = Executors.newCachedThreadPool()
        private val serverSocket = Socket()
        private lateinit var socket: Socket
        val port: Int

        init {
            serverSocket.reuseAddress = true
            assertEquals(0, serverSocket.setSockFlag(SockOpt.TRANSTYPE, transtype))
            assertEquals(0, serverSocket.bind(InetAddress.getLoopbackAddress(), 0))
            port = serverSocket.localPort
        }

        fun enqueue(sendBytes: ByteArray, receiveByteCount: Int): Future<ByteArray?> {
            return executor.submit(Callable<ByteArray?> {
                assertEquals(0, serverSocket.listen(1))
                val pair = serverSocket.accept()
                assertNotNull(pair.second)
                socket = pair.first
                val outputStream = socket.getOutputStream()
                assertEquals(sendBytes.size, outputStream.write(sendBytes))
                val inputStream = socket.getInputStream()
                val result = ByteArray(receiveByteCount)
                var total = 0
                while (total < receiveByteCount) {
                    total += inputStream.read(result, total, result.size - total)
                }
                result
            })
        }

        fun shutdown() {
            socket.close()
            serverSocket.close()
            executor.shutdown()
        }
    }

}