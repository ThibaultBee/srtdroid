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

class Epoll {
public:
    static int getNative(JNIEnv *env, jobject epoll) {
        jclass epollClazz = env->GetObjectClass(epoll);
        if (!epollClazz) {
            LOGE("Can't get Epoll class");
            return -1;
        }

        jfieldID eidField = env->GetFieldID(epollClazz, "eid", "I");
        if (!eidField) {
            LOGE("Can't get eid field");
            env->DeleteLocalRef(epollClazz);
            return -1;
        }

        jint eid = env->GetIntField(epoll, eidField);

        env->DeleteLocalRef(epollClazz);

        return eid;
    }

    static jobject getJava(JNIEnv *env, int eid) {
        jclass epollClazz = env->FindClass(EPOLL_CLASS);
        if (!epollClazz) {
            LOGE("Can't find Epoll class");
            return nullptr;
        }

        jmethodID epollConstructorMethod = env->GetMethodID(epollClazz, "<init>", "()V");
        if (!epollConstructorMethod) {
            LOGE("Can't get Epoll constructor");
            env->DeleteLocalRef(epollClazz);
            return nullptr;
        }

        jobject epoll = env->NewObject(epollClazz, epollConstructorMethod, eid);

        env->DeleteLocalRef(epollClazz);

        return epoll;
    }

    static void setJava(JNIEnv *env, jobject epoll, int eid) {
        jclass epollClazz = env->GetObjectClass(epoll);
        if (!epollClazz) {
            LOGE("Can't get Epoll class");
            return;
        }

        jfieldID eidField = env->GetFieldID(epollClazz, "eid", "I");
        if (!eidField) {
            LOGE("Can't get eid field");
            env->DeleteLocalRef(epollClazz);
            return;
        }

        env->SetIntField(epoll, eidField, eid);

        env->DeleteLocalRef(epollClazz);
    }
};