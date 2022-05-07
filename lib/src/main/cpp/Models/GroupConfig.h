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

#include "../Enums/Enums.h"
#include "Models.h"
#include "InetSocketAddress.h"

class GroupConfig {
public:
    static SRT_SOCKGROUPCONFIG *
    getNative(JNIEnv *env, jobject groupConfig) {
        SRT_SOCKGROUPCONFIG *srt_groupconfig = nullptr;

        if (groupConfig == nullptr)
            return nullptr;

        jclass groupConfigClazz = env->GetObjectClass(groupConfig);
        if (!groupConfigClazz) {
            LOGE("Can't get GroupConfig class");
            return nullptr;
        }

        jfieldID socketField = env->GetFieldID(groupConfigClazz, "socket", "L" SOCKET_CLASS ";");
        if (!socketField) {
            LOGE("Can't get socket field");
            env->DeleteLocalRef(groupConfigClazz);
            return nullptr;
        }

        jfieldID srcAddrField = env->GetFieldID(groupConfigClazz, "src",
                                                "L" INETSOCKETADDRESS_CLASS ";");
        if (!srcAddrField) {
            LOGE("Can't get src field");
            env->DeleteLocalRef(groupConfigClazz);
            return nullptr;
        }

        jfieldID peerAddrField = env->GetFieldID(groupConfigClazz, "peer",
                                                 "L" INETSOCKETADDRESS_CLASS ";");
        if (!peerAddrField) {
            LOGE("Can't get peer field");
            env->DeleteLocalRef(groupConfigClazz);
            return nullptr;
        }

        jfieldID weightField = env->GetFieldID(groupConfigClazz, "weight", "S");
        if (!weightField) {
            LOGE("Can't get weight field");
            env->DeleteLocalRef(groupConfigClazz);
            return nullptr;
        }

        jfieldID
                errorTypeField = env->GetFieldID(groupConfigClazz, "error", "L"
        ERRORTYPE_CLASS
        ";");
        if (!errorTypeField) {
            LOGE("Can't get error type field");
            env->DeleteLocalRef(groupConfigClazz);
            return nullptr;
        }

        jfieldID tokenField = env->GetFieldID(groupConfigClazz, "token", "I");
        if (!tokenField) {
            LOGE("Can't get token field");
            env->DeleteLocalRef(groupConfigClazz);
            return nullptr;
        }

        srt_groupconfig = (SRT_SOCKGROUPCONFIG *) malloc(sizeof(SRT_SOCKGROUPCONFIG));
        if (srt_groupconfig != nullptr) {
            srt_groupconfig->id = Socket::getNative(env,
                                                    env->GetObjectField(groupConfig, socketField);
            InetSocketAddress::getNative(env, env->GetObjectField(groupConfig, srcAddrField),
                                         srt_groupconfig->srcaddr);
            InetSocketAddress::getNative(env, env->GetObjectField(groupConfig, peerAddrField),
                                         srt_groupconfig->peeraddr);
            srt_groupconfig->weight = env->GetShortField(groupConfig, weightField);
            srt_groupconfig->errorcode = EnumsSingleton::getInstance(
                    env)->errorType->getNativeValue(env,
                                                    env->GetObjectField(groupConfig,
                                                                        errorTypeField));
            srt_groupconfig->token = env->GetIntField(groupConfig, tokenField);
        }

        env->DeleteLocalRef(groupConfigClazz);

        return srt_groupconfig;
    }
};