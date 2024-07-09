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

class MsgCtrl {
public:
    static SRT_MSGCTRL *
    getNative(JNIEnv *env, jobject msgCtrl) {
        SRT_MSGCTRL *srt_msgctrl = nullptr;

        if (msgCtrl == nullptr)
            return nullptr;

        jclass msgCtrlClazz = env->GetObjectClass(msgCtrl);
        if (!msgCtrlClazz) {
            LOGE("Can't get MsgCtrl class");
            return nullptr;
        }

        jfieldID msgCtrlFlagsField = env->GetFieldID(msgCtrlClazz, "flags", "I");
        if (!msgCtrlFlagsField) {
            LOGE("Can't get flags field");
            env->DeleteLocalRef(msgCtrlClazz);
            return nullptr;
        }

        jfieldID msgCtrlTtlField = env->GetFieldID(msgCtrlClazz, "ttl", "I");
        if (!msgCtrlTtlField) {
            LOGE("Can't get ttl field");
            env->DeleteLocalRef(msgCtrlClazz);
            return nullptr;
        }

        jfieldID msgCtrlInOrderField = env->GetFieldID(msgCtrlClazz, "inOrder", "Z");
        if (!msgCtrlInOrderField) {
            LOGE("Can't get inOrder field");
            env->DeleteLocalRef(msgCtrlClazz);
            return nullptr;
        }

        jfieldID msgCtrlBondaryField = env->GetFieldID(msgCtrlClazz, "boundary",
        "L" BOUNDARY_CLASS ";");
        if (!msgCtrlBondaryField) {
            LOGE("Can't get boundary field");
            env->DeleteLocalRef(msgCtrlClazz);
            return nullptr;
        }

        jfieldID msgCtrlSrcTimeField = env->GetFieldID(msgCtrlClazz, "srcTime", "J");
        if (!msgCtrlSrcTimeField) {
            LOGE("Can't get srcTime field");
            env->DeleteLocalRef(msgCtrlClazz);
            return nullptr;
        }

        jfieldID msgCtrlPktSeqField = env->GetFieldID(msgCtrlClazz, "pktSeq", "I");
        if (!msgCtrlPktSeqField) {
            LOGE("Can't get pktSeq field");
            env->DeleteLocalRef(msgCtrlClazz);
            return nullptr;
        }

        jfieldID msgCtrlNoField = env->GetFieldID(msgCtrlClazz, "no", "I");
        if (!msgCtrlNoField) {
            LOGE("Can't get message number field");
            env->DeleteLocalRef(msgCtrlClazz);
            return nullptr;
        }

        srt_msgctrl = (SRT_MSGCTRL *) malloc(sizeof(SRT_MSGCTRL));
        if (srt_msgctrl != nullptr) {
            srt_msgctrl->flags = env->GetIntField(msgCtrl, msgCtrlFlagsField);
            srt_msgctrl->msgttl = env->GetIntField(msgCtrl, msgCtrlTtlField);
            srt_msgctrl->inorder = env->GetBooleanField(msgCtrl, msgCtrlInOrderField);
            srt_msgctrl->boundary = EnumsSingleton::getInstance(env)->boundary->getNativeValue(env,
                                                                                               env->GetObjectField(
                                                                                                       msgCtrl,
                                                                                                       msgCtrlBondaryField));
            srt_msgctrl->srctime = (uint64_t) env->GetLongField(msgCtrl, msgCtrlSrcTimeField);
            srt_msgctrl->pktseq = env->GetIntField(msgCtrl, msgCtrlPktSeqField);
            srt_msgctrl->msgno = env->GetIntField(msgCtrl, msgCtrlNoField);
        }

        env->DeleteLocalRef(msgCtrlClazz);

        return srt_msgctrl;
    }
};