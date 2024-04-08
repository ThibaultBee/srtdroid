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

class SockStatus {
public:
    inline static const char *clazzIdentifier = SOCKSTATUS_CLASS;
    inline static SRT_SOCKSTATUS fallbackError = SRTS_BROKEN;
    inline static map<string, SRT_SOCKSTATUS> map = {{"INIT",       SRTS_INIT},
                                                     {"OPENED",     SRTS_OPENED},
                                                     {"LISTENING",  SRTS_LISTENING},
                                                     {"CONNECTING", SRTS_CONNECTING},
                                                     {"CONNECTED",  SRTS_CONNECTED},
                                                     {"BROKEN",     SRTS_BROKEN},
                                                     {"CLOSING",    SRTS_CLOSING},
                                                     {"CLOSED",     SRTS_CLOSED},
                                                     {"NONEXIST",   SRTS_NONEXIST}
    };
};

