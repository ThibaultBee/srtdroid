package com.github.thibaultbee.srtdroid.examples.tests

import android.util.Log
import com.github.thibaultbee.srtdroid.enums.SockOpt
import com.github.thibaultbee.srtdroid.enums.Transtype
import com.github.thibaultbee.srtdroid.examples.Utils
import com.github.thibaultbee.srtdroid.models.Socket
import com.google.common.primitives.Ints
import com.google.common.primitives.Longs
import java.io.File
import java.util.concurrent.ExecutorService

class RecvFile(
    private val sendFileName: String,
    private val recvFileName: String,
    private val recvFileDir: File,
    executorService: ExecutorService,
    onSuccess: (String) -> Unit,
    onError: (String?) -> Unit
) : Test(executorService, onSuccess, onError) {
    companion object {
        private val TAG = RecvFile::class.simpleName
    }

    override val testName: String = this::class.simpleName!!

    override fun launchImpl(ip: String, port: Int, socket: Socket) {
        Log.i(TAG, "Will get file $sendFileName from server")

        if (!socket.isValid) {
            throw Exception("Invalid socket")
        }

        socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
        socket.connect(ip, port)

        // Request server file
        socket.send(Ints.toByteArray(sendFileName.length).reversedArray())

        socket.send(sendFileName)

        val fileSize = Longs.fromByteArray(socket.recv(Longs.BYTES).second.reversedArray())

        // Where file will be written
        val recvFile = File(recvFileDir, recvFileName)
        if (fileSize != socket.recvFile(recvFile, 0, fileSize)) {
            throw Exception("Failed to recv file: ${Utils.getErrorMessage()}")
        }

        // If session is close too early, last msg will not be receive by server
        Thread.sleep(1000)

        successMsg = "Recv file is in $recvFile"
    }

}