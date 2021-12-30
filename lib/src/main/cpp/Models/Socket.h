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

class Socket {
public:
    static SRTSOCKET getNative(JNIEnv *env, jobject srtSocket) {
        jclass socketClazz = env->GetObjectClass(srtSocket);
        if (!socketClazz) {
            LOGE("Can't get Socket class");
            return SRT_INVALID_SOCK;
        }

        jfieldID srtSocketField = env->GetFieldID(socketClazz, "srtsocket", "I");
        if (!srtSocketField) {
            LOGE("Can't get srtsocket field");
            env->DeleteLocalRef(socketClazz);
            return SRT_INVALID_SOCK;
        }

        jint srtsocket = env->GetIntField(srtSocket, srtSocketField);

        env->DeleteLocalRef(socketClazz);

        return srtsocket;
    }

    static jobject getJava(JNIEnv *env, SRTSOCKET srtsocket) {
        jclass srtSocketClazz = env->FindClass(SOCKET_CLASS);
        jobject inetSocketAddress = getJava(env, srtSocketClazz, srtsocket);
        env->DeleteLocalRef(srtSocketClazz);
        return inetSocketAddress;
    }

    static jobject getJava(JNIEnv *env, jclass clazz, SRTSOCKET srtsocket) {
        if (!clazz) {
            LOGE("Can't find Srt Socket class");
            return nullptr;
        }

        jmethodID srtSocketConstructorMethod = env->GetMethodID(clazz, "<init>", "(I)V");
        if (!srtSocketConstructorMethod) {
            LOGE("Can't get SrtSocket constructor");
            return nullptr;
        }

        jobject srtSocket = env->NewObject(clazz, srtSocketConstructorMethod, srtsocket);
        return srtSocket;
    }

    static void setJava(JNIEnv *env, jobject srtSocket, SRTSOCKET srtsocket) {
        jclass socketClazz = env->GetObjectClass(srtSocket);
        if (!socketClazz) {
            LOGE("Can't get Socket class");
            return;
        }

        jfieldID srtSocketField = env->GetFieldID(socketClazz, "srtsocket", "I");
        if (!srtSocketField) {
            LOGE("Can't get srtsocket field");
            env->DeleteLocalRef(socketClazz);
            return;
        }

        env->SetIntField(srtSocket, srtSocketField, srtsocket);

        env->DeleteLocalRef(socketClazz);
    }

    static SRTSOCKET *getNativeSockets(JNIEnv *env, jobject srtSocketList, int *nSockets) {
        *nSockets = List::getSize(env, srtSocketList);
        if (*nSockets == 0)
            return nullptr;

        SRTSOCKET *srtsocket = (SRTSOCKET *) malloc(
                reinterpret_cast<size_t>(*nSockets * sizeof(SRTSOCKET)));

        for (int i = 0; i < *nSockets; i++) {
            jobject srtSocket = List::get(env, srtSocketList, i);
            srtsocket[i] = Socket::getNative(env, srtSocket);
        }

        return srtsocket;
    }
};