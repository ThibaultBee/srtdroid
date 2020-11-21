package com.github.thibaultbee.srtdroid.models

import com.github.thibaultbee.srtdroid.enums.ErrorType

class Error {
    companion object {
        @JvmStatic
        private external fun nativeGetLastErrorMessage(): String
        val lastErrorMessage: String
            get() = nativeGetLastErrorMessage()

        @JvmStatic
        private external fun nativeGetLastError(): ErrorType
        val lastError: ErrorType
            get() = nativeGetLastError()

        @JvmStatic
        external fun clearLastError()
    }
}