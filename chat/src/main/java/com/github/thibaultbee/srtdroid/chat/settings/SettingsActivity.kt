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
package com.github.thibaultbee.srtdroid.chat.settings

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.github.thibaultbee.srtdroid.chat.R
import com.github.thibaultbee.srtdroid.chat.chat.ChatActivity
import com.github.thibaultbee.srtdroid.chat.databinding.ActivitySettingsBinding
import com.github.thibaultbee.srtdroid.chat.singleton.SocketHandler
import com.github.thibaultbee.srtdroid.chat.utils.DialogUtils
import com.jakewharton.rxbinding4.view.clicks
import com.tbruyelle.rxpermissions3.RxPermissions
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable

class SettingsActivity : AppCompatActivity() {
    private val TAG = SettingsActivity::class.qualifiedName
    private val activityDisposables = CompositeDisposable()
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction().replace(
            R.id.preferenceLayout,
            SettingsFragment()
        )
            .commit()

        bindProperties()
    }

    private fun bindProperties() {
        val isServer = intent.getBooleanExtra("isServer", false)
        Observable.just(isServer)
            .subscribe {
                if (it) {
                    binding.connectButton.text = getString(R.string.listen)
                } else {
                    binding.connectButton.text = getString(R.string.connect)
                }
            }.let(activityDisposables::add)

        val rxPermissions = RxPermissions(this)
        val connectButtonObservable = binding.connectButton.clicks().share()
        connectButtonObservable
            .compose(rxPermissions.ensure(Manifest.permission.INTERNET))
            .filter { granted -> !granted }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                DialogUtils.showAlert(
                    this,
                    "Permissions",
                    "Internet permission is not granted"
                )
            }
            .let(activityDisposables::add)

        connectButtonObservable
            .compose(rxPermissions.ensure(Manifest.permission.INTERNET))
            .filter { granted -> granted == true }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                try {
                    if (isServer) {
                        createServerFromPreferences()
                    } else {
                        createClientFromPreferences()
                    }
                    goToChatActivity()
                } catch (e: Exception) {
                    DialogUtils.showAlert(this, getString(R.string.connection_error), e.message)
                }
            }
            .let(activityDisposables::add)
    }

    private fun goToChatActivity() {
        startActivity(Intent(this, ChatActivity::class.java))
    }

    private fun createServerFromPreferences() {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(this)
        val ip = sharedPreferences.getString("ip_key", "")!!
        val port = Integer.valueOf(sharedPreferences.getString("port_key", "0")!!)

        SocketHandler.createServer(ip, port)
    }

    private fun createClientFromPreferences() {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(this)
        val ip = sharedPreferences.getString("ip_key", "")!!
        val port = Integer.valueOf(sharedPreferences.getString("port_key", "0")!!)

        SocketHandler.createClient(ip, port)
    }

    override fun onDestroy() {
        super.onDestroy()
        activityDisposables.clear()
    }
}
