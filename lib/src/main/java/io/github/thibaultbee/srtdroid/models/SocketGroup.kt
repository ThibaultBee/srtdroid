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
package io.github.thibaultbee.srtdroid.models

import io.github.thibaultbee.srtdroid.Srt
import io.github.thibaultbee.srtdroid.enums.*
import java.io.*
import java.net.*

/**
 * This class represents a SRT socket or a SRT group socket.
 *
 * To avoid creating an unresponsive UI, don't perform SRT network operations on the main thread.
 * Once it has been called, you must release Srt context with [Srt.cleanUp] when application leaves.
 */
class SocketGroup : Socket {
    companion object {
        init {
            Srt.startUp() // TODO needed?
        }

        @JvmStatic
        private external fun nativeCreateGroup(groupType: GroupType): Int
    }

    constructor(groupType: GroupType) : super(nativeCreateGroup(groupType))
    internal constructor(socket: Int) : super(socket)

    // Socket Group Management
    private external fun nativeGroupData(): List<GroupData>

    /**
     * Get the [GroupData] for each socket of this SRT socket group.
     */
    val data: List<GroupData>
        /**
         * @return the [GroupData] for each socket of this SRT socket group.
         */
        get() = nativeGroupData()

    private external fun nativeGroupSize(): Int

    /**
     * Get this SRT socket group size
     */
    val size: Int
        /**
         * @return this SRT socket group size
         */
        get() = nativeGroupSize()

    private external fun nativeConnect(configs: List<GroupConfig>): Int

    /**
     * This function does almost the same as calling [Socket.connect] in a loop for every item specified in the socket list.
     *
     * **See Also:** [srt_connect_group](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_connect_group)
     *
     * @param configs list of group config to connect
     * @throws ConnectException if connection has failed
     */
    fun connect(configs: List<GroupConfig>) {
        if (nativeConnect(configs) != 0) {
            throw ConnectException(Error.lastErrorMessage)
        }
    }
}