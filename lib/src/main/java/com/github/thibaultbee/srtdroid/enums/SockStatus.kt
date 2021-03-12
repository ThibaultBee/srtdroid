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
package com.github.thibaultbee.srtdroid.enums

import com.github.thibaultbee.srtdroid.models.Socket

/**
 * SRT socket status (from [Socket.sockState]).
 *
 * **See Also:** [srt_getsockstate](https://github.com/Haivision/srt/blob/master/docs/API-functions.md#srt_getsockstate)
 */
enum class SockStatus {
    /**
     * Created, but not bound.
     */
    INIT,

    /**
     * Created and bound, but not in use yet.
     */
    OPENED,

    /**
     * Socket is in listening state.
     */
    LISTENING,

    /**
     * The connect operation was initiated, but not yet finished.
     */
    CONNECTING,

    /**
     * The socket is connected and ready for transmission.
     */
    CONNECTED,

    /**
     * The socket was connected, but the connection was broken.
     */
    BROKEN,

    /**
     * The socket may still be open and active, but closing is requested.
     */
    CLOSING,

    /**
     * The socket has been closed, but not yet removed by the GC thread.
     */
    CLOSED,

    /**
     * The specified number does not correspond to a valid socket.
     */
    NON_EXIST
}