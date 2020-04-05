package com.github.thibaultbee.srtwrapper.examples

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import butterknife.BindView
import butterknife.ButterKnife
import com.google.common.primitives.Ints
import com.google.common.primitives.Longs
import com.github.thibaultbee.srtwrapper.Srt
import com.github.thibaultbee.srtwrapper.enums.SockOpt
import com.github.thibaultbee.srtwrapper.enums.Transtype
import com.github.thibaultbee.srtwrapper.models.Socket
import com.jakewharton.rxbinding3.view.clicks
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.io.File
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.qualifiedName
    private val activityDisposables = CompositeDisposable()

    @BindView(R.id.testClientButton)
    lateinit var testClientButton: Button
    @BindView(R.id.testServerButton)
    lateinit var testServerButton: Button
    @BindView(R.id.recvFileButton)
    lateinit var recvFileButton: Button
    @BindView(R.id.sendFileButton)
    lateinit var sendFile: Button

    private lateinit var srt: Srt

    private val serverFileName = "MyFileToSend"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.preferenceClientLayout, MainClientSettingsFragment())
            .replace(R.id.preferenceServerLayout, MainServerSettingsFragment())
            .commit()

        srt = Srt()
        srt.startUp()

        bindProperties()
    }

    private fun bindProperties() {
        val rxPermissions = RxPermissions(this)
        rxPermissions.request(Manifest.permission.INTERNET)

        testClientButton.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                try {
                    launchTestClient(
                        Utils.getClientIpFromPreference(this),
                        Utils.getClientPortFromPreference(this)
                    )
                    Utils.showAlert(this, "Success", "Noice!")
                } catch (e: Exception) {
                    Utils.showAlert(this, "Error", e.message ?: "")
                }
            }
            .let(activityDisposables::add)

        testServerButton.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                try {
                    launchTestServer(
                        Utils.getServerIpFromPreference(this),
                        Utils.getServerPortFromPreference(this)
                    )
                    Utils.showAlert(this, "Success", "Noice! (check logcat)")
                } catch (e: Exception) {
                    Utils.showAlert(this, "Error", e.message ?: "")
                }
            }
            .let(activityDisposables::add)

        recvFileButton.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(rxPermissions.ensure(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            .subscribe {
                try {
                    Log.i(TAG, "Will get file $serverFileName from server")
                    val file = launchRecvFile(
                        Utils.getClientIpFromPreference(this),
                        Utils.getClientPortFromPreference(this),
                        serverFileName)
                    Utils.showAlert(this, "Success", "Check out ${file.path}")
                } catch (e: Exception) {
                    Utils.showAlert(this, "Failed to recv file", e.message ?: "")
                }
            }
            .let(activityDisposables::add)

        sendFile.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(rxPermissions.ensure(Manifest.permission.READ_EXTERNAL_STORAGE))
            .compose(rxPermissions.ensure(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            .subscribe {
                try {
                    val stats = launchSendFile(
                        Utils.getServerIpFromPreference(this),
                        Utils.getServerPortFromPreference(this)
                    )
                    Utils.showAlert(this, "Success", "Noice!\nSpeed = ${stats.mbpsRate}\nLoss = ${stats.pktLossTotal} pkt ( ${stats.lossPercent} %)")
                } catch (e: Exception) {
                    Utils.showAlert(this, "Error", e.message ?: "")
                }
            }
            .let(activityDisposables::add)
    }


    // To be tested with examples/test-c-server.c
    private fun launchTestClient(ip: String, port: Int) {
        val socket = Socket()
        if (!socket.isValid()) {
            throw Exception("Invalid socket")
        }

        if (socket.setSockFlag(SockOpt.SENDER, 1) != 0) {
            socket.close()
            throw Exception("Failed to set sock flag to Sender: ${Utils.getErrorMessage()}")
        }

        if (socket.connect(ip, port) != 0) {
            socket.close()
            throw Exception("Failed to connect to $ip:$port: ${Utils.getErrorMessage()}")
        }

        repeat(100) {
            if (socket.send("This message should be sent to the other side") <= 0) {
                socket.close()
                throw Exception("Failed to send message $it to $ip:$port: ${Utils.getErrorMessage()}")
            }
        }

        Thread.sleep(1000) // If session is close too early, last msg will not be receive by server
        socket.close()
    }

    // To be tested with examples/test-c-client.c
    private fun launchTestServer(ip: String, port: Int) {
        val socket = Socket()
        if (!socket.isValid()) {
            throw Exception("Invalid socket")
        }

        if (socket.setSockFlag(SockOpt.RCVSYN, true) != 0) {
            socket.close()
            throw Exception("Failed to set sock flag to RCVSYN: ${Utils.getErrorMessage()}")
        }

        if (socket.bind(ip, port) != 0) {
            socket.close()
            throw Exception("Failed to bind to $ip:$port: ${Utils.getErrorMessage()}")
        }

        if (socket.listen(2) != 0) {
            socket.close()
            throw Exception("Failed to listen: ${Utils.getErrorMessage()}")
        }

        val peer = socket.accept()
        val clientSocket = peer.first
        if (!clientSocket.isValid()) {
            socket.close()
            throw Exception("Failed to accept: ${Utils.getErrorMessage()}")
        }

        repeat(100) {
            val pair = clientSocket.recv(2048)
            val res = pair.first
            val message = pair.second
            when {
                res > 0 -> Log.i(TAG, "Got msg of len ${message.size} << ${String(message)}")
                res == 0 -> {
                    socket.close()
                    throw Exception("Connection has been closed")
                }
                else -> {
                    socket.close()
                    throw Exception("Failed to recv: ${Utils.getErrorMessage()}")
                }
            }

        }

        socket.close()
    }

    // To be tested with examples/sendfile.c
    private fun launchRecvFile(ip: String, port: Int, serverFileName: String): File {
        val socket = Socket()
        if (!socket.isValid()) {
            throw Exception("Invalid socket")
        }

        if (socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE) != 0) {
            socket.close()
            throw Exception("Failed to set sock flag to Sender: ${Utils.getErrorMessage()}")
        }

        if (socket.connect(ip, port) != 0) {
            socket.close()
            throw Exception("Failed to connect to $ip:$port: ${Utils.getErrorMessage()}")
        }

        // Request server file
        if (socket.sendMsg(Ints.toByteArray(serverFileName.length).reversedArray()) <= 0) {
                socket.close()
                throw Exception("Failed to send file length to $ip:$port: ${Utils.getErrorMessage()}")
        }

        if (socket.sendMsg(serverFileName) <= 0) {
            socket.close()
            throw Exception("Failed to send file name to $ip:$port: ${Utils.getErrorMessage()}")
        }

        val fileSize = Longs.fromByteArray(socket.recv(Longs.BYTES).second.reversedArray())
        if (fileSize <= 0) {
            socket.close()
            throw Exception("Failed to get file size for $serverFileName from $ip:$port: ${Utils.getErrorMessage()}")
        }

        // Where file will be written
        val myFile = File(this.filesDir,"RecvFile")
        if (fileSize != socket.recvFile(myFile, 0, fileSize)) {
            socket.close()
            throw Exception("Failed to recv file from $ip:$port: ${Utils.getErrorMessage()}")
        }

        socket.close()

        return myFile
    }

    // To be tested with examples/recvfile.c
    private fun launchSendFile(ip: String, port: Int): SimpleStats {
        val socket = Socket()
        if (!socket.isValid()) {
            throw Exception("Invalid socket")
        }

        if (socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE) != 0) {
            socket.close()
            throw Exception("Failed to set sock flag to RCVSYN: ${Utils.getErrorMessage()}")
        }

        if (socket.bind(ip, port) != 0) {
            socket.close()
            throw Exception("Failed to bind to $ip:$port: ${Utils.getErrorMessage()}")
        }

        if (socket.listen(2) != 0) {
            socket.close()
            throw Exception("Failed to listen: ${Utils.getErrorMessage()}")
        }

        val peer = socket.accept()
        val clientSocket = peer.first
        if (!clientSocket.isValid()) {
            socket.close()
            throw Exception("Failed to accept: ${Utils.getErrorMessage()}")
        }

        // Get file name length
        var pair = clientSocket.recv(Ints.BYTES)
        var res = pair.first
        val fileNameLength = Ints.fromByteArray(pair.second.reversedArray())
        when {
            res > 0 -> Log.i(TAG, "File name is $fileNameLength char long")
            res == 0 -> {
                socket.close()
                throw Exception("Connection has been closed")
            }
            else -> {
                socket.close()
                throw Exception("Failed to recv: ${Utils.getErrorMessage()}")
            }
        }

        // Get file name
        pair = clientSocket.recv(fileNameLength)
        res = pair.first
        val fileName = String(pair.second)
        when {
            res > 0 -> Log.i(TAG, "File name is $fileName")
            res == 0 -> {
                socket.close()
                throw Exception("Connection has been closed")
            }
            else -> {
                socket.close()
                throw Exception("Failed to recv: ${Utils.getErrorMessage()}")
            }
        }

        val file = File("${this.filesDir}/$fileName")
        if (!file.exists()) {
            Log.e(TAG, "File ${file.path} does not exist. Try to create it")
            Utils.writeFile(file, "myServerFileContent. Hello Client! This is server.")
        }
        if (!file.exists()) {
            socket.close()
            throw Exception("Failed to get file ${file.path}")
        }

        // Send file size
        if (clientSocket.sendMsg(Longs.toByteArray(file.length()).reversedArray()) <= 0) {
            socket.close()
            throw Exception("Failed to send file ${file.name} length to $ip:$port: ${Utils.getErrorMessage()}")
        }

        // Send file
        if (clientSocket.sendFile(file) <= 0) {
            socket.close()
            throw Exception("Failed to send file ${file.name} to $ip:$port: ${Utils.getErrorMessage()}")
        }

        val stats = clientSocket.bstats(true)
        val simpleStats = SimpleStats(stats.mbpsSendRate, stats.pktSndLossTotal, 100 * stats.pktSndLossTotal / stats.pktSent.toInt())

        socket.close()

        return simpleStats
    }

    override fun onDestroy() {
        super.onDestroy()
        srt.cleanUp()
        activityDisposables.clear()
    }
}
