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

#include "AddressFamily.h"
#include "Boundary.h"
#include "EpollFlag.h"
#include "EpollOpt.h"
#include "ErrorType.h"
#include "GroupType.h"
#include "KMState.h"
#include "MemberStatus.h"
#include "RejectReasonCode.h"
#include "SockOpt.h"
#include "SockStatus.h"
#include "TransType.h"
#include "EnumConverter.h"
#include "srt/srt.h"

class EnumsSingleton {
private:
    EnumsSingleton(JNIEnv *env) {
        addressFamily = new EnumConverter<int>(AddressFamily::map, AddressFamily::fallbackError);
        boundary = new EnumConverter<int>(env, Boundary::map, Boundary::fallbackError,
                                          Boundary::clazzIdentifier);
        epollFlag = new EnumConverter<int>(env, EpollFlag::map, EpollFlag::fallbackError,
                                           EpollFlag::clazzIdentifier);
        epollOpt = new EnumConverter<SRT_EPOLL_OPT>(env, EpollOpt::map, EpollOpt::fallbackError,
                                                    EpollOpt::clazzIdentifier);
        errorType = new EnumConverter<SRT_ERRNO>(env, ErrorType::map, ErrorType::fallbackError,
                                                 ErrorType::clazzIdentifier);
        groupType = new EnumConverter<SRT_GROUP_TYPE>(env, GroupType::map, GroupType::fallbackError,
                                                      GroupType::clazzIdentifier);
        kmState = new EnumConverter<SRT_KM_STATE>(env, KMState::map, KMState::fallbackError,
                                                  KMState::clazzIdentifier);
        memberStatus = new EnumConverter<SRT_MEMBERSTATUS>(env, MemberStatus::map,
                                                           MemberStatus::fallbackError,
                                                           MemberStatus::clazzIdentifier);
        rejectReasonCode = new EnumConverter<SRT_REJECT_REASON>(env,
                                                                RejectReasonCode::map,
                                                                RejectReasonCode::fallbackError,
                                                                RejectReasonCode::clazzIdentifier);
        sockOpt = new EnumConverter<int>(env, SockOpt::map,
                                         SockOpt::fallbackError,
                                         SockOpt::clazzIdentifier);
        sockStatus = new EnumConverter<SRT_SOCKSTATUS>(env, SockStatus::map,
                                                       SockStatus::fallbackError,
                                                       SockStatus::clazzIdentifier);
        transType = new EnumConverter<SRT_TRANSTYPE>(env, TransType::map, TransType::fallbackError,
                                                     TransType::clazzIdentifier);
    }

    inline static EnumsSingleton *instance;

public:
    EnumConverter<int> *addressFamily;
    EnumConverter<int> *boundary;
    EnumConverter<int> *epollFlag;
    EnumConverter<SRT_EPOLL_OPT> *epollOpt;
    EnumConverter<SRT_ERRNO> *errorType;
    EnumConverter<SRT_GROUP_TYPE> *groupType;
    EnumConverter<SRT_KM_STATE> *kmState;
    EnumConverter<SRT_MEMBERSTATUS> *memberStatus;
    EnumConverter<SRT_REJECT_REASON> *rejectReasonCode;
    EnumConverter<int> *sockOpt;
    EnumConverter<SRT_SOCKSTATUS> *sockStatus;
    EnumConverter<SRT_TRANSTYPE> *transType;

    static EnumsSingleton *getInstance(JNIEnv *env) {
        if (instance == nullptr) {
            instance = new EnumsSingleton(env);
        }
        return instance;
    }
};
