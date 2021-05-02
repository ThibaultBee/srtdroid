/*
 * Copyright (C) 2021 Thibault B.
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
import androidx.lifecycle.ViewModelProvider
import com.github.thibaultbee.srtdroid.Srt
import com.github.thibaultbee.srtdroid.examples.databinding.ActivityMainBinding
import com.jakewharton.rxbinding4.view.clicks
import com.tbruyelle.rxpermissions3.RxPermissions
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val TAG = this::class.qualifiedName
    private val activityDisposables = CompositeDisposable()

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.preferenceClientLayout, MainClientSettingsFragment())
            .replace(R.id.preferenceServerLayout, MainServerSettingsFragment())
            .commit()

        bindProperties()
    }

    private fun bindProperties() {
        val rxPermissions = RxPermissions(this)

        binding.testClientButton.clicks()
            .throttleFirst(3, TimeUnit.SECONDS) // 3s = SRT default SRTO_CONNTIMEO
            .observeOn(Schedulers.io()) // Do not execute SRT networking operation on main thread
            .switchMap {
                Observable.just(viewModel.launchTestClient())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .let(activityDisposables::add)

        binding.testServerButton.clicks()
            .observeOn(Schedulers.io()) // Do not execute SRT networking operation on main thread
            .switchMap {
                Observable.just(viewModel.launchTestServer())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .let(activityDisposables::add)

        binding.recvFileButton.clicks()
            .throttleFirst(3, TimeUnit.SECONDS) // 3s = SRT default SRTO_CONNTIMEO
            .observeOn(AndroidSchedulers.mainThread())
            .compose(
                rxPermissions.ensure(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
            .observeOn(Schedulers.io()) // Do not execute SRT networking operation on main thread
            .switchMap { granted ->
                if (granted) {
                    Observable.just(viewModel.launchRecvFile(this.filesDir))
                } else {
                    Utils.showAlertDialog(
                        this,
                        getString(R.string.Permission),
                        "Missing permission: WRITE_EXTERNAL_STORAGE"
                    )
                    null
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .let(activityDisposables::add)

        binding.sendFileButton.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(
                rxPermissions.ensureEachCombined(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
            .observeOn(Schedulers.io()) // Do not execute SRT networking operation on main thread
            .switchMap { permission ->
                if (permission.granted) {
                    Observable.just(viewModel.launchSendFile(this.filesDir))
                } else {
                    Utils.showAlertDialog(
                        this,
                        getString(R.string.Permission),
                        "Missing permissions: WRITE_EXTERNAL_STORAGE or READ_EXTERNAL_STORAGE"
                    )
                    null
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .let(activityDisposables::add)

        viewModel.error.observe(this) {
            Utils.showAlertDialog(this, getString(R.string.Error), it)
        }

        viewModel.success.observe(this) {
            Utils.showAlertDialog(this, getString(R.string.Success), "Noice! $it")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        Srt.cleanUp()
        activityDisposables.clear()
    }
}
