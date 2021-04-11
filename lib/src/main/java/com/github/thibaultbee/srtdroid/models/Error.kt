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
package com.github.thibaultbee.srtdroid.models

import com.github.thibaultbee.srtdroid.Srt
import com.github.thibaultbee.srtdroid.enums.ErrorType

/**
 * This class contains API to manage errors.
 * Once it has been called, you must release Srt context with [Srt.cleanUp] when application leaves.
 */
object Error {
    init {
        Srt.startUp()
    }

    private external fun nativeGetLastErrorMessage(): String

    /**
     * Gets the text message for the last error.
     *
     * **See Also:** [srt_getlasterror_str](https://github.com/Haivision/srt/blob/master/docs/API-functions.md#srt_getlasterror_str)
     *
     * @return the last error message
     */
    val lastErrorMessage: String
        get() = nativeGetLastErrorMessage()

    private external fun nativeGetLastError(): ErrorType

    /**
     * Gets the last error code.
     *
     * **See Also:** [srt_getlasterror](https://github.com/Haivision/srt/blob/master/docs/API-functions.md#srt_getlasterror)
     *
     * @return the [ErrorType] of the error
     */
    val lastError: ErrorType
        get() = nativeGetLastError()

    /**
     * Clears the last error code.
     *
     * **See Also:** [srt_clearlasterror](https://github.com/Haivision/srt/blob/master/docs/API-functions.md#srt_clearlasterror)
     */
    external fun clearLastError()
}