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

#include "List.h"

class EpollFlags {
public:
    static int getNative(JNIEnv *env, jobject epollFlagList) {
        int nFlags = List::getSize(env, epollFlagList);
        int flags = 0;

        for (int i = 0; i < nFlags; i++) {
            jobject epollFlag = List::get(env, epollFlagList, i);
            flags |= EnumsSingleton::getInstance(env)->epollFlag->getNativeValue(env, epollFlag);
        }

        return flags;
    }

    static jobject getJava(JNIEnv *env, int epoll_flags) {
        int max = SRT_EPOLL_ENABLE_OUTPUTCHECK;
        int epoll_flag;

        jclass epollFlagClazz = env->FindClass(EPOLLFLAG_CLASS);
        if (!epollFlagClazz) {
            LOGE("Can't find EpollFlag class");
            return nullptr;
        }

        jobject epollFlagList = List::newJavaList(env);
        if (!epollFlagList) {
            LOGE("Can't create EpollFlag List");
            env->DeleteLocalRef(epollFlagClazz);
            return nullptr;
        }

        jobject epollFlag;

        for (int i = 0; i < max; i++) {
            epoll_flag = epoll_flags & 1 << i;
            if (epoll_flag != 0) {
                epollFlag = EnumsSingleton::getInstance(env)->epollFlag->getJavaValue(env,
                                                                                      epoll_flag);
                if (List::add(env, epollFlagList, epollFlag) == 0) {
                    LOGE("Can't add epollFlag %d", i);
                }
            }
        }

        env->DeleteLocalRef(epollFlagClazz);

        return epollFlagList;
    }
};