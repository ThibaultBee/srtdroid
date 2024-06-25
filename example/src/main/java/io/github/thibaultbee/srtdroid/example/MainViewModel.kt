package io.github.thibaultbee.srtdroid.example

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.github.thibaultbee.srtdroid.example.tests.RecvFile
import io.github.thibaultbee.srtdroid.example.tests.SendFile
import io.github.thibaultbee.srtdroid.example.tests.TestClient
import io.github.thibaultbee.srtdroid.example.tests.TestServer
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val configuration = Configuration(getApplication())
    private val sendFileName = "MyFileToSend"
    private val recvFileName = "RecvFile"

    val error = MutableLiveData<String>()
    val success = MutableLiveData<String>()

    val testClientCompletion = MutableLiveData<Boolean>()
    val testServerCompletion = MutableLiveData<Boolean>()
    val recvFileCompletion = MutableLiveData<Boolean>()
    val sendFileCompletion = MutableLiveData<Boolean>()

    private val testServer =
        TestServer()
    private val testClient =
        TestClient()

    private val recvFile =
        RecvFile(
            sendFileName,
            recvFileName,
            (getApplication() as Context).filesDir
        )
    private val sendFile =
        SendFile((getApplication() as Context).filesDir)


    private var testClientJob: Job? = null
    private var testServerJob: Job? = null
    private var recvFileJob: Job? = null
    private var sendFileJob: Job? = null

    /**
     * Sends multiple messages to a SRT server.
     *
     * Same as SRT examples/test-c-client.c
     */
    fun launchTestClient() {
        if (testClientJob?.isActive == true) {
            Log.w(TAG, "Test client job is already running")
            return
        }
        testClientJob = viewModelScope.launch {
            try {
                testClient.run(configuration.clientIP, configuration.clientPort)
                success.postValue("Client success!")
            } catch (e: Exception) {
                Log.e(TAG, "Client error: ${e.message}", e)
                error.postValue(e.message)
            } finally {
                testClientCompletion.postValue(true)
            }
        }
    }

    fun cancelTestClient() {
        Log.i(TAG, "Canceling test client job")
        testClientJob?.cancelChildren()
        testClientJob = null
    }

    /**
     * Receives multiple messages from a SRT client.
     *
     * Same as SRT examples/test-c-server.c
     */
    fun launchTestServer() {
        if (testServerJob?.isActive == true) {
            Log.w(TAG, "Test server job is already running")
            return
        }
        testServerJob = viewModelScope.launch {
            try {
                testServer.run(configuration.serverIP, configuration.serverPort)
                success.postValue("Server success!")
            } catch (e: Exception) {
                Log.e(TAG, "Server error: ${e.message}", e)
                error.postValue(e.message)
            } finally {
                testServerCompletion.postValue(true)
            }
        }
    }

    fun cancelTestServer() {
        Log.i(TAG, "Canceling test server job")
        testServerJob?.cancel()
        testServerJob = null
    }

    /**
     * Requests a file from a SRT server and receives it.
     *
     * Same as SRT examples/recvfile.cpp
     */
    fun launchRecvFile() {
        if (recvFileJob?.isActive == true) {
            Log.w(TAG, "Recv file job is already running")
            return
        }
        recvFileJob = viewModelScope.launch {
            try {
                recvFile.run(configuration.clientIP, configuration.clientPort)
                success.postValue("RecvFile success!")
            } catch (e: Exception) {
                Log.e(TAG, "RecvFile error: ${e.message}", e)
                error.postValue(e.message)
            } finally {
                recvFileCompletion.postValue(true)
            }
        }
    }

    fun cancelRecvFile() {
        Log.i(TAG, "Canceling recv file job")
        recvFileJob?.cancel()
        recvFileJob = null
    }

    /**
     * Sends a requested file to a SRT client.
     * If requested file does not exist, it is created.
     *
     * Same as SRT examples/sendfile.cpp
     */
    fun launchSendFile() {
        if (sendFileJob?.isActive == true) {
            Log.w(TAG, "Send file job is already running")
            return
        }
        sendFileJob = viewModelScope.launch {
            try {
                sendFile.run(configuration.serverIP, configuration.serverPort)
                success.postValue("SendFile success!")
            } catch (e: Exception) {
                Log.e(TAG, "SendFile error: ${e.message}", e)
                error.postValue(e.message)
            } finally {
                sendFileCompletion.postValue(true)
            }
        }
    }

    fun cancelSendFile() {
        Log.i(TAG, "Canceling send file job")
        sendFileJob?.cancel()
        sendFileJob = null
    }

    companion object {
        private val TAG = MainViewModel::class.simpleName
    }
}