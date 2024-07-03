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

class EpollEvent {
public:
    static SRT_EPOLL_EVENT *getNative(JNIEnv *env, jobject epollEvent, SRT_EPOLL_EVENT *srt_event) {
        jclass epollEventClazz = env->GetObjectClass(epollEvent);
        if (!epollEventClazz) {
            LOGE("Can't get EpollEvent class");
            return nullptr;
        }

        jfieldID
                socketField = env->GetFieldID(epollEventClazz, "socket", "L"
                                                                         SOCKET_CLASS
                                                                         ";");
        if (!socketField) {
            LOGE("Can't get Socket field");
            env->DeleteLocalRef(epollEventClazz);
            return nullptr;
        }
        jobject srtSocket = env->GetObjectField(epollEvent, socketField);
        srt_event->fd = Socket::getNative(env, srtSocket);

        jfieldID
                epollOptsField = env->GetFieldID(epollEventClazz, "events", "L" LIST_CLASS ";");
        if (!epollOptsField) {
            LOGE("Can't get events field");
            env->DeleteLocalRef(epollEventClazz);
            return nullptr;
        }
        jobject epollOpts = static_cast<jobjectArray>(env->GetObjectField(epollEvent,
                                                                          epollOptsField));
        srt_event->events = EpollOpts::getNative(env, epollOpts);

        env->DeleteLocalRef(epollEventClazz);

        return srt_event;
    }

    static jobject getJava(JNIEnv *env, SRT_EPOLL_EVENT epoll_event) {
        jclass clazz = env->FindClass(EPOLLEVENT_CLASS);
        if (!clazz) {
            LOGE("Can't get EpollEvent class");
            return nullptr;
        }

        jmethodID constructor = env->GetMethodID(clazz, "<init>", "(L" SOCKET_CLASS ";L" LIST_CLASS ";)V");
        if (!constructor) {
            LOGE("Can't get EpollEvent constructor");
            return nullptr;
        }

        jobject srtSocket = Socket::getJava(env, epoll_event.fd);
        jobject jevents = EpollOpts::getJava(env, epoll_event.events);

        jobject epollEvent = env->NewObject(clazz, constructor, srtSocket, jevents);

        return epollEvent;
    }
};