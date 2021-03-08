/*
 * Copyright (C) 2021 Thibault Beyou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.thibaultbee.srtdroid.examples

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.thibaultbee.srtdroid.Srt
import com.github.thibaultbee.srtdroid.enums.SockOpt
import com.github.thibaultbee.srtdroid.enums.Transtype
import com.github.thibaultbee.srtdroid.examples.databinding.ActivityMainBinding
import com.github.thibaultbee.srtdroid.models.Socket
import com.google.common.primitives.Ints
import com.google.common.primitives.Longs
import com.jakewharton.rxbinding3.view.clicks
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.io.File

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.qualifiedName
    private val activityDisposables = CompositeDisposable()
    private lateinit var binding: ActivityMainBinding

    private lateinit var srt: Srt

    private val serverFileName = "MyFileToSend"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        binding.testClientButton.clicks()
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

        binding.testServerButton.clicks()
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

        binding.recvFileButton.clicks()
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

        binding.sendFileButton.clicks()
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
        if (!socket.isValid) {
            throw Exception("Invalid socket")
        }

        try {
            socket.setSockFlag(SockOpt.SENDER, 1)
            socket.connect(ip, port)

            repeat(100) {
                socket.send("This message should be sent to the other side")
            }
        } finally {
            Thread.sleep(1000) // If session is close too early, last msg will not be receive by server
            socket.close()
        }
    }

    // To be tested with examples/test-c-client.c
    private fun launchTestServer(ip: String, port: Int) {
        val socket = Socket()
        if (!socket.isValid) {
            throw Exception("Invalid socket")
        }

        try {
            socket.setSockFlag(SockOpt.RCVSYN, true)
            socket.bind(ip, port)
            socket.listen(2)

            val peer = socket.accept()
            val clientSocket = peer.first

            repeat(100) {
                val pair = clientSocket.recv(2048)
                val message = pair.second
                Log.i(TAG, "#$it >> Got msg of length ${message.size} << ${String(message)}")
            }
        } finally {
            Thread.sleep(1000) // If session is close too early, last msg will not be receive by server
            socket.close()
        }
    }

    // To be tested with examples/sendfile.c
    private fun launchRecvFile(ip: String, port: Int, serverFileName: String): File {
        val socket = Socket()
        if (!socket.isValid) {
            throw Exception("Invalid socket")
        }

        return try {
            socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
            socket.connect(ip, port)

            // Request server file
            socket.send(Ints.toByteArray(serverFileName.length).reversedArray())

            socket.send(serverFileName)

            val fileSize = Longs.fromByteArray(socket.recv(Longs.BYTES).second.reversedArray())

            // Where file will be written
            val myFile = File(this.filesDir, "RecvFile")
            if (fileSize != socket.recvFile(myFile, 0, fileSize)) {
                throw Exception("Failed to recv file from $ip:$port: ${Utils.getErrorMessage()}")
            }
            myFile
        } finally {
            Thread.sleep(1000) // If session is close too early, last msg will not be receive by server
            socket.close()
        }
    }

    // To be tested with examples/recvfile.c
    private fun launchSendFile(ip: String, port: Int): SimpleStats {
        val socket = Socket()
        if (!socket.isValid) {
            throw Exception("Invalid socket")
        }

        return try {
            socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.FILE)
            socket.bind(ip, port)
            socket.listen(2)

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

            val file = File("${this.filesDir}/$fileName")
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

            val stats = clientSocket.bstats(true)
            val simpleStats = SimpleStats(
                stats.mbpsSendRate,
                stats.pktSndLossTotal,
                100 * stats.pktSndLossTotal / stats.pktSent.toInt()
            )
            simpleStats
        } finally {
            socket.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        srt.cleanUp()
        activityDisposables.clear()
    }
}
