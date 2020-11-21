package com.github.thibaultbee.srtdroid.chat.utils

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.github.thibaultbee.srtdroid.chat.R

class DialogUtils {
    companion object {
        fun showAlert(context: Context, title: String, message: String?) {
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.dismiss) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
                .show()
        }

        fun showAlertCloseActivity(context: Context, title: String, message: String?) {
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.dismiss) { _: DialogInterface, _: Int ->
                    (context as Activity).finish()
                }
                .show()
        }
    }
}