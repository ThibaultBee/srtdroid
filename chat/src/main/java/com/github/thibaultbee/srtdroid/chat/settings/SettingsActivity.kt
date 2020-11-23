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
import com.jakewharton.rxbinding3.view.clicks
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

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
            .filter { granted -> granted }
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
