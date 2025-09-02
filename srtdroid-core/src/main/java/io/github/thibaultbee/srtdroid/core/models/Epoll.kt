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

import android.util.Pair
import io.github.thibaultbee.srtdroid.core.Srt
import io.github.thibaultbee.srtdroid.core.enums.EpollFlag
import io.github.thibaultbee.srtdroid.core.enums.EpollOpt
import java.security.InvalidParameterException

/**
 * This class is currently the only method for using multiple sockets in one thread with having the
 * blocking operation moved to epoll waiting so that it can block on multiple sockets at once.
 * Once it has been called, you must release Srt context with [Srt.cleanUp] when application leaves.
 *
 * **See Also:** [Asynchronous operations (epoll)](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#asynchronous-operations-epoll-1)
 */
class Epoll
private constructor(private val eid: Int) {
    companion object {
        @JvmStatic
        private external fun nativeCreate(): Int
    }

    /**
     * Creates an epoll container.
     */
    constructor() : this(nativeCreate())

    private external fun nativeIsValid(): Boolean

    /**
     * Tests if the [Epoll] is a valid one.
     *
     * @return true if [Epoll] is valid, otherwise false
     */
    val isValid: Boolean
        get() = nativeIsValid()

    private external fun nativeAddUSock(
        socket: SrtSocket,
        events: List<EpollOpt>?
    ): Int

    /**
     * Adds a socket to a epoll container.
     *
     * **See Also:** [srt_epoll_add_usock](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_epoll_add_usock)
     *
     * @param socket the SRT socket to add
     * @param events list of selected [EpollOpt]. Set null if you want to subscribe a socket for all events.
     * @throws InvalidParameterException if [Epoll] is not valid
     */
    fun addUSock(
        socket: SrtSocket,
        events: List<EpollOpt>?
    ) {
        if (nativeAddUSock(socket, events) != 0) {
            throw InvalidParameterException(SrtError.lastErrorMessage)
        }
    }

    private external fun nativeUpdateUSock(
        socket: SrtSocket,
        events: List<EpollOpt>?
    ): Int

    /**
     * Updates a socket to a epoll container.
     *
     * **See Also:** [srt_epoll_update_usock](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_epoll_update_usock)
     *
     * @param socket the SRT socket to add
     * @param events list of selected [EpollOpt]. Set null if you want to subscribe a socket for all events.
     * @throws InvalidParameterException if [Epoll] is not valid
     */
    fun updateUSock(
        socket: SrtSocket,
        events: List<EpollOpt>?
    ) {
        if (nativeUpdateUSock(socket, events) != 0) {
            throw InvalidParameterException(SrtError.lastErrorMessage)
        }
    }

    private external fun nativeRemoveUSock(socket: SrtSocket): Int

    /**
     * Removes a specified socket from an epoll container.
     *
     * **See Also:** [srt_epoll_remove_usock](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_epoll_remove_usock)
     *
     * @param socket the SRT socket to add
     * @throws InvalidParameterException if [Epoll] is not valid
     */
    fun removeUSock(socket: SrtSocket) {
        if (nativeRemoveUSock(socket) != 0) {
            throw InvalidParameterException(SrtError.lastErrorMessage)
        }
    }

    private external fun nativeWait(
        timeOut: Long,
        expectedReadReadySocketSize: Int,
        expectedWriteReadySocketSize: Int,
    ): Pair<Int, Pair<List<SrtSocket>, List<SrtSocket>>>

    /**
     * Blocks the call until any readiness state occurs in the epoll container.
     *
     * **See Also:** [srt_epoll_wait](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_epoll_wait)
     *
     * @param readFds List of SRT sockets that are read-ready
     * @param writeFds List of SRT sockets that are write-ready
     * @param timeout Timeout specified in milliseconds. Set to -1, if you want to block indefinitely
     * @throws InvalidParameterException if [Epoll] is not valid or timeout is triggered
     */
    fun wait(
        timeout: Long,
        expectedReadReadySocketSize: Int = 2,
        expectedWriteReadySocketSize: Int = 2
    ): Pair<List<SrtSocket>, List<SrtSocket>> {
        val pair = nativeWait(timeout, expectedReadReadySocketSize, expectedWriteReadySocketSize)
        if (pair.first < 0) {
            throw InvalidParameterException(SrtError.lastErrorMessage)
        }
        return pair.second
    }

    private external fun nativeUWait(
        timeOut: Long,
        expectedEpollEventSize: Int
    ): Pair<Int, List<EpollEvent>>

    /**
     * Blocks a call until any readiness state occurs in the epoll container.
     *
     * **See Also:** [srt_epoll_uwait](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_epoll_uwait)
     *
     * @param fdsSet List of [EpollEvent]
     * @param timeout Timeout specified in milliseconds. Set to -1, if you want to block indefinitely
     * @throws InvalidParameterException if [Epoll] is not valid or if fdsSet is invalid
     */
    fun uWait(
        timeout: Long,
        expectedEpollEventSize: Int = 2
    ): List<EpollEvent> {
        val epollEvents = mutableListOf<EpollEvent>()
        while (true) {
            val pair = nativeUWait(timeout, expectedEpollEventSize)
            if (pair.first < 0) {
                throw InvalidParameterException(SrtError.lastErrorMessage)
            }
            epollEvents.addAll(pair.second)
            if (pair.first <= expectedEpollEventSize) {
                break
            }
        }
        return epollEvents
    }

    private external fun nativeClearUSock(): Int

    /**
     * Removes all SRT socket subscriptions from the epoll container.
     *
     * **See Also:** [srt_epoll_clear_usocks](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_epoll_clear_usocks)
     *
     * @throws InvalidParameterException if [Epoll] is not valid
     */
    fun clearUSock() {
        if (nativeClearUSock() != 0) {
            throw InvalidParameterException(SrtError.lastErrorMessage)
        }
    }

    private external fun nativeSetFlags(events: List<EpollFlag>): List<EpollFlag>?
    private external fun nativeGetFlags(): List<EpollFlag>?

    /**
     * Set flags that change the default behavior of the epoll functions. Same as [flags] but returns the effective list of [EpollFlag].
     *
     * **See Also:** [srt_epoll_set](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_epoll_set)
     *
     * @param [EpollFlag] to set
     * @see [flags]
     */
    fun setFlags(events: List<EpollFlag>) =
        nativeSetFlags(events) ?: throw InvalidParameterException(SrtError.lastErrorMessage)

    /**
     * Set or retrieves flags that change the default behavior of the epoll functions.
     *
     * **See Also:** [srt_epoll_set](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_epoll_set)
     */
    var flags: List<EpollFlag>
        /**
         * Get [Epoll] function flag.
         *
         * @return the current [EpollFlag]
         * @throws InvalidParameterException if [Epoll] is not valid
         */
        get() = nativeGetFlags() ?: throw InvalidParameterException(SrtError.lastErrorMessage)
        /**
         * Get [Epoll] function flag.
         *
         * @param [EpollFlag] to set
         * @throws InvalidParameterException if [Epoll] is not valid
         */
        set(value) {
            setFlags(value)
        }

    private external fun nativeRelease(): Int

    /**
     * Deletes the epoll container.
     *
     * **See Also:** [srt_epoll_release](https://github.com/Haivision/srt/blob/master/docs/API/API-functions.md#srt_epoll_release)
     *
     * @throws InvalidParameterException if [Epoll] is not valid
     */
    fun release() {
        if (nativeRelease() != 0) {
            throw InvalidParameterException(SrtError.lastErrorMessage)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Epoll) return false
        return eid == other.eid
    }

    override fun hashCode(): Int {
        return eid.hashCode()
    }
}