package io.github.thibaultbee.srtdroid.models

import io.github.thibaultbee.srtdroid.Srt
import io.github.thibaultbee.srtdroid.enums.ErrorType
import io.github.thibaultbee.srtdroid.enums.SockOpt
import io.github.thibaultbee.srtdroid.enums.Transtype
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.lang.Thread.sleep
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.*

class SrtSocketCallbackTest {
    private lateinit var socket: SrtSocket

    @Before
    fun setUp() {
        socket = SrtSocket()
        assertTrue(socket.isValid)
        socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
    }

    @After
    fun tearDown() {
        socket.close()
        Srt.cleanUp()
    }

    @Test
    fun onConnectTest() {
        val lock = CountDownLatch(1)
        val server = ServerConnectClose()

        val futureResult = server.enqueue()
        socket.clientListener = object : SrtSocket.ClientListener {
            override fun onConnectionLost(ns: SrtSocket, error: ErrorType, peerAddress: InetSocketAddress, token: Int) {
                lock.countDown()
            }
        }

        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        futureResult.get()

        lock.await(1000, TimeUnit.MILLISECONDS)
        assertEquals(0, lock.count)

        server.shutdown()
    }

    /**
     * Create a server that close its communication Socket after 100 ms.
     */
    internal class ServerConnectClose {
        private val executor = Executors.newCachedThreadPool()
        private val serverSocket = SrtSocket()

        val port: Int

        init {
            serverSocket.reuseAddress = true
            serverSocket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
            serverSocket.bind(InetAddress.getLoopbackAddress(), 0)
            port = serverSocket.localPort
        }

        fun enqueue(): Future<Boolean> {
            return executor.submit(Callable {
                serverSocket.listen(1)
                val pair = serverSocket.accept()
                val comSocket = pair.first
                // wait a while before closing the Socket
                sleep(100)
                comSocket.close()
                true
            })
        }

        fun shutdown() {
            serverSocket.close()
            executor.shutdown()
        }
    }
}