package com.github.thibaultbee.srtwrapper.models

import com.github.thibaultbee.srtwrapper.enums.ErrorType

class Error {
    companion object {
        @JvmStatic
        private external fun nativeGetLastErrorStr(): String

        @JvmStatic
        private external fun nativeGetLastError(): ErrorType

        @JvmStatic
        private external fun nativeClearLastError()

        fun getLastErrorMessage(): String {
            return nativeGetLastErrorStr()
        }

        fun getLastError(): ErrorType {
            return nativeGetLastError()
        }

        fun clearLastError() {
            return nativeClearLastError()
        }
    }
}