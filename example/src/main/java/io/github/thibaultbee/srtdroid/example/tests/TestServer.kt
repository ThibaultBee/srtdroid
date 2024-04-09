package io.github.thibaultbee.srtdroid.example.tests

import android.util.Log
import io.github.thibaultbee.srtdroid.enums.SockOpt
import io.github.thibaultbee.srtdroid.models.Socket
import java.util.concurrent.ExecutorService

class TestServer(
    executorService: ExecutorService,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) : Test(executorService, onSuccess, onError) {
    companion object {
        private val TAG = TestServer::class.simpleName
    }
    
    override val testName: String = this::class.simpleName!!

    override fun launchImpl(ip: String, port: Int, socket: Socket) {
        Log.i(TAG, "Waiting messages from the client")
        val numOfMessages = 100

        if (!socket.isValid) {
            throw Exception("Invalid socket")
        }

        socket.setSockFlag(SockOpt.RCVSYN, true)
        socket.bind(ip, port)
        socket.listen(10)

        val peer = socket.accept()
        val clientSocket = peer.first

        repeat(numOfMessages) {
            val pair = clientSocket.recv(2048)
            val message = pair.second
            Log.i(TAG, "#$it >> Got msg of length ${message.size} << ${String(message)}")
        }

        // If session is close too early, last msg will not be receive by server
        clientSocket.close()
        Thread.sleep(1000)

        successMsg = "Received $numOfMessages messages (check logcat)"
    }

}