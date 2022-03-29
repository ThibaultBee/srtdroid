package io.github.thibaultbee.srtdroid.examples

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import io.github.thibaultbee.srtdroid.examples.tests.RecvFile
import io.github.thibaultbee.srtdroid.examples.tests.SendFile
import io.github.thibaultbee.srtdroid.examples.tests.TestClient
import io.github.thibaultbee.srtdroid.examples.tests.TestServer
import java.util.concurrent.Executors

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val configuration = Configuration(getApplication())
    private val sendFileName = "MyFileToSend"
    private val recvFileName = "RecvFile"

    private val executor = Executors.newFixedThreadPool(4)

    val error = MutableLiveData<String>()
    val success = MutableLiveData<String>()

    val testClientCompletion = MutableLiveData<Boolean>()
    val testServerCompletion = MutableLiveData<Boolean>()
    val recvFileCompletion = MutableLiveData<Boolean>()
    val sendFileCompletion = MutableLiveData<Boolean>()

    private val testServer =
        TestServer(executor,
            { msg ->
                testServerCompletion.postValue(true)
                success.postValue(msg)
            },
            { msg ->
                testClientCompletion.postValue(true)
                error.postValue(msg)
            }
        )
    private val testClient =
        TestClient(
            executor,
            { msg ->
                testClientCompletion.postValue(true)
                success.postValue(msg)
            },
            { msg ->
                testClientCompletion.postValue(true)
                error.postValue(msg)
            }
        )

    private val recvFile =
        RecvFile(sendFileName,
            recvFileName,
            (getApplication() as Context).filesDir,
            executor,
            { msg ->
                recvFileCompletion.postValue(true)
                success.postValue(msg)
            },
            { msg ->
                recvFileCompletion.postValue(true)
                error.postValue(msg)
            }
        )

    private val sendFile =
        SendFile((getApplication() as Context).filesDir,
            executor,
            { msg ->
                sendFileCompletion.postValue(true)
                success.postValue(msg)
            },
            { msg ->
                sendFileCompletion.postValue(true)
                error.postValue(msg)
            }
        )

    /**
     * Sends multiple messages to a SRT server.
     *
     * Same as SRT examples/test-c-client.c
     */
    fun launchTestClient() {
        testClient.launch(configuration.clientIP, configuration.clientPort)
    }

    fun cancelTestClient() {
        testClient.cancel()
    }

    /**
     * Receives multiple messages from a SRT client.
     *
     * Same as SRT examples/test-c-server.c
     */
    fun launchTestServer() {
        testServer.launch(configuration.serverIP, configuration.serverPort)
    }

    fun cancelTestServer() {
        testServer.cancel()
    }

    /**
     * Requests a file from a SRT server and receives it.
     *
     * Same as SRT examples/recvfile.cpp
     */
    fun launchRecvFile() {
        recvFile.launch(configuration.clientIP, configuration.clientPort)
    }

    fun cancelRecvFile() {
        recvFile.cancel()
    }

    /**
     * Sends a requested file to a SRT client.
     * If requested file does not exist, it is created.
     *
     * Same as SRT examples/sendfile.cpp
     */
    fun launchSendFile() {
        sendFile.launch(configuration.serverIP, configuration.serverPort)
    }

    fun cancelSendFile() {
        sendFile.cancel()
    }
}