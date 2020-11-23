package com.github.thibaultbee.srtdroid.chat.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.thibaultbee.srtdroid.Srt
import com.github.thibaultbee.srtdroid.chat.databinding.ActivityMainBinding
import com.github.thibaultbee.srtdroid.chat.settings.SettingsActivity
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable


class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.qualifiedName
    private val activityDisposables = CompositeDisposable()
    private lateinit var binding: ActivityMainBinding

    private lateinit var srt: Srt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        srt = Srt()
        srt.startUp()

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
        srt.cleanUp()
        activityDisposables.clear()
    }

}
