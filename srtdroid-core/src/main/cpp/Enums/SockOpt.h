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

#include <map>
#include "srt/srt.h"
#include "Enums.h"

using namespace std;

class SockOpt {
public:
    inline static const char *clazzIdentifier = SOCKOPT_CLASS;
    inline static int fallbackError = -EINVAL;
    inline static map<string, int> map = {{"MSS",                SRTO_MSS},
                                          {"SNDSYN",             SRTO_SNDSYN},
                                          {"RCVSYN",             SRTO_RCVSYN},
                                          {"ISN",                SRTO_ISN},
                                          {"FC",                 SRTO_FC},
                                          {"SNDBUF",             SRTO_SNDBUF},
                                          {"RCVBUF",             SRTO_RCVBUF},
                                          {"LINGER",             SRTO_LINGER},
                                          {"UDP_SNDBUF",         SRTO_UDP_SNDBUF},
                                          {"UDP_RCVBUF",         SRTO_UDP_RCVBUF},
                                          {"RENDEZVOUS",         SRTO_RENDEZVOUS},
                                          {"SNDTIMEO",           SRTO_SNDTIMEO},
                                          {"RCVTIMEO",           SRTO_RCVTIMEO},
                                          {"REUSEADDR",          SRTO_REUSEADDR},
                                          {"MAXBW",              SRTO_MAXBW},
                                          {"STATE",              SRTO_STATE},
                                          {"EVENT",              SRTO_EVENT},
                                          {"SNDDATA",            SRTO_SNDDATA},
                                          {"RCVDATA",            SRTO_RCVDATA},
                                          {"SENDER",             SRTO_SENDER},
                                          {"TSBPDMODE",          SRTO_TSBPDMODE},
                                          {"LATENCY",            SRTO_LATENCY},
                                          {"INPUTBW",            SRTO_INPUTBW},
                                          {"OHEADBW",            SRTO_OHEADBW},
                                          {"PASSPHRASE",         SRTO_PASSPHRASE},
                                          {"PBKEYLEN",           SRTO_PBKEYLEN},
                                          {"KMSTATE",            SRTO_KMSTATE},
                                          {"IPTTL",              SRTO_IPTTL},
                                          {"IPTOS",              SRTO_IPTOS},
                                          {"TLPKTDROP",          SRTO_TLPKTDROP},
                                          {"SNDDROPDELAY",       SRTO_SNDDROPDELAY},
                                          {"NAKREPORT",          SRTO_NAKREPORT},
                                          {"VERSION",            SRTO_VERSION},
                                          {"PEERVERSION",        SRTO_PEERVERSION},
                                          {"CONNTIMEO",          SRTO_CONNTIMEO},
                                          {"DRIFTTRACER",        SRTO_DRIFTTRACER},
                                          {"MININPUTBW",         SRTO_MININPUTBW},
                                          {"SNDKMSTATE",         SRTO_SNDKMSTATE},
                                          {"RCVKMSTATE",         SRTO_RCVKMSTATE},
                                          {"LOSSMAXTTL",         SRTO_LOSSMAXTTL},
                                          {"RCVLATENCY",         SRTO_RCVLATENCY},
                                          {"PEERLATENCY",        SRTO_PEERLATENCY},
                                          {"MINVERSION",         SRTO_MINVERSION},
                                          {"STREAMID",           SRTO_STREAMID},
                                          {"CONGESTION",         SRTO_CONGESTION},
                                          {"MESSAGEAPI",         SRTO_MESSAGEAPI},
                                          {"PAYLOADSIZE",        SRTO_PAYLOADSIZE},
                                          {"TRANSTYPE",          SRTO_TRANSTYPE},
                                          {"KMREFRESHRATE",      SRTO_KMREFRESHRATE},
                                          {"KMPREANNOUNCE",      SRTO_KMPREANNOUNCE},
                                          {"ENFORCEDENCRYPTION", SRTO_ENFORCEDENCRYPTION},
                                          {"IPV6ONLY",           SRTO_IPV6ONLY},
                                          {"PEERIDLETIMEO",      SRTO_PEERIDLETIMEO},
                                          {"BINDTODEVICE",       SRTO_BINDTODEVICE},
                                          {"PACKETFILTER",       SRTO_PACKETFILTER},
                                          {"RETRANSMITALGO",     SRTO_RETRANSMITALGO}
    };
};
