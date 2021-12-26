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

class ErrorType {
public:
    inline static const char *clazzIdentifier = ERRORTYPE_CLASS;
    inline static SRT_ERRNO fallbackError = SRT_EUNKNOWN;
    inline static map <string, SRT_ERRNO> map = {{"EUNKNOWN",        SRT_EUNKNOWN},
                                                 {"SUCCESS",         SRT_SUCCESS},
                                                 {"ECONNSETUP",      SRT_ECONNSETUP},
                                                 {"ENOSERVER",       SRT_ENOSERVER},
                                                 {"ECONNREJ",        SRT_ECONNREJ},
                                                 {"ESOCKFAIL",       SRT_ESOCKFAIL},
                                                 {"ESECFAIL",        SRT_ESECFAIL},
                                                 {"ESCLOSED",        SRT_ESCLOSED},
                                                 {"ECONNFAIL",       SRT_ECONNFAIL},
                                                 {"ECONNLOST",       SRT_ECONNLOST},
                                                 {"ENOCONN",         SRT_ENOCONN},
                                                 {"ERESOURCE",       SRT_ERESOURCE},
                                                 {"ETHREAD",         SRT_ETHREAD},
                                                 {"ENOBUF",          SRT_ENOBUF},
                                                 {"ESYSOBJ",         SRT_ESYSOBJ},
                                                 {"EFILE",           SRT_EFILE},
                                                 {"EINVRDOFF",       SRT_EINVRDOFF},
                                                 {"ERDPERM",         SRT_ERDPERM},
                                                 {"EINVWROFF",       SRT_EINVWROFF},
                                                 {"EWRPERM",         SRT_EWRPERM},
                                                 {"EINVOP",          SRT_EINVOP},
                                                 {"EBOUNDSOCK",      SRT_EBOUNDSOCK},
                                                 {"ECONNSOCK",       SRT_ECONNSOCK},
                                                 {"EINVPARAM",       SRT_EINVPARAM},
                                                 {"EINVSOCK",        SRT_EINVSOCK},
                                                 {"EUNBOUNDSOCK",    SRT_EUNBOUNDSOCK},
                                                 {"ENOLISTEN",       SRT_ENOLISTEN},
                                                 {"ERDVNOSERV",      SRT_ERDVNOSERV},
                                                 {"ERDVUNBOUND",     SRT_ERDVUNBOUND},
                                                 {"EINVALMSGAPI",    SRT_EINVALMSGAPI},
                                                 {"EINVALBUFFERAPI", SRT_EINVALBUFFERAPI},
                                                 {"EDUPLISTEN",      SRT_EDUPLISTEN},
                                                 {"ELARGEMSG",       SRT_ELARGEMSG},
                                                 {"EINVPOLLID",      SRT_EINVPOLLID},
                                                 {"EPOLLEMPTY",      SRT_EPOLLEMPTY},
                                                 {"EBINDCONFLICT",   SRT_EBINDCONFLICT},
                                                 {"EASYNCFAIL",      SRT_EASYNCFAIL},
                                                 {"EASYNCSND",       SRT_EASYNCSND},
                                                 {"EASYNCRCV",       SRT_EASYNCRCV},
                                                 {"ETIMEOUT",        SRT_ETIMEOUT},
                                                 {"ECONGEST",        SRT_ECONGEST},
                                                 {"EPEERERR",        SRT_EPEERERR}
    };
};
