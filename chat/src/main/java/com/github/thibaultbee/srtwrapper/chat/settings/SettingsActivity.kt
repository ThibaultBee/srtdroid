package com.github.thibaultbee.srtwrapper.chat.settings

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import butterknife.BindView
import butterknife.ButterKnife
import com.github.thibaultbee.srtwrapper.chat.R
import com.github.thibaultbee.srtwrapper.chat.chat.ChatActivity
import com.github.thibaultbee.srtwrapper.chat.singleton.SocketHandler
import com.github.thibaultbee.srtwrapper.chat.utils.DialogUtils
import com.jakewharton.rxbinding3.view.clicks
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class SettingsActivity : AppCompatActivity() {
    private val TAG = SettingsActivity::class.qualifiedName
    private val activityDisposables = CompositeDisposable()

    @BindView(R.id.connectButton)
    lateinit var connectButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        ButterKnife.bind(this)
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
                    connectButton.text = getString(R.string.listen)
                } else {
                    connectButton.text = getString(R.string.connect)
                }
            }.let(activityDisposables::add)

        val rxPermissions = RxPermissions(this)
        val connectButtonObservable = connectButton.clicks().share()
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
