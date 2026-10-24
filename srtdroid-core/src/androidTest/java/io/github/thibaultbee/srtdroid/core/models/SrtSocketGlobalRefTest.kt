package io.github.thibaultbee.srtdroid.core.models

import io.github.thibaultbee.srtdroid.core.Srt
import io.github.thibaultbee.srtdroid.core.enums.SockOpt
import io.github.thibaultbee.srtdroid.core.enums.SockStatus
import io.github.thibaultbee.srtdroid.core.enums.Transtype
import io.github.thibaultbee.srtdroid.core.utils.ConditionalLocalNetworkPermissionRule
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.ref.WeakReference
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress

/**
 * Checks that a closed [SrtSocket] is not kept reachable by a JNI global reference from its connect.
 */
class SrtSocketGlobalRefTest {
    @get:Rule
    val permissionRule = ConditionalLocalNetworkPermissionRule()

    private lateinit var server: SrtSocket
    private lateinit var silentPeer: DatagramSocket

    @Before
    fun setUp() {
        server = SrtSocket()
        server.reuseAddress = true
        server.setSockFlag(SockOpt.TRANSTYPE, Transtype.LIVE)
        server.bind(InetAddress.getLoopbackAddress(), 0)
        server.listen(CONNECTION_COUNT)

        silentPeer = DatagramSocket(0, InetAddress.getLoopbackAddress())
    }

    @After
    fun tearDown() {
        silentPeer.close()
        server.close()
        Srt.cleanUp()
    }

    @Test
    fun connectedThenClosedSocketIsReleased() {
        val references = (1..CONNECTION_COUNT).map { connectThenClose() }

        assertEquals(0, countReachable(references))
    }

    @Test
    fun failedConnectThenClosedSocketIsReleased() {
        val references = (1..FAILED_CONNECTION_COUNT).map { failToConnectThenClose() }

        assertEquals(0, countReachable(references))
    }

    @Test
    fun socketClosedWhileConnectingIsReleased() {
        val references = (1..FAILED_CONNECTION_COUNT).map { closeWhileConnecting() }
        // Let the connection timeout expire after the close.
        Thread.sleep(CONNECTION_TIMEOUT_MS * 2L)

        assertEquals(0, countReachable(references))
    }

    private fun connectThenClose(): WeakReference<SrtSocket> {
        val socket = SrtSocket()
        socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.LIVE)
        socket.connect(InetAddress.getLoopbackAddress(), server.localPort)
        val comSocket = server.accept().first
        assertEquals(SockStatus.CONNECTED, socket.sockState)
        socket.close()
        // The server side may already be closed.
        runCatching { comSocket.close() }
        return WeakReference(socket)
    }

    private fun failToConnectThenClose(): WeakReference<SrtSocket> {
        val socket = nonBlockingSocket()
        socket.connect(InetAddress.getLoopbackAddress(), silentPeer.localPort)
        val deadline = System.currentTimeMillis() + CONNECTION_TIMEOUT_MS * 3L
        while (socket.sockState == SockStatus.CONNECTING && System.currentTimeMillis() < deadline) {
            Thread.sleep(20)
        }
        assertEquals(SockStatus.BROKEN, socket.sockState)
        socket.close()
        return WeakReference(socket)
    }

    private fun closeWhileConnecting(): WeakReference<SrtSocket> {
        val socket = nonBlockingSocket()
        socket.connect(InetAddress.getLoopbackAddress(), silentPeer.localPort)
        Thread.sleep(100)
        socket.close()
        return WeakReference(socket)
    }

    private fun nonBlockingSocket(): SrtSocket {
        val socket = SrtSocket()
        assertTrue(socket.isValid)
        socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.LIVE)
        socket.setSockFlag(SockOpt.CONNTIMEO, CONNECTION_TIMEOUT_MS)
        socket.setSockFlag(SockOpt.RCVSYN, false)
        return socket
    }

    private fun countReachable(references: List<WeakReference<SrtSocket>>): Int {
        var reachable = references.size
        val deadline = System.currentTimeMillis() + 5000
        while (reachable > 0 && System.currentTimeMillis() < deadline) {
            Runtime.getRuntime().gc()
            System.runFinalization()
            Thread.sleep(200)
            reachable = references.count { it.get() != null }
        }
        return reachable
    }

    companion object {
        private const val CONNECTION_COUNT = 50
        private const val FAILED_CONNECTION_COUNT = 10
        private const val CONNECTION_TIMEOUT_MS = 500
    }
}
