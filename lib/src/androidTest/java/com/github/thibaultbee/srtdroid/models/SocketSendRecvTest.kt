package com.github.thibaultbee.srtdroid.models

import com.github.thibaultbee.srtdroid.Srt
import com.github.thibaultbee.srtdroid.enums.SockOpt
import com.github.thibaultbee.srtdroid.enums.Transtype
import com.github.thibaultbee.srtdroid.utils.Utils
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class SocketSendRecvTest {
    private lateinit var socket: Socket
    private val server = ServerRecv()

    @Before
    fun setUp() {
        socket = Socket()
        assertTrue(socket.isValid)
        socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
    }

    @After
    fun tearDown() {
        server.shutdown()
        socket.close()
        Srt.cleanUp()
    }

    @Test
    fun sendOutputStream() {
        val arraySize = 1000
        val futureResult = server.enqueue(arraySize)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val expectedArray = Utils.generateRandomArray(arraySize)
        socket.getOutputStream().use {
            it.write(expectedArray)
        }
        val resultedArray = futureResult.get(1000, TimeUnit.MILLISECONDS)
        assertArrayEquals(expectedArray, resultedArray)
    }

    @Test
    fun sendByteBuffer() {
        val bufferSize = 1000
        val futureResult = server.enqueue(bufferSize)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val expectedBuffer = Utils.generateRandomDirectBuffer(bufferSize)
        socket.send(expectedBuffer)
        val resultedArray = futureResult.get(1000, TimeUnit.MILLISECONDS)
        assertByteBufferEquals(expectedBuffer, ByteBuffer.wrap(resultedArray))
    }

    @Test
    fun sendByteBufferWithOffset() {
        val bufferSize = 1000
        val offset = 10
        val futureResult = server.enqueue(bufferSize - offset)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val expectedBuffer = Utils.generateRandomDirectBuffer(bufferSize)
        expectedBuffer.position(offset)
        socket.send(expectedBuffer)
        val resultedArray = futureResult.get(1000, TimeUnit.MILLISECONDS)
        assertByteBufferEquals(expectedBuffer, ByteBuffer.wrap(resultedArray))
    }

    @Test
    fun sendByteBufferWithOffsetAndLength() {
        val bufferSize = 1000
        val offset = 10
        val length = 100
        val futureResult = server.enqueue(length)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val expectedBuffer = Utils.generateRandomDirectBuffer(bufferSize)
        expectedBuffer.position(offset)
        expectedBuffer.limit(offset + length)
        socket.send(expectedBuffer)
        val resultedArray = futureResult.get(1000, TimeUnit.MILLISECONDS)
        assertByteBufferEquals(expectedBuffer, ByteBuffer.wrap(resultedArray))
    }

    @Test
    fun sendSimpleByteArray() {
        val arraySize = 1000
        val futureResult = server.enqueue(arraySize)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val expectedArray = Utils.generateRandomArray(arraySize)
        socket.send(expectedArray)
        val resultedArray = futureResult.get(1000, TimeUnit.MILLISECONDS)
        assertArrayEquals(expectedArray, resultedArray)
    }

    @Test
    fun sendByteArrayWithOffset() {
        val arraySize = 1000
        val offset = 10
        val futureResult = server.enqueue(arraySize - offset)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val expectedArray = Utils.generateRandomArray(arraySize)
        socket.send(expectedArray, offset = offset, size = arraySize - offset)
        val resultedArray = futureResult.get(1000, TimeUnit.MILLISECONDS)
        assertArrayEquals(expectedArray.copyOfRange(offset, arraySize), resultedArray)
    }

    @Test
    fun sendByteArrayWithOffsetAndLength() {
        val arraySize = 1000
        val offset = 10
        val length = 100
        val futureResult = server.enqueue(length)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val expectedArray = Utils.generateRandomArray(arraySize)
        socket.send(expectedArray, offset = offset, size = length)
        val resultedArray = futureResult.get(1000, TimeUnit.MILLISECONDS)
        assertArrayEquals(expectedArray.copyOfRange(offset, offset + length), resultedArray)
    }


    private fun assertByteBufferEquals(expectedBuffer: ByteBuffer, resultedBuffer: ByteBuffer) {
        assertEquals(expectedBuffer.remaining(), resultedBuffer.remaining())
        while(expectedBuffer.hasRemaining()) {
            assertEquals("Not equals at position ${expectedBuffer.position()}", expectedBuffer.get(), resultedBuffer.get())
        }
    }

    internal class ServerRecv() {
        private val executor = Executors.newCachedThreadPool()
        private val serverSocket = Socket()

        val port: Int

        init {
            serverSocket.reuseAddress = true
            serverSocket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
            serverSocket.bind(InetAddress.getLoopbackAddress(), 0)
            port = serverSocket.localPort
        }

        fun enqueue(receiveByteCount: Int): Future<ByteArray> {
            return executor.submit(Callable {
                serverSocket.listen(1)
                val pair = serverSocket.accept()
                val comSocket = pair.first
                val byteArray = comSocket.recv(receiveByteCount).second
                comSocket.close()
                byteArray
            })
        }

        fun shutdown() {
            serverSocket.close()
            executor.shutdown()
        }
    }
}