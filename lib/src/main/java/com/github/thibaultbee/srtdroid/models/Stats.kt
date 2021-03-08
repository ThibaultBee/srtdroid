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

data class Stats(
    val msTimeStamp: Long,
    val pktSentTotal: Long,
    val pktRecvTotal: Long,
    val pktSndLossTotal: Int,
    val pktRcvLossTotal: Int,
    val pktRetransTotal: Int,
    val pktSentACKTotal: Int,
    val pktRecvACKTotal: Int,
    val pktSentNAKTotal: Int,
    val pktRecvNAKTotal: Int,
    val usSndDurationTotal: Long,

    val pktSndDropTotal: Int,
    val pktRcvDropTotal: Int,
    val pktRcvUndecryptTotal: Int,
    val byteSentTotal: Long,
    val byteRecvTotal: Long,
    val byteRcvLossTotal: Long,
    val byteRetransTotal: Long,
    val byteSndDropTotal: Long,
    val byteRcvDropTotal: Long,
    val byteRcvUndecryptTotal: Long,
    val pktSent: Long,
    val pktRecv: Long,
    val pktSndLoss: Int,
    val pktRcvLoss: Int,
    val pktRetrans: Int,
    val pktRcvRetrans: Int,
    val pktSentACK: Int,
    val pktRecvACK: Int,
    val pktSentNAK: Int,
    val pktRecvNAK: Int,
    val mbpsSendRate: Double,
    val mbpsRecvRate: Double,
    val usSndDuration: Long,
    val pktReorderDistance: Int,
    val pktRcvAvgBelatedTime: Double,
    val pktRcvBelated: Long,

    val pktSndDrop: Int,
    val pktRcvDrop: Int,
    val pktRcvUndecrypt: Int,
    val byteSent: Long,
    val byteRecv: Long,
    val byteRcvLoss: Long,
    val byteRetrans: Long,
    val byteSndDrop: Long,
    val byteRcvDrop: Long,
    val byteRcvUndecrypt: Long,

    val usPktSndPeriod: Double,
    val pktFlowWindow: Int,
    val pktCongestionWindow: Int,
    val pktFlightSize: Int,
    val msRTT: Double,
    val mbpsBandwidth: Double,
    val byteAvailSndBuf: Int,
    val byteAvailRcvBuf: Int,

    val mbpsMaxBW: Double,
    val byteMSS: Int,

    val pktSndBuf: Int,
    val byteSndBuf: Int,
    val msSndBuf: Int,
    val msSndTsbPdDelay: Int,

    val pktRcvBuf: Int,
    val byteRcvBuf: Int,
    val msRcvBuf: Int,
    val msRcvTsbPdDelay: Int,

    val pktSndFilterExtraTotal: Int,
    val pktRcvFilterExtraTotal: Int,
    val pktRcvFilterSupplyTotal: Int,
    val pktRcvFilterLossTotal: Int,

    val pktSndFilterExtra: Int,
    val pktRcvFilterExtra: Int,
    val pktRcvFilterSupply: Int,
    val pktRcvFilterLoss: Int,
    val pktReorderTolerance: Int,

    val pktSentUniqueTotal: Long,
    val pktRecvUniqueTotal: Long,
    val byteSentUniqueTotal: Long,
    val byteRecvUniqueTotal: Long,

    val pktSentUnique: Long,
    val pktRecvUnique: Long,
    val byteSentUnique: Long,
    val byteRecvUnique: Long
)
