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
package io.github.thibaultbee.srtdroid.core.models

import io.github.thibaultbee.srtdroid.core.Srt

/**
 * This class contains time utils.
 * Once it has been called, you must release Srt context with [Srt.cleanUp] when application leaves.
 */
object Time {
    init {
        Srt.startUp()
    }

    /**
     * Gets time in microseconds elapsed since epoch using SRT internal clock.
     *
     * **See Also:** [srt_time_now](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_time_now)
     *
     * @return the current time in microseconds
     */
    external fun now(): Long
}