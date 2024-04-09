package io.github.thibaultbee.srtdroid.example.tests

import io.github.thibaultbee.srtdroid.models.Socket
import java.util.concurrent.ExecutorService

abstract class Test(
    private val executor: ExecutorService,
    protected val onSuccess: (String) -> Unit,
    protected val onError: (String) -> Unit
) {
    protected abstract val testName: String
    private var socket: Socket? = null
    protected var successMsg: String = ""

    fun launch(ip: String, port: Int) {
        executor.execute {
            try {
                socket = Socket()
                launchImpl(ip, port, socket!!)
                socket?.close()
                onSuccess("$testName: $successMsg")
            } catch (e: Exception) {
                onError("$testName: ${e.message}")
            }
        }
    }

    abstract fun launchImpl(ip: String, port: Int, socket: Socket)

    fun cancel() {
        socket?.close()
    }
}
