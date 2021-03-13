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
package com.github.thibaultbee.srtdroid.chat.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.thibaultbee.srtdroid.Srt
import com.github.thibaultbee.srtdroid.chat.databinding.ActivityMainBinding
import com.github.thibaultbee.srtdroid.chat.settings.SettingsActivity
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable


class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.qualifiedName
    private val activityDisposables = CompositeDisposable()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Srt.startUp()

        val connectionDetails: SharedPreferences =
            this.getSharedPreferences("connectionDetails", Context.MODE_PRIVATE)
        if (connectionDetails.contains("isServer")) {
            goToSettingsActivity(connectionDetails.getBoolean("isServer", false))
        }

        bindProperties()
    }

    private fun bindProperties() {
        binding.serverButton.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                goToSettingsActivity(true)
            }
            .let(activityDisposables::add)

        binding.clientButton.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                goToSettingsActivity(false)
            }
            .let(activityDisposables::add)
    }

    private fun goToSettingsActivity(isServer: Boolean) {
        val connectionDetails: SharedPreferences =
            this.getSharedPreferences("connectionDetails", Context.MODE_PRIVATE)
        connectionDetails.edit()
            .putBoolean("isServer", isServer)
            .apply()
        val intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra("isServer", isServer)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Srt.cleanUp()
        activityDisposables.clear()
    }

}
