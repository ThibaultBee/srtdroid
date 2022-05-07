/*
 * Copyright (C) 2022 Thibault B.
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

class MemberStatus {
public:
    inline static const char *clazzIdentifier = MEMBERSTATUS_CLASS;
    inline static SRT_MEMBERSTATUS fallbackError = SRT_GST_PENDING;
    inline static map <string, SRT_MEMBERSTATUS> map = {{"PENDING", SRT_GST_PENDING},
                                                        {"IDLE",    SRT_GST_IDLE},
                                                        {"RUNNING", SRT_GST_RUNNING},
                                                        {"BROKEN",  SRT_GST_BROKEN}
    };
};

