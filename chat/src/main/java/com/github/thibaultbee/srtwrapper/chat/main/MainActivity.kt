package com.github.thibaultbee.srtwrapper.chat.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import butterknife.BindView
import butterknife.ButterKnife
import com.github.thibaultbee.srtwrapper.Srt
import com.github.thibaultbee.srtwrapper.chat.R
import com.github.thibaultbee.srtwrapper.chat.settings.SettingsActivity
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable


class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.qualifiedName
    private val activityDisposables = CompositeDisposable()

    @BindView(R.id.serverButton)
    lateinit var serverButton: Button
    @BindView(R.id.clientButton)
    lateinit var clientButton: Button

    private lateinit var srt: Srt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

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
        serverButton.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                goToSettingsActivity(true)
            }
            .let(activityDisposables::add)

        clientButton.clicks()
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
