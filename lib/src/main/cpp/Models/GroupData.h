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
#include "../Enums/EnumsSingleton.h"
#include "Models.h"
#include "InetSocketAddress.h"
#include "Socket.h"

class GroupData {
public:
    static SRT_SOCKGROUPDATA *
    getNative(JNIEnv *env, jobject groupData) {
        SRT_SOCKGROUPDATA *srt_groupdata = nullptr;

        if (groupData == nullptr)
            return nullptr;

        jclass groupDataClazz = env->GetObjectClass(groupData);
        if (!groupDataClazz) {
            LOGE("Can't get GroupData class");
            return nullptr;
        }

        jfieldID socketField = env->GetFieldID(groupDataClazz, "socket", "L" SOCKET_CLASS ";");
        if (!socketField) {
            LOGE("Can't get socket field");
            env->DeleteLocalRef(groupDataClazz);
            return nullptr;
        }

        jfieldID peerAddrField = env->GetFieldID(groupDataClazz, "peer",
                                                 "L" INETSOCKETADDRESS_CLASS ";");
        if (!peerAddrField) {
            LOGE("Can't get peer field");
            env->DeleteLocalRef(groupDataClazz);
            return nullptr;
        }

        jfieldID sockStatusField = env->GetFieldID(groupDataClazz, "sockStatus",
                                                   "L"
                                                   SOCKSTATUS_CLASS
                                                   ";");
        if (!sockStatusField) {
            LOGE("Can't get sock status field");
            env->DeleteLocalRef(groupDataClazz);
            return nullptr;
        }

        jfieldID weightField = env->GetFieldID(groupDataClazz, "weight", "S");
        if (!weightField) {
            LOGE("Can't get weight field");
            env->DeleteLocalRef(groupDataClazz);
            return nullptr;
        }

        jfieldID memberStatusField = env->GetFieldID(groupDataClazz, "memberStatus", "L"
                                                                                     MEMBERSTATUS_CLASS
                                                                                     ";");
        if (!memberStatusField) {
            LOGE("Can't get member status field");
            env->DeleteLocalRef(groupDataClazz);
            return nullptr;
        }

        jfieldID resultField = env->GetFieldID(groupDataClazz, "result", "I");
        if (!resultField) {
            LOGE("Can't get result field");
            env->DeleteLocalRef(groupDataClazz);
            return nullptr;
        }

        jfieldID tokenField = env->GetFieldID(groupDataClazz, "token", "I");
        if (!tokenField) {
            LOGE("Can't get token field");
            env->DeleteLocalRef(groupDataClazz);
            return nullptr;
        }

        srt_groupdata = (SRT_SOCKGROUPDATA *) malloc(sizeof(SRT_SOCKGROUPDATA));
        if (srt_groupdata != nullptr) {
            srt_groupdata->id = Socket::getNative(env,
                                                  env->GetObjectField(groupData, socketField));
            InetSocketAddress::getNative(env, env->GetObjectField(groupData, peerAddrField),
                                         &(srt_groupdata->peeraddr));
            srt_groupdata->sockstate = EnumsSingleton::getInstance(env)->sockStatus->getNativeValue(env,
                                                     env->GetObjectField(groupData,
                                                                         sockStatusField));
            srt_groupdata->weight = env->GetShortField(groupData, weightField);
            srt_groupdata->memberstate = EnumsSingleton::getInstance(
                    env)->memberStatus->getNativeValue(env,
                                                       env->GetObjectField(groupData,
                                                                           memberStatusField));
            srt_groupdata->result = env->GetIntField(groupData, resultField);
            srt_groupdata->token = env->GetIntField(groupData, tokenField);
        }

        env->DeleteLocalRef(groupDataClazz);

        return srt_groupdata;
    }

    static jobject getJava(JNIEnv *env, SRT_SOCKGROUPDATA srt_groupdata) {
        jclass groupDataClazz = env->FindClass(GROUPDATA_CLASS);
        if (!groupDataClazz) {
            LOGE("Can't find GroupData class");
            return nullptr;
        }

        jmethodID groupDataConstructor = env->GetMethodID(groupDataClazz, "<init>",
                                                          "(L" SOCKET_CLASS ";L" INETSOCKETADDRESS_CLASS ";L" SOCKSTATUS_CLASS ";SL" MEMBERSTATUS_CLASS ";II)V");
        if (!groupDataConstructor) {
            LOGE("Can't get GroupData constructor");
            env->DeleteLocalRef(groupDataClazz);
            return nullptr;
        }

        jobject groupData = env->NewObject(groupDataClazz, groupDataConstructor,
                                           Socket::getJava(env, srt_groupdata.id),
                                           InetSocketAddress::getJava(env,
                                                                      &(srt_groupdata.peeraddr)),
                                           EnumsSingleton::getInstance(
                                                   env)->sockStatus->getJavaValue(env,
                                                                                  srt_groupdata.sockstate),
                                           (short) srt_groupdata.weight,
                                           EnumsSingleton::getInstance(
                                                   env)->memberStatus->getJavaValue(env,
                                                                                    srt_groupdata.memberstate),
                                           srt_groupdata.result,
                                           srt_groupdata.token);

        env->DeleteLocalRef(groupDataClazz);

        return groupData;
    }
};