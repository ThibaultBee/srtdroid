package com.github.thibaultbee.srtdroid.chat.utils

import com.github.thibaultbee.srtdroid.models.Error

class ErrorUtils {
    companion object {
        fun getMessage(): String {
            val message = Error.lastErrorMessage
            Error.clearLastError()
            return message
        }
    }
}