package io.github.thibaultbee.srtdroid.example.tests

import android.util.Log
import androidx.core.util.component1
import androidx.core.util.component2
import io.github.thibaultbee.srtdroid.core.enums.SockOpt
import io.github.thibaultbee.srtdroid.ktx.CoroutineSrtSocket
import io.github.thibaultbee.srtdroid.ktx.bind
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class TestServer : Test {
    private val numOfMessages = 100

    companion object {
        private val TAG = TestServer::class.simpleName
    }

    override val name: String = this::class.simpleName!!

    override suspend fun run(ip: String, port: Int) {
        Log.i(TAG, "Waiting $numOfMessages messages from the client")
        val socket = CoroutineSrtSocket()

        try {
            Log.i(TAG, "Will bind on $ip:$port")
            socket.bind(ip, port)
            socket.listen(10)

            Log.i(TAG, "Waiting for incoming socket on $ip:$port")
            val (clientSocket, _) = socket.acceptFlow().first()

            Log.i(TAG, "Get an incoming connection")

            clientSocket.setSockFlag(SockOpt.RCVTIMEO, 3000)
            repeat(numOfMessages) {
                val message = clientSocket.recv(2048)
                Log.i(TAG, "#$it >> Got msg of length ${message.size} << ${String(message)}")
            }

            // If session is close too early, last msg will not be receive by server
            clientSocket.close()
            delay(1000)

            Log.i(TAG, "Received $numOfMessages messages (check logcat)")
        } catch (e: Exception) {
            throw e
        } finally {
            socket.close()
        }
    }

}