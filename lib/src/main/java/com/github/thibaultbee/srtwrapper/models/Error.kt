package com.github.thibaultbee.srtwrapper.models

import com.github.thibaultbee.srtwrapper.enums.ErrorType

class Error {
    companion object {
        @JvmStatic
        external fun getLastErrorMessage(): String

        @JvmStatic
        external fun getLastError(): ErrorType

        @JvmStatic
        external fun clearLastError()
    }
}