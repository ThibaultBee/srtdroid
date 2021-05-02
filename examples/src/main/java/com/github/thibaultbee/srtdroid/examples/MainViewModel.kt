package com.github.thibaultbee.srtdroid.examples

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.github.thibaultbee.srtdroid.enums.SockOpt
import com.github.thibaultbee.srtdroid.enums.Transtype
import com.github.thibaultbee.srtdroid.models.Socket
import com.google.common.primitives.Ints
import com.google.common.primitives.Longs
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = this::class.qualifiedName

    private val configuration = Configuration(getApplication())

    private val sendFileName = "MyFileToSend"
    private val recvFileName = "RecvFile"


    val error = MutableLiveData<String>()
    val success = MutableLiveData<String>()

    /**
     * Sends multiple messages to a SRT server.
     * Same as SRT examples/test-c-client.c
     */
    fun launchTestClient() {
        try {
            Log.i(TAG, "Will send messages to the server")
            val numOfMessages = 100

            val socket = Socket()
            if (!socket.isValid) {
                throw Exception("Invalid socket")
            }

            socket.setSockFlag(SockOpt.SENDER, 1)
            socket.connect(configuration.clientIP, configuration.clientPort)

            repeat(numOfMessages) {
                socket.send("This message should be sent to the other side")
            }
            // If session is close too early, last msg will not be receive by server
            Thread.sleep(1000)
            socket.close()

            success.postValue("Sent $numOfMessages messages")
        } catch (e: Exception) {
            error.postValue(e.message)
        }
    }


    /**
     * Receives multiple messages from a SRT client.
     *
     * Same as SRT examples/test-c-server.c
     */
    fun launchTestServer() {
        try {
            Log.i(TAG, "Waiting messages from the client")
            val numOfMessages = 100

            val socket = Socket()
            if (!socket.isValid) {
                throw Exception("Invalid socket")
            }

            socket.setSockFlag(SockOpt.RCVSYN, true)
            socket.bind(configuration.serverIP, configuration.serverPort)
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
            socket.close()

            success.postValue("Received $numOfMessages messages (check logcat)")
        } catch (e: Exception) {
            error.postValue(e.message)
        }

    }

    /**
     * Requests a file from a SRT server and receives it.
     *
     * Same as SRT examples/recvfile.cpp
     *
     * @param recvFileDir directory where to find received file
     */
    fun launchRecvFile(recvFileDir: File) {
        try {
            Log.i(TAG, "Will get file $sendFileName from server")

            val socket = Socket()
            if (!socket.isValid) {
                throw Exception("Invalid socket")
            }

            socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
            socket.connect(configuration.clientIP, configuration.clientPort)

            // Request server file
            socket.send(Ints.toByteArray(sendFileName.length).reversedArray())

            socket.send(sendFileName)

            val fileSize = Longs.fromByteArray(socket.recv(Longs.BYTES).second.reversedArray())

            // Where file will be written
            val recvFile = File(recvFileDir, recvFileName)
            if (fileSize != socket.recvFile(recvFile, 0, fileSize)) {
                throw Exception("Failed to recv file from ${configuration.clientIP}:${configuration.clientPort}: ${Utils.getErrorMessage()}")
            }

            // If session is close too early, last msg will not be receive by server
            Thread.sleep(1000)
            socket.close()

            success.postValue("Recv file is in $recvFile")
        } catch (e: Exception) {
            error.postValue(e.message)
        }
    }


    /**
     * Sends a requested file to a SRT client.
     * If requested file does not exist, it is created.
     *
     * Same as SRT examples/sendfile.cpp
     *
     * @param fileSendDir directory where to find the file to send
     */
    fun launchSendFile(fileSendDir: File) {
        try {
            Log.i(TAG, "Will send requested file")

            val socket = Socket()
            if (!socket.isValid) {
                throw Exception("Invalid socket")
            }

            socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
            socket.bind(configuration.serverIP, configuration.serverPort)
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
            socket.close()

            success.postValue("Sent file ${file.path}")
        } catch (e: Exception) {
            error.postValue(e.message)
        }
    }
}