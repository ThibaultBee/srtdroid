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

class RejectReasonCode {
public:
    inline static const char *clazzIdentifier = REJECT_REASON_CLASS;
    inline static SRT_REJECT_REASON fallbackError = SRT_REJ_UNKNOWN;
    inline static map <string, SRT_REJECT_REASON> map = {{"UNKNOWN", SRT_REJ_UNKNOWN},
                                                         {"SYSTEM", SRT_REJ_SYSTEM},
                                                         {"PEER", SRT_REJ_PEER},
                                                         {"RESOURCE", SRT_REJ_RESOURCE},
                                                         {"ROGUE", SRT_REJ_ROGUE},
                                                         {"BACKLOG", SRT_REJ_BACKLOG},
                                                         {"IPE", SRT_REJ_IPE},
                                                         {"CLOSE", SRT_REJ_CLOSE},
                                                         {"VERSION", SRT_REJ_VERSION},
                                                         {"RDVCOOKIE", SRT_REJ_RDVCOOKIE},
                                                         {"BADSECRET", SRT_REJ_BADSECRET},
                                                         {"UNSECURE", SRT_REJ_UNSECURE},
                                                         {"MESSAGEAPI", SRT_REJ_MESSAGEAPI},
                                                         {"CONGESTION", SRT_REJ_CONGESTION},
                                                         {"FILTER", SRT_REJ_FILTER},
                                                         {"GROUP", SRT_REJ_GROUP},
                                                         {"TIMEOUT", SRT_REJ_TIMEOUT}
    };
};

