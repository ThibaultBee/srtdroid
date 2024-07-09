package io.github.thibaultbee.srtdroid.example.tests

import android.util.Log
import com.google.common.primitives.Ints
import com.google.common.primitives.Longs
import io.github.thibaultbee.srtdroid.core.enums.SockOpt
import io.github.thibaultbee.srtdroid.core.enums.Transtype
import io.github.thibaultbee.srtdroid.example.Utils
import io.github.thibaultbee.srtdroid.ktx.CoroutineSrtSocket
import io.github.thibaultbee.srtdroid.ktx.extensions.connect
import io.github.thibaultbee.srtdroid.ktx.extensions.recvFile
import io.github.thibaultbee.srtdroid.ktx.extensions.send
import kotlinx.coroutines.delay
import java.io.File

class RecvFile(
    private val sendFileName: String,
    private val recvFileName: String,
    private val recvFileDir: File,
) : Test {
    companion object {
        private val TAG = RecvFile::class.simpleName
    }

    override val name: String = this::class.simpleName!!

    override suspend fun run(ip: String, port: Int) {
        Log.i(TAG, "Will get file $sendFileName from server")
        val socket = CoroutineSrtSocket()

        try {
            socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
            socket.connect(ip, port)
            Log.i(
                TAG,
                "Is connected: ${socket.isConnected}"
            )

            // Request server file
            socket.send(Ints.toByteArray(sendFileName.length).reversedArray())

            socket.send(sendFileName)

            val array = socket.recv(Longs.BYTES)
            val fileSize = Longs.fromByteArray(array.reversedArray())

            // Where file will be written
            val recvFile = File(recvFileDir, recvFileName)
            Log.i(TAG, "Receiving file ${recvFile.path}")
            if (fileSize != socket.recvFile(recvFile, 0, fileSize)) {
                throw Exception("Failed to recv file: ${Utils.getErrorMessage()}")
            }

            // If session is close too early, last msg will not be receive by server
            delay(1000)
            Log.i(TAG, "Received file $recvFile")
        } catch (e: Exception) {
            throw e
        } finally {
            socket.close()
        }
    }
}