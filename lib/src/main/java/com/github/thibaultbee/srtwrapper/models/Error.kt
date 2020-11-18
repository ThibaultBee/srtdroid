package com.github.thibaultbee.srtwrapper.models

import com.github.thibaultbee.srtwrapper.enums.ErrorType

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