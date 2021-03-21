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

import com.github.thibaultbee.srtdroid.enums.Boundary

/**
 * This class represents extra parameters for [Socket.send] and [Socket.recv]
 *
 * **See Also:** [srt_msgctrl](https://github.com/Haivision/srt/blob/master/docs/API-functions.md#srt_msgctrl)
 */
data class MsgCtrl(
    /**
     * Reserved for future use. Should be 0.
     */
    val flags: Int = 0,
    /**
     * The time (in ms) to wait for a successful delivery. -1 means no time limitation.
     */
    val ttl: Int = -1, // SRT_MSGTTL_INF
    /**
     * Required to be received in the order of sending.
     */
    val inOrder: Boolean = false,
    /**
     * Reserved for future use. Should be [Boundary.SUBSEQUENT].
     */
    val boundary: Boundary = Boundary.SUBSEQUENT,
    /**
     * Receiver: specifies the time when the packet was intended to be delivered to the receiving application (in microseconds since SRT clock epoch).
     * Sender: specifies the application-provided timestamp to be associated with the packet.
     */
    val srcTime: Long = 0,
    /**
     * Receiver only: reports the sequence number for the packet carrying out the message being returned.
     */
    val pktSeq: Int = -1, // SRT_SEQNO_NONE
    /**
     * Message number that can be sent by both sender and receiver.
     */
    val no: Int = -1 // SRT_MSGNO_NONE
)