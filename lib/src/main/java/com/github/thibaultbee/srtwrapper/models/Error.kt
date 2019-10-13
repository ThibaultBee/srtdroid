package com.github.thibaultbee.srtwrapper.models

class Error {
    companion object {
        @JvmStatic private external fun nativeGetLastErrorStr(): String
        @JvmStatic private external fun nativeGetLastError(): Int

        @JvmStatic
        fun getLastErrorMessage(): String {
            return nativeGetLastErrorStr()
        }

        @JvmStatic
        fun getLastError(): Int {
            return nativeGetLastError()
        }
    }
}