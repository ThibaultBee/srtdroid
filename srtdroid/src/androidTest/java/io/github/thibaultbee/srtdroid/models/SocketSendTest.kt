package io.github.thibaultbee.srtdroid.models

import io.github.thibaultbee.srtdroid.Srt
import io.github.thibaultbee.srtdroid.enums.SockOpt
import io.github.thibaultbee.srtdroid.enums.Transtype
import io.github.thibaultbee.srtdroid.utils.Utils
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

class SocketSendTest {
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
        val actualArray = futureResult.get(1000, TimeUnit.MILLISECONDS)
        assertArrayEquals(expectedArray, actualArray)
    }

    @Test
    fun sendByteBuffer() {
        val bufferSize = 1000
        val futureResult = server.enqueue(bufferSize)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val expectedBuffer = Utils.generateRandomDirectBuffer(bufferSize)
        socket.send(expectedBuffer)
        val actualArray = futureResult.get(1000, TimeUnit.MILLISECONDS)
        Utils.assertByteBufferEquals(expectedBuffer, ByteBuffer.wrap(actualArray))
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
        val actualArray = futureResult.get(1000, TimeUnit.MILLISECONDS)
        Utils.assertByteBufferEquals(expectedBuffer, ByteBuffer.wrap(actualArray))
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
        val actualArray = futureResult.get(1000, TimeUnit.MILLISECONDS)
        Utils.assertByteBufferEquals(expectedBuffer, ByteBuffer.wrap(actualArray))
    }

    @Test
    fun sendByteBuffer2WithOffsetAndLength() {
        val bufferSize = 1000
        val offset = 10
        val length = 100
        val futureResult = server.enqueue(length)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val expectedBuffer = Utils.generateRandomDirectBuffer(bufferSize)
        expectedBuffer.position(offset)
        expectedBuffer.limit(offset + length)
        socket.send(expectedBuffer, ttl = 1000, inOrder = false)
        val actualArray = futureResult.get(1000, TimeUnit.MILLISECONDS)
        Utils.assertByteBufferEquals(expectedBuffer, ByteBuffer.wrap(actualArray))
    }

    @Test
    fun sendByteBuffer3WithOffsetAndLength() {
        val bufferSize = 1000
        val offset = 10
        val length = 100
        val futureResult = server.enqueue(length)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val expectedBuffer = Utils.generateRandomDirectBuffer(bufferSize)
        expectedBuffer.position(offset)
        expectedBuffer.limit(offset + length)
        socket.send(expectedBuffer, MsgCtrl(inOrder = false))
        val actualArray = futureResult.get(1000, TimeUnit.MILLISECONDS)
        Utils.assertByteBufferEquals(expectedBuffer, ByteBuffer.wrap(actualArray))
    }

    @Test
    fun sendSimpleByteArray() {
        val arraySize = 1000
        val futureResult = server.enqueue(arraySize)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val expectedArray = Utils.generateRandomArray(arraySize)
        socket.send(expectedArray)
        val actualArray = futureResult.get(1000, TimeUnit.MILLISECONDS)
        assertArrayEquals(expectedArray, actualArray)
    }

    @Test
    fun sendByteArrayWithOffset() {
        val arraySize = 1000
        val offset = 10
        val futureResult = server.enqueue(arraySize - offset)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val expectedArray = Utils.generateRandomArray(arraySize)
        socket.send(expectedArray, offset = offset, size = arraySize - offset)
        val actualArray = futureResult.get(1000, TimeUnit.MILLISECONDS)
        assertArrayEquals(expectedArray.copyOfRange(offset, arraySize), actualArray)
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
        val actualArray = futureResult.get(1000, TimeUnit.MILLISECONDS)
        assertArrayEquals(expectedArray.copyOfRange(offset, offset + length), actualArray)
    }

    @Test
    fun sendByteArray2WithOffsetAndLength() {
        val arraySize = 1000
        val offset = 10
        val length = 100
        val futureResult = server.enqueue(length)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val expectedArray = Utils.generateRandomArray(arraySize)
        socket.send(expectedArray, offset = offset, size = length, ttl = 1000, inOrder = false)
        val actualArray = futureResult.get(1000, TimeUnit.MILLISECONDS)
        assertArrayEquals(expectedArray.copyOfRange(offset, offset + length), actualArray)
    }

    @Test
    fun sendByteArray3WithOffsetAndLength() {
        val arraySize = 1000
        val offset = 10
        val length = 100
        val futureResult = server.enqueue(length)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val expectedArray = Utils.generateRandomArray(arraySize)
        socket.send(expectedArray, offset = offset, size = length, MsgCtrl(inOrder = false))
        val actualArray = futureResult.get(1000, TimeUnit.MILLISECONDS)
        assertArrayEquals(expectedArray.copyOfRange(offset, offset + length), actualArray)
    }

    internal class ServerRecv {
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
                val byteArray = comSocket.recv(receiveByteCount)
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