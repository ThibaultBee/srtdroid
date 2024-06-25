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
package io.github.thibaultbee.srtdroid.enums

/**
 * Used by [SockOpt.TRANSTYPE] option.
 *
 * **See Also:** [srt_transtype](https://github.com/Haivision/srt/blob/master/docs/API/API-socket-options.md#srt_transtype)
 */
enum class Transtype(val displayName: String) {
    /**
     * Live mode.
     */
    LIVE("live"),

    /**
     * File mode.
     */
    FILE("file"),

    /**
     * Invalid mode.
     */
    INVALID("invalid");

    companion object {
        fun entryOf(displayName: String) = entries.firstOrNull { it.displayName == displayName } ?: INVALID
    }
}