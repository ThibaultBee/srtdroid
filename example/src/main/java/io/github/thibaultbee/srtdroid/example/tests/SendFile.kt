package io.github.thibaultbee.srtdroid.example.tests

import android.util.Log
import com.google.common.primitives.Ints
import com.google.common.primitives.Longs
import io.github.thibaultbee.srtdroid.enums.SockOpt
import io.github.thibaultbee.srtdroid.enums.Transtype
import io.github.thibaultbee.srtdroid.ktx.CoroutineSocket
import io.github.thibaultbee.srtdroid.ktx.extensions.bind
import io.github.thibaultbee.srtdroid.ktx.extensions.sendFile
import kotlinx.coroutines.delay
import java.io.File

class SendFile(
    private val fileSendDir: File,
) : Test {
    companion object {
        private val TAG = SendFile::class.simpleName
    }

    override val name: String = this::class.simpleName!!

    override suspend fun run(ip: String, port: Int) {
        Log.i(TAG, "Will send requested file")
        val socket = CoroutineSocket()

        try {
            socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)

            Log.i(TAG, "Will bind on $ip:$port")
            socket.bind(ip, port)
            socket.listen(10)

            Log.i(TAG, "Waiting for incoming socket on $ip:$port")
            val peer = socket.accept()
            val clientSocket = peer.first

            Log.i(TAG, "Get an incoming connection")
            // Get file name length
            var array = clientSocket.recv(Ints.BYTES)
            val fileNameLength = Ints.fromByteArray(array.reversedArray())
            when {
                array.isNotEmpty() -> Log.i(TAG, "File name is $fileNameLength char long")
            }

            // Get file name
            array = clientSocket.recv(fileNameLength)
            val fileName = String(array)
            Log.i(TAG, "File name is $fileName")

            val file = File("$fileSendDir/$fileName")
            if (!file.exists()) {
                Log.w(TAG, "File ${file.path} does not exist. Try to create it")
                file.writeText("myServerFileContent. Hello Client! This is server.")
            }
            if (!file.exists()) {
                throw Exception("Failed to get file ${file.path}")
            }

            // Send file size
            clientSocket.send(Longs.toByteArray(file.length()).reversedArray())

            // Send file
            Log.i(TAG, "Sending file ${file.path}")
            clientSocket.sendFile(file)

            // If session is close too early, last msg will not be receive by server
            clientSocket.close()
            delay(1000)

            Log.i(TAG, "Sent file ${file.path}")
        } catch (e: Exception) {
            throw e
        } finally {
            socket.close()
        }
    }
}