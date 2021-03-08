/*
 * Copyright (C) 2021 Thibault Beyou
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

import com.github.thibaultbee.srtdroid.enums.EpollFlag
import com.github.thibaultbee.srtdroid.enums.EpollOpt

class Epoll {
    private var eid: Int

    init {
        eid = create()
    }

    private external fun create(): Int

    external fun nativeIsValid(): Boolean
    val isValid: Boolean
        get() = nativeIsValid()

    external fun addUSock(socket: Socket, events: List<EpollOpt> = emptyList()): Int

    external fun updateUSock(socket: Socket, events: List<EpollOpt> = emptyList()): Int

    external fun removeUSock(socket: Socket): Int

    external fun wait(
        readFds: List<Socket> = emptyList(),
        writeFds: List<Socket> = emptyList(),
        timeOut: Long
    ): Int

    external fun uWait(fdsSet: List<EpollEvent>, timeOut: Long): Int

    external fun setFlags(events: List<EpollFlag>): List<EpollFlag>
    private external fun nativeGetFlags(): List<EpollFlag>
    var flags: List<EpollFlag>
        get() = nativeGetFlags()
        set(value) {
            setFlags(value)
        }

    external fun release(): Int
}