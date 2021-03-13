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

import com.github.thibaultbee.srtdroid.models.rejectreason.InternalRejectReason

/**
 * Internal reject reason for [InternalRejectReason].
 *
 * **See Also:** [Reject Reasons](https://github.com/Haivision/srt/blob/master/docs/API-functions.md#rejection-reasons-1)
 */
enum class RejectReasonCode {
    /**
     * Initial set when in progress
     */
    UNKNOWN,

    /**
     * Broken due to system function error
     */
    SYSTEM,

    /**
     * Connection was rejected by peer
     */
    PEER,

    /**
     * Internal problem with resource allocation
     */
    RESOURCE,

    /**
     * Incorrect data in handshake messages
     */
    ROGUE,

    /**
     * Listener's backlog exceeded
     */
    BACKLOG,

    /**
     * Internal program error
     */
    IPE,

    /**
     * Socket is closing
     */
    CLOSE,

    /**
     * Peer is older version than agent's minimum set
     */
    VERSION,

    /**
     * Rendezvous cookie collision
     */
    RDVCOOKIE,

    /**
     * Wrong password
     */
    BADSECRET,

    /**
     * Password required or unexpected
     */
    UNSECURE,

    /**
     * Streamapi/messageapi collision
     */
    MESSAGEAPI,

    /**
     * Incompatible congestion-controller type
     */
    CONGESTION,

    /**
     * Incompatible packet filter
     */
    FILTER,

    /**
     * Incompatible group
     */
    GROUP,

    /**
     * Connection timeout
     */
    TIMEOUT;

    /**
     * Returns a string for the reason of the connection reject.
     *
     * **See Also:** [srt_rejectreason_str](https://github.com/Haivision/srt/blob/master/docs/API-functions.md#srt_rejectreason_str)
     *
     * @return the string reason of the connection reject.
     */
    external override fun toString(): String

    companion object {
        /**
         * Standard server error codes offset
         */
        const val PREDEFINED_OFFSET = 1000 // SRT_REJC_PREDEFINED

        /**
         * User defined error codes offset
         */
        const val USERDEFINED_OFFSET = 2000 //  SRT_REJC_USERDEFINED
    }
}