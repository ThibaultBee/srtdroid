package io.github.thibaultbee.srtdroid.example.tests

import android.util.Log
import io.github.thibaultbee.srtdroid.enums.SockOpt
import io.github.thibaultbee.srtdroid.enums.Transtype
import io.github.thibaultbee.srtdroid.example.Utils
import io.github.thibaultbee.srtdroid.models.Socket
import com.google.common.primitives.Ints
import com.google.common.primitives.Longs
import java.io.File
import java.util.concurrent.ExecutorService

class SendFile(
    private val fileSendDir: File,
    executorService: ExecutorService,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) : Test(executorService, onSuccess, onError) {
    companion object {
        private val TAG = SendFile::class.simpleName
    }

    override val testName: String = this::class.simpleName!!

    override fun launchImpl(ip: String, port: Int, socket: Socket) {
        Log.i(TAG, "Will send requested file")

        if (!socket.isValid) {
            throw Exception("Invalid socket")
        }

        socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
        socket.bind(ip, port)
        socket.listen(10)

        val peer = socket.accept()
        val clientSocket = peer.first

        // Get file name length
        var pair = clientSocket.recv(Ints.BYTES)
        val res = pair.first
        val fileNameLength = Ints.fromByteArray(pair.second.reversedArray())
        when {
            res > 0 -> Log.i(TAG, "File name is $fileNameLength char long")
        }

        // Get file name
        pair = clientSocket.recv(fileNameLength)
        val fileName = String(pair.second)
        Log.i(TAG, "File name is $fileName")

        val file = File("$fileSendDir/$fileName")
        if (!file.exists()) {
            Log.w(TAG, "File ${file.path} does not exist. Try to create it")
            Utils.writeFile(file, "myServerFileContent. Hello Client! This is server.")
        }
        if (!file.exists()) {
            throw Exception("Failed to get file ${file.path}")
        }

        // Send file size
        clientSocket.send(Longs.toByteArray(file.length()).reversedArray())

        // Send file
        clientSocket.sendFile(file)

        // If session is close too early, last msg will not be receive by server
        clientSocket.close()
        Thread.sleep(1000)

        successMsg = "Sent file ${file.path}"
    }

}