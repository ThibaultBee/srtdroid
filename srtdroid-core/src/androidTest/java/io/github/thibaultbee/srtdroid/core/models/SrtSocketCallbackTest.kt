package io.github.thibaultbee.srtdroid.core.models

import io.github.thibaultbee.srtdroid.core.Srt
import io.github.thibaultbee.srtdroid.core.enums.ErrorType
import io.github.thibaultbee.srtdroid.core.enums.SockOpt
import io.github.thibaultbee.srtdroid.core.enums.SockStatus
import io.github.thibaultbee.srtdroid.core.enums.Transtype
import io.github.thibaultbee.srtdroid.core.utils.ConditionalLocalNetworkPermissionRule
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.Thread.sleep
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class SrtSocketCallbackTest {
    @get:Rule
    val permissionRule = ConditionalLocalNetworkPermissionRule()

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
        socket.clientListener =
            object : SrtSocket.ClientListener {
                override fun onConnectionLost(
                    ns: SrtSocket,
                    error: ErrorType,
                    peerAddress: InetSocketAddress,
                    token: Int
                ) {
                    lock.countDown()
                }
            }

        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        futureResult.get()

        lock.await(1000, TimeUnit.MILLISECONDS)
        assertEquals(0, lock.count)

        server.shutdown()
    }

    @Test
    fun onListenRejectAfterSecondListenTest() {
        val server = listenTwice(-1)

        socket.setSockFlag(SockOpt.CONNTIMEO, 1000)
        try {
            socket.connect(InetAddress.getLoopbackAddress(), server.localPort)
            fail("Connection must be rejected by the server listener")
        } catch (e: Exception) {
        }

        server.close()
    }

    @Test
    fun onListenAcceptAfterSecondListenTest() {
        val server = listenTwice(0)

        socket.setSockFlag(SockOpt.CONNTIMEO, 1000)
        socket.connect(InetAddress.getLoopbackAddress(), server.localPort)
        assertEquals(SockStatus.CONNECTED, socket.sockState)

        server.close()
    }

    /**
     * Create a server that listens twice and answers every connection with [verdict].
     */
    private fun listenTwice(verdict: Int): SrtSocket {
        val server = SrtSocket()
        server.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
        server.serverListener =
            object : SrtSocket.ServerListener {
                override fun onListen(
                    ns: SrtSocket,
                    hsVersion: Int,
                    peerAddress: InetSocketAddress,
                    streamId: String
                ) = verdict
            }
        server.bind(InetAddress.getLoopbackAddress(), 0)
        server.listen(1)
        server.listen(1)
        return server
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
            serverSocket.listen(1)
            port = serverSocket.localPort
        }

        fun enqueue(): Future<Boolean> {
            return executor.submit(Callable {
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