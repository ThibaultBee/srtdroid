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
import java.security.InvalidParameterException

/**
 * This class is currently the only method for using multiple sockets in one thread with having the
 * blocking operation moved to epoll waiting so that it can block on multiple sockets at once.
 *
 * **See Also:** [Asynchronous operations (epoll)](https://github.com/Haivision/srt/blob/master/docs/API-functions.md#asynchronous-operations-epoll-1)
 */
class Epoll {
    private var eid: Int

    init {
        eid = create()
    }

    private external fun create(): Int

    private external fun nativeIsValid(): Boolean

    /**
     * Tests if the [Epoll] is a valid one.
     *
     * @return true if [Epoll] is valid, otherwise false
     */
    val isValid: Boolean
        get() = nativeIsValid()

    private external fun nativeAddUSock(socket: Socket, events: List<EpollOpt>?): Int

    /**
     * Adds a socket to a epoll container.
     *
     * **See Also:** [srt_epoll_add_usock](https://github.com/Haivision/srt/blob/master/docs/API-functions.md#srt_epoll_add_usock)
     *
     * @param socket the SRT socket to add
     * @param events list of selected [EpollOpt]. Set null if you want to subscribe a socket for all events.
     * @throws InvalidParameterException if [Epoll] is not valid
     */
    fun addUSock(socket: Socket, events: List<EpollOpt>?) {
        if (nativeAddUSock(socket, events) != 0) {
            throw InvalidParameterException(Error.lastErrorMessage)
        }
    }

    private external fun nativeUpdateUSock(socket: Socket, events: List<EpollOpt>?): Int

    /**
     * Updates a socket to a epoll container.
     *
     * **See Also:** [srt_epoll_update_usock](https://github.com/Haivision/srt/blob/master/docs/API-functions.md#srt_epoll_update_usock)
     *
     * @param socket the SRT socket to add
     * @param events list of selected [EpollOpt]. Set null if you want to subscribe a socket for all events.
     * @throws InvalidParameterException if [Epoll] is not valid
     */
    fun updateUSock(socket: Socket, events: List<EpollOpt>?) {
        if (nativeUpdateUSock(socket, events) != 0) {
            throw InvalidParameterException(Error.lastErrorMessage)
        }
    }

    private external fun nativeRemoveUSock(socket: Socket): Int

    /**
     * Removes a specified socket from an epoll container.
     *
     * **See Also:** [srt_epoll_remove_usock](https://github.com/Haivision/srt/blob/master/docs/API-functions.md#srt_epoll_remove_usock)
     *
     * @param socket the SRT socket to add
     * @throws InvalidParameterException if [Epoll] is not valid
     */
    fun removeUSock(socket: Socket) {
        if (nativeRemoveUSock(socket) != 0) {
            throw InvalidParameterException(Error.lastErrorMessage)
        }
    }

    private external fun nativeWait(
        readFds: List<Socket>,
        writeFds: List<Socket>,
        timeOut: Long
    ): Int

    /**
     * Blocks the call until any readiness state occurs in the epoll container.
     *
     * **See Also:** [srt_epoll_wait](https://github.com/Haivision/srt/blob/master/docs/API-functions.md#srt_epoll_wait)
     *
     * @param readFds List of SRT sockets that are read-ready
     * @param writeFds List of SRT sockets that are write-ready
     * @param timeout Timeout specified in milliseconds. Set to -1, if you want to block indefinitely
     * @throws InvalidParameterException if [Epoll] is not valid or timeout is triggered
     */
    fun wait(
        readFds: List<Socket> = emptyList(),
        writeFds: List<Socket> = emptyList(),
        timeout: Long
    ) {
        if (nativeWait(readFds, writeFds, timeout) != 0) {
            throw InvalidParameterException(Error.lastErrorMessage)
        }
    }

    private external fun nativeUWait(fdsSet: List<EpollEvent>, timeOut: Long): Int

    /**
     * Blocks a call until any readiness state occurs in the epoll container.
     *
     * **See Also:** [srt_epoll_uwait](https://github.com/Haivision/srt/blob/master/docs/API-functions.md#srt_epoll_uwait)
     *
     * @param fdsSet List of [EpollEvent]
     * @param timeout Timeout specified in milliseconds. Set to -1, if you want to block indefinitely
     * @throws InvalidParameterException if [Epoll] is not valid or if fdsSet is invalid
     */
    fun uWait(
        fdsSet: List<EpollEvent>, timeout: Long
    ) {
        if (nativeUWait(fdsSet, timeout) != 0) {
            throw InvalidParameterException(Error.lastErrorMessage)
        }
    }

    private external fun nativeClearUSock(): Int

    /**
     * Removes all SRT socket subscriptions from the epoll container.
     *
     * **See Also:** [srt_epoll_clear_usocks](https://github.com/Haivision/srt/blob/master/docs/API-functions.md#srt_epoll_clear_usocks)
     *
     * @throws InvalidParameterException if [Epoll] is not valid
     */
    fun clearUSock() {
        if (nativeClearUSock() != 0) {
            throw InvalidParameterException(Error.lastErrorMessage)
        }
    }

    private external fun nativeSetFlags(events: List<EpollFlag>): List<EpollFlag>?
    private external fun nativeGetFlags(): List<EpollFlag>?

    /**
     * Set flags that change the default behavior of the epoll functions. Same as [flags] but returns list of [EpollFlag].
     *
     * **See Also:** [srt_epoll_set](https://github.com/Haivision/srt/blob/master/docs/API-functions.md#srt_epoll_set)
     *
     * @param [EpollFlag] to set
     * @see [flags]
     */
    fun setFlags(events: List<EpollFlag>) =
        nativeSetFlags(events) ?: throw InvalidParameterException(Error.lastErrorMessage)

    /**
     * Set or retrieves flags that change the default behavior of the epoll functions.
     *
     * **See Also:** [srt_epoll_set](https://github.com/Haivision/srt/blob/master/docs/API-functions.md#srt_epoll_set)
     */
    var flags: List<EpollFlag>
        /**
         * Get [Epoll] function flag.
         *
         * @return the current [EpollFlag]
         * @throws InvalidParameterException if [Epoll] is not valid
         */
        get() = nativeGetFlags() ?: throw InvalidParameterException(Error.lastErrorMessage)
        /**
         * Get [Epoll] function flag.
         *
         * @param [EpollFlag] to set
         * @throws InvalidParameterException if [Epoll] is not valid
         */
        set(value) {
            nativeSetFlags(value) ?: throw InvalidParameterException(Error.lastErrorMessage)
        }

    private external fun nativeRelease(): Int

    /**
     * Deletes the epoll container.
     *
     * **See Also:** [srt_epoll_release](https://github.com/Haivision/srt/blob/master/docs/API-functions.md#srt_epoll_release)
     *
     * @throws InvalidParameterException if [Epoll] is not valid
     */
    fun release() {
        if (nativeRelease() != 0) {
            throw InvalidParameterException(Error.lastErrorMessage)
        }
    }
}