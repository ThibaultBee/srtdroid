package io.github.thibaultbee.srtdroid.examples

import android.content.Context
import androidx.preference.PreferenceManager
import io.github.thibaultbee.srtdroid.examples.R

class Configuration(context: Context) {
    private val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    private val resources = context.resources

    val serverIP: String
        get() = sharedPref.getString(resources.getString(R.string.ip_server_key), "")!!

    val serverPort: Int
        get() = sharedPref.getString(resources.getString(R.string.port_server_key), "9998")!!.toInt()


    val clientIP: String
        get() = sharedPref.getString(resources.getString(R.string.ip_client_key), "")!!

    val clientPort: Int
        get() = sharedPref.getString(resources.getString(R.string.port_client_key), "9998")!!.toInt()
}