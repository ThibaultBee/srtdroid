package com.github.thibaultbee.srtwrapper.chat.utils

import com.github.thibaultbee.srtwrapper.models.Error

class ErrorUtils {
    companion object {
        fun getMessage(): String {
            val message = Error.getLastErrorMessage()
            Error.clearLastError()
            return message
        }
    }
}