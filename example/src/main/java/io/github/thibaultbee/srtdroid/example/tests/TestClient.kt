package io.github.thibaultbee.srtdroid.example.tests

import android.util.Log
import io.github.thibaultbee.srtdroid.enums.SockOpt
import io.github.thibaultbee.srtdroid.models.Socket
import java.util.concurrent.ExecutorService

class TestClient(
    executorService: ExecutorService,
    onSuccess: (String) -> Unit,
    onError: (String?) -> Unit
) : Test(executorService, onSuccess, onError) {
    companion object {
        private val TAG = TestClient::class.simpleName
    }

    override val testName: String = this::class.simpleName!!

    override fun launchImpl(ip: String, port: Int, socket: Socket) {
        Log.i(TAG, "Will send messages to the server")
        val numOfMessages = 100

        if (!socket.isValid) {
            throw Exception("Invalid socket")
        }

        socket.setSockFlag(SockOpt.SENDER, 1)
        socket.connect(ip, port)

        repeat(numOfMessages) {
            socket.send("This message should be sent to the other side")
        }
        // If session is close too early, last msg will not be receive by server
        Thread.sleep(1000)

        successMsg = "Sent $numOfMessages messages"
    }

}