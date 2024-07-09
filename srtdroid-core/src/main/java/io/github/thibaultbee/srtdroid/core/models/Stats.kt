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

/**
 * This class represents SRT statistics
 *
 * **See Also:** [statistics.md](https://github.com/Haivision/srt/blob/master/docs/statistics.md)
 */
data class Stats(
    /**
     * The time since the UDT entity is started, in milliseconds
     */
    val msTimeStamp: Long,
    /**
     * The total number of sent data packets, including retransmissions
     */
    val pktSentTotal: Long,
    /**
     * The total number of received packets
     */
    val pktRecvTotal: Long,
    /**
     * The total number of lost packets (sender side)
     */
    val pktSndLossTotal: Int,
    /**
     * The total number of lost packets (receiver side)
     */
    val pktRcvLossTotal: Int,
    /**
     * The total number of retransmitted packets
     */
    val pktRetransTotal: Int,
    /**
     * The total number of sent ACK packets
     */
    val pktSentACKTotal: Int,
    /**
     * The total number of received ACK packets
     */
    val pktRecvACKTotal: Int,
    /**
     * The total number of sent NAK packets
     */
    val pktSentNAKTotal: Int,
    /**
     * The total number of received NAK packets
     */
    val pktRecvNAKTotal: Int,
    /**
     * The total time duration when UDT is sending data (idle time exclusive)
     */
    val usSndDurationTotal: Long,

    /**
     * The number of too-late-to-send dropped packets
     */
    val pktSndDropTotal: Int,
    /**
     * The number of too-late-to play missing packets
     */
    val pktRcvDropTotal: Int,
    /**
     * The number of undecrypted packets
     */
    val pktRcvUndecryptTotal: Int,
    /**
     * The total number of sent data bytes, including retransmissions
     */
    val byteSentTotal: Long,
    /**
     * The total number of received bytes
     */
    val byteRecvTotal: Long,
    /**
     * The total number of lost bytes
     */
    val byteRcvLossTotal: Long,
    /**
     * The total number of retransmitted bytes
     */
    val byteRetransTotal: Long,
    /**
     * The number of too-late-to-send dropped bytes
     */
    val byteSndDropTotal: Long,
    /**
     * The number of too-late-to play missing bytes (estimate based on average packet size)
     */
    val byteRcvDropTotal: Long,
    /**
     * The number of undecrypted bytes
     */
    val byteRcvUndecryptTotal: Long,
    /**
     * The number of sent data packets, including retransmissions
     */
    val pktSent: Long,
    /**
     * The number of received packets
     */
    val pktRecv: Long,
    /**
     * The number of lost packets (sender side)
     */
    val pktSndLoss: Int,
    /**
     * The number of lost packets (receiver side)
     */
    val pktRcvLoss: Int,
    /**
     * The number of retransmitted packets
     */
    val pktRetrans: Int,
    /**
     * The number of retransmitted packets received
     */
    val pktRcvRetrans: Int,
    /**
     * The number of sent ACK packets
     */
    val pktSentACK: Int,
    /**
     * The number of received ACK packets
     */
    val pktRecvACK: Int,
    /**
     * The number of sent NAK packets
     */
    val pktSentNAK: Int,
    /**
     * The number of received NAK packets
     */
    val pktRecvNAK: Int,
    /**
     * The sending rate in Mb/s
     */
    val mbpsSendRate: Double,
    /**
     * The receiving rate in Mb/s
     */
    val mbpsRecvRate: Double,
    /**
     * The busy sending time (i.e., idle time exclusive)
     */
    val usSndDuration: Long,
    /**
     * The size of order discrepancy in received sequences
     */
    val pktReorderDistance: Int,
    /**
     * The average time of packet delay for belated packets (packets with sequence past the ACK)
     */
    val pktRcvAvgBelatedTime: Double,
    /**
     * The number of received AND IGNORED packets due to having come too late
     */
    val pktRcvBelated: Long,

    /**
     * The number of too-late-to-send dropped packets
     */
    val pktSndDrop: Int,
    /**
     * The number of too-late-to play missing packets
     */
    val pktRcvDrop: Int,
    /**
     * The number of undecrypted packets
     */
    val pktRcvUndecrypt: Int,
    /**
     * The number of sent data bytes, including retransmissions
     */
    val byteSent: Long,
    /**
     * The number of received bytes
     */
    val byteRecv: Long,
    /**
     * The number of lost bytes
     */
    val byteRcvLoss: Long,
    /**
     * The number of retransmitted bytes
     */
    val byteRetrans: Long,
    /**
     * The number of too-late-to-send dropped bytes
     */
    val byteSndDrop: Long,
    /**
     * The number of too-late-to play missing bytes (estimate based on average packet size)
     */
    val byteRcvDrop: Long,
    /**
     * The number of undecrypted bytes
     */
    val byteRcvUndecrypt: Long,

    /**
     * The packet sending period, in microseconds
     */
    val usPktSndPeriod: Double,
    /**
     * The flow window size, in number of packets
     */
    val pktFlowWindow: Int,
    /**
     * The congestion window size, in number of packets
     */
    val pktCongestionWindow: Int,
    /**
     * The number of packets on flight
     */
    val pktFlightSize: Int,
    /**
     * The RTT, in milliseconds
     */
    val msRTT: Double,
    /**
     * The estimated bandwidth, in Mb/s
     */
    val mbpsBandwidth: Double,
    /**
     * The available UDT sender buffer size
     */
    val byteAvailSndBuf: Int,
    /**
     * The available UDT receiver buffer size
     */
    val byteAvailRcvBuf: Int,

    /**
     * The Transmit Bandwidth ceiling (Mbps)
     */
    val mbpsMaxBW: Double,
    /**
     * The MTU
     */
    val byteMSS: Int,

    /**
     * The UnACKed packets in UDT sender
     */
    val pktSndBuf: Int,
    /**
     * The UnACKed bytes in UDT sender
     */
    val byteSndBuf: Int,
    /**
     * The UnACKed timespan (msec) of UDT sender
     */
    val msSndBuf: Int,
    /**
     * The Timestamp-based Packet Delivery Delay
     */
    val msSndTsbPdDelay: Int,

    /**
     * The Undelivered packets in UDT receiver
     */
    val pktRcvBuf: Int,
    /**
     * The Undelivered bytes of UDT receiver
     */
    val byteRcvBuf: Int,
    /**
     * The Undelivered timespan (msec) of UDT receiver
     */
    val msRcvBuf: Int,
    /**
     * The Timestamp-based Packet Delivery Delay
     */
    val msRcvTsbPdDelay: Int,

    /**
     * The number of control packets supplied by packet filter
     */
    val pktSndFilterExtraTotal: Int,
    /**
     * The number of control packets received and not supplied back
     */
    val pktRcvFilterExtraTotal: Int,
    /**
     * The number of packets that the filter supplied extra (e.g. FEC rebuilt)
     */
    val pktRcvFilterSupplyTotal: Int,
    /**
     * The number of packet loss not coverable by filter
     */
    val pktRcvFilterLossTotal: Int,

    /**
     * The number of control packets supplied by packet filter
     */
    val pktSndFilterExtra: Int,
    /**
     * The number of control packets received and not supplied back
     */
    val pktRcvFilterExtra: Int,
    /**
     * The number of packets that the filter supplied extra (e.g. FEC rebuilt)
     */
    val pktRcvFilterSupply: Int,
    /**
     * The number of packet loss not coverable by filter
     */
    val pktRcvFilterLoss: Int,
    /**
     * The packet reorder tolerance value
     */
    val pktReorderTolerance: Int,

    /**
     * The total number of data packets sent by the application
     */
    val pktSentUniqueTotal: Long,
    /**
     * The total number of packets to be received by the application
     */
    val pktRecvUniqueTotal: Long,
    /**
     * The total number of data bytes, sent by the application
     */
    val byteSentUniqueTotal: Long,
    /**
     * The total number of data bytes to be received by the application
     */
    val byteRecvUniqueTotal: Long,

    /**
     * The number of data packets sent by the application
     */
    val pktSentUnique: Long,
    /**
     * The number of packets to be received by the application
     */
    val pktRecvUnique: Long,
    /**
     * The number of data bytes, sent by the application
     */
    val byteSentUnique: Long,
    /**
     * The number of data bytes to be received by the application
     */
    val byteRecvUnique: Long
)
