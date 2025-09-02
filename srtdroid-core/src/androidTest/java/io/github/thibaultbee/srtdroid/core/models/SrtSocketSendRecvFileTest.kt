package io.github.thibaultbee.srtdroid.core.models

import androidx.test.platform.app.InstrumentationRegistry
import io.github.thibaultbee.srtdroid.core.Srt
import io.github.thibaultbee.srtdroid.core.enums.SockOpt
import io.github.thibaultbee.srtdroid.core.enums.Transtype
import io.github.thibaultbee.srtdroid.core.utils.Utils
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.net.InetAddress
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

class SrtSocketSendRecvFileTest {
    private lateinit var socket: SrtSocket
    private val server = MockSendServer()

    @Before
    fun setUp() {
        socket = SrtSocket()
        socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
        assertTrue(socket.isValid)
    }

    @After
    fun tearDown() {
        socket.close()
        server.shutdown()
    }

    @Test
    fun recvFileTest() {
        val server = MockSendServer()
        val message = UUID.randomUUID().toString()
        val sendFile = Utils.createTestFile(message = message)

        val futureResult = server.enqueue(sendFile)
        socket.connect(InetAddress.getLoopbackAddress(), server.port)

        val recvFile = File(
            InstrumentationRegistry.getInstrumentation().context.externalCacheDir,
            UUID.randomUUID().toString()
        )
        assertEquals(sendFile.length(), socket.recvFile(recvFile, size = sendFile.length()))
        assertEquals(sendFile.length(), futureResult.get())
    }

    internal class MockSendServer {
        private val executor = Executors.newCachedThreadPool()
        private val serverSocket = SrtSocket()
        val port: Int

        init {
            serverSocket.reuseAddress = true
            serverSocket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
            serverSocket.bind(InetAddress.getLoopbackAddress(), 0)
            port = serverSocket.localPort
        }

        fun enqueue(file: File): Future<*> {
            return executor.submit(Callable {
                serverSocket.listen(1)
                val pair = serverSocket.accept()
                val comSocket = pair.first
                val numOfSentBytes = comSocket.sendFile(file)
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