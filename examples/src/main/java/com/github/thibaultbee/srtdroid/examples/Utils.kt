package com.github.thibaultbee.srtdroid.examples

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import com.github.thibaultbee.srtdroid.models.Error
import java.io.File
import java.io.FileOutputStream


class Utils {
    companion object {
        fun getServerIpFromPreference(context: Context): String {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getString("ip_server_key", "") !!
        }

        fun getServerPortFromPreference(context: Context): Int {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return Integer.valueOf(sharedPreferences.getString("port_server_key", "0") !!)
        }

        fun getClientIpFromPreference(context: Context): String {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getString("ip_client_key", "") !!
        }

        fun getClientPortFromPreference(context: Context): Int {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return Integer.valueOf(sharedPreferences.getString("port_client_key", "0") !!)
        }

        fun showAlert(context: Context, title: String, message: String = "") {
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.dismiss) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
                .show()
        }

        fun getErrorMessage(): String {
            val message = Error.lastErrorMessage
            Error.clearLastError()
            return message
        }

        fun writeFile(file: File, text: String) {
            val fos = FileOutputStream(file)
            fos.write(text.toByteArray())
            fos.close()
        }
    }
}
