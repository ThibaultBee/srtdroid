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

class KMState {
public:
    inline static const char *clazzIdentifier = KMSTATE_CLASS;
    inline static SRT_KM_STATE fallbackError = SRT_KM_S_UNSECURED;
    inline static map <string, SRT_KM_STATE> map = {{"KM_S_UNSECURED", SRT_KM_S_UNSECURED},
                                                    {"KM_S_SECURING",  SRT_KM_S_SECURING},
                                                    {"KM_S_SECURED",   SRT_KM_S_SECURED},
                                                    {"KM_S_NOSECRET",  SRT_KM_S_NOSECRET},
                                                    {"KM_S_BADSECRET", SRT_KM_S_BADSECRET}
    };
};

