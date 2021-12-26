package com.github.thibaultbee.srtdroid.models

import com.github.thibaultbee.srtdroid.Srt
import com.github.thibaultbee.srtdroid.enums.SockOpt
import com.github.thibaultbee.srtdroid.enums.Transtype
import com.github.thibaultbee.srtdroid.utils.Utils
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.lang.Thread.sleep
import java.net.InetAddress
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class SocketRecvTest {
    private lateinit var socket: Socket
    private val server = ServerSend()

    @Before
    fun setUp() {
        socket = Socket()
        Assert.assertTrue(socket.isValid)
        socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
    }

    @After
    fun tearDown() {
        server.shutdown()
        socket.close()
        Srt.cleanUp()
    }

    @Test
    fun recvIntputStream() {
        val arraySize = 1000
        val expectedArray = Utils.generateRandomArray(arraySize)
        val futureResult = server.enqueue(expectedArray)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val actualArray = ByteArray(arraySize)
        socket.getInputStream().use {
            it.read(actualArray)
        }

        val numOfSentBytes = futureResult.get(1000, TimeUnit.MILLISECONDS)
        Assert.assertEquals(arraySize, numOfSentBytes)
        Assert.assertArrayEquals(expectedArray, actualArray)
    }

    @Test
    fun recvSimple() {
        val arraySize = 1000
        val expectedArray = Utils.generateRandomArray(arraySize)
        val futureResult = server.enqueue(expectedArray)
        sleep(1000)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val actualArray = socket.recv(arraySize).second

        val numOfSentBytes = futureResult.get(1000, TimeUnit.MILLISECONDS)
        Assert.assertEquals(arraySize, numOfSentBytes)
        Assert.assertArrayEquals(expectedArray, actualArray)
    }

    @Test
    fun recv2Simple() {
        val arraySize = 1000
        val expectedArray = Utils.generateRandomArray(arraySize)
        val futureResult = server.enqueue(expectedArray)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val actualArray = socket.recv(arraySize, msgCtrl = MsgCtrl(ttl = 100)).second

        val numOfSentBytes = futureResult.get(1000, TimeUnit.MILLISECONDS)
        Assert.assertEquals(arraySize, numOfSentBytes)
        Assert.assertArrayEquals(expectedArray, actualArray)
    }

    @Test
    fun recvInMemory() {
        val arraySize = 1000
        val expectedArray = Utils.generateRandomArray(arraySize)
        val futureResult = server.enqueue(expectedArray)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val actualArray = ByteArray(arraySize)
        socket.recv(actualArray)

        val numOfSentBytes = futureResult.get(1000, TimeUnit.MILLISECONDS)
        Assert.assertEquals(arraySize, numOfSentBytes)
        Assert.assertArrayEquals(expectedArray, actualArray)
    }

    @Test
    fun recvInMemoryWithOffsetAndLength() {
        val arraySize = 1000
        val offset = 10
        val length = 100
        val expectedArray = Utils.generateRandomArray(length)
        val futureResult = server.enqueue(expectedArray)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val actualArray = ByteArray(arraySize)
        socket.recv(actualArray, offset, length)

        val numOfSentBytes = futureResult.get(1000, TimeUnit.MILLISECONDS)
        Assert.assertEquals(length, numOfSentBytes)
        Assert.assertArrayEquals(expectedArray, actualArray.copyOfRange(offset, offset + length))
    }

    @Test
    fun recv2InMemoryWithOffsetAndLength() {
        val arraySize = 1000
        val offset = 10
        val length = 100
        val expectedArray = Utils.generateRandomArray(length)
        val futureResult = server.enqueue(expectedArray)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val actualArray = ByteArray(arraySize)
        socket.recv(actualArray, offset, length, msgCtrl = MsgCtrl(ttl = 100))

        val numOfSentBytes = futureResult.get(1000, TimeUnit.MILLISECONDS)
        Assert.assertEquals(length, numOfSentBytes)
        Assert.assertArrayEquals(expectedArray, actualArray.copyOfRange(offset, offset + length))
    }

    internal class ServerSend {
        private val executor = Executors.newCachedThreadPool()
        private val serverSocket = Socket()

        val port: Int

        init {
            serverSocket.reuseAddress = true
            serverSocket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
            serverSocket.bind(InetAddress.getLoopbackAddress(), 0)
            port = serverSocket.localPort
        }

        fun enqueue(array: ByteArray): Future<Int> {
            return executor.submit(Callable {
                serverSocket.listen(1)
                val pair = serverSocket.accept()
                val comSocket = pair.first
                val numOfSentBytes = comSocket.send(array)
                comSocket.close()
                numOfSentBytes
            })
        }

        fun shutdown() {
            serverSocket.close()
            executor.shutdown()
        }
    }
}