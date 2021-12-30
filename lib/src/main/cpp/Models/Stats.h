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
#pragma once

#include "Models.h"

class Stats {
public:
    static jobject getJava(JNIEnv *env, SRT_TRACEBSTATS tracebstats) {
        jclass statsClazz = env->FindClass(STATS_CLASS);
        if (!statsClazz) {
            LOGE("Can't find Srt Stats class");
            return nullptr;
        }

        jmethodID statsConstructorMethod = env->GetMethodID(statsClazz, "<init>",
                                                            "(JJJIIIIIIIJIIIJJJJJJJJJIIIIIIIIDDJIDJIIIJJJJJJJDIIIDDIIDIIIIIIIIIIIIIIIIIIJJJJJJJJ)V");
        if (!statsConstructorMethod) {
            LOGE("Can't get Stats constructor");
            env->DeleteLocalRef(statsClazz);
            return nullptr;
        }

        jobject srtSocket = env->NewObject(statsClazz, statsConstructorMethod,
                                           tracebstats.msTimeStamp,
                                           tracebstats.pktSentTotal,
                                           tracebstats.pktRecvTotal,
                                           tracebstats.pktSndLossTotal,
                                           tracebstats.pktRcvLossTotal,
                                           tracebstats.pktRetransTotal,
                                           tracebstats.pktSentACKTotal,
                                           tracebstats.pktRecvACKTotal,
                                           tracebstats.pktSentNAKTotal,
                                           tracebstats.pktRecvNAKTotal,
                                           tracebstats.usSndDurationTotal,

                                           tracebstats.pktSndDropTotal,
                                           tracebstats.pktRcvDropTotal,
                                           tracebstats.pktRcvUndecryptTotal,
                                           (jlong) tracebstats.byteSentTotal,
                                           (jlong) tracebstats.byteRecvTotal,
                                           (jlong) tracebstats.byteRcvLossTotal,
                                           (jlong) tracebstats.byteRetransTotal,
                                           (jlong) tracebstats.byteSndDropTotal,
                                           (jlong) tracebstats.byteRcvDropTotal,
                                           (jlong) tracebstats.byteRcvUndecryptTotal,
                                           tracebstats.pktSent,
                                           tracebstats.pktRecv,
                                           tracebstats.pktSndLoss,
                                           tracebstats.pktRcvLoss,
                                           tracebstats.pktRetrans,
                                           tracebstats.pktRcvRetrans,
                                           tracebstats.pktSentACK,
                                           tracebstats.pktRecvACK,
                                           tracebstats.pktSentNAK,
                                           tracebstats.pktRecvNAK,
                                           tracebstats.mbpsSendRate,
                                           tracebstats.mbpsRecvRate,
                                           tracebstats.usSndDuration,
                                           tracebstats.pktReorderDistance,
                                           tracebstats.pktRcvAvgBelatedTime,
                                           tracebstats.pktRcvBelated,

                                           tracebstats.pktSndDrop,
                                           tracebstats.pktRcvDrop,
                                           tracebstats.pktRcvUndecrypt,
                                           (jlong) tracebstats.byteSent,
                                           (jlong) tracebstats.byteRecv,
                                           (jlong) tracebstats.byteRcvLoss,
                                           (jlong) tracebstats.byteRetrans,
                                           (jlong) tracebstats.byteSndDrop,
                                           (jlong) tracebstats.byteRcvDrop,
                                           (jlong) tracebstats.byteRcvUndecrypt,

                                           tracebstats.usPktSndPeriod,
                                           tracebstats.pktFlowWindow,
                                           tracebstats.pktCongestionWindow,
                                           tracebstats.pktFlightSize,
                                           tracebstats.msRTT,
                                           tracebstats.mbpsBandwidth,
                                           tracebstats.byteAvailSndBuf,
                                           tracebstats.byteAvailRcvBuf,

                                           tracebstats.mbpsMaxBW,
                                           tracebstats.byteMSS,

                                           tracebstats.pktSndBuf,
                                           tracebstats.byteSndBuf,
                                           tracebstats.msSndBuf,
                                           tracebstats.msSndTsbPdDelay,

                                           tracebstats.pktRcvBuf,
                                           tracebstats.byteRcvBuf,
                                           tracebstats.msRcvBuf,
                                           tracebstats.msRcvTsbPdDelay,

                                           tracebstats.pktSndFilterExtraTotal,
                                           tracebstats.pktRcvFilterExtraTotal,
                                           tracebstats.pktRcvFilterSupplyTotal,
                                           tracebstats.pktRcvFilterLossTotal,

                                           tracebstats.pktSndFilterExtra,
                                           tracebstats.pktRcvFilterExtra,
                                           tracebstats.pktRcvFilterSupply,
                                           tracebstats.pktRcvFilterLoss,
                                           tracebstats.pktReorderTolerance,

                                           (jlong) tracebstats.pktSentUniqueTotal,
                                           (jlong) tracebstats.pktRecvUniqueTotal,
                                           (jlong) tracebstats.byteSentUniqueTotal,
                                           (jlong) tracebstats.byteRecvUniqueTotal,

                                           (jlong) tracebstats.pktSentUnique,
                                           (jlong) tracebstats.pktRecvUnique,
                                           (jlong) tracebstats.byteSentUnique,
                                           (jlong) tracebstats.byteRecvUnique
        );

        env->DeleteLocalRef(statsClazz);

        return srtSocket;
    }
};