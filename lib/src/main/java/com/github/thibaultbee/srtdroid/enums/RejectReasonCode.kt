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

enum class RejectReasonCode {
    UNKNOWN,     // initial set when in progress
    SYSTEM,      // broken due to system function error
    PEER,        // connection was rejected by peer
    RESOURCE,    // internal problem with resource allocation
    ROGUE,       // incorrect data in handshake messages
    BACKLOG,     // listener's backlog exceeded
    IPE,         // internal program error
    CLOSE,       // socket is closing
    VERSION,     // peer is older version than agent's minimum set
    RDVCOOKIE,   // rendezvous cookie collision
    BADSECRET,   // wrong password
    UNSECURE,    // password required or unexpected
    MESSAGEAPI,  // streamapi/messageapi collision
    CONGESTION,  // incompatible congestion-controller type
    FILTER,      // incompatible packet filter
    GROUP,       // incompatible group
    TIMEOUT;     // connection timeout

    external override fun toString(): String

    companion object {
        val PREDEFINED_OFFSET = 1000 // SRT_REJC_PREDEFINED - Standard server error codes
        val USERDEFINED_OFFSET = 2000 //  SRT_REJC_USERDEFINED - User defined error codes
    }
}