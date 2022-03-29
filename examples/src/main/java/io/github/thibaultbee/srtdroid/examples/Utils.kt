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
package io.github.thibaultbee.srtdroid.examples

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import io.github.thibaultbee.srtdroid.examples.R
import io.github.thibaultbee.srtdroid.models.Error
import java.io.File
import java.io.FileOutputStream


object Utils {
    fun showAlertDialog(context: Context, title: String, message: String = "") {
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
        FileOutputStream(file).use {
            it.write(text.toByteArray())
        }
    }
}
