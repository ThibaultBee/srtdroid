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

import io.github.thibaultbee.srtdroid.models.Epoll

/**
 * Use this enumeration in [Epoll.flags] and [Epoll.setFlags].
 *
 * **See Also:** [srt_epoll_set](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_epoll_set)
 */
enum class EpollFlag {
    /**
     * Clear all flags (set all defaults)
     */
    CLEAR_ALL,

    /**
     * Allows the [Epoll.wait] and the [Epoll.uWait] functions to be called with the EID not subscribed to any socket.
     */
    ENABLE_EMPTY,

    /**
     *  Forces the [Epoll.wait] and the [Epoll.uWait] functions to check if the output array is not empty.
     */
    ENABLE_OUTPUTCHECK;
}