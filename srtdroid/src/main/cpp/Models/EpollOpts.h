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

class EpollOpts {
public:
    static int getNative(JNIEnv *env, jobject epollEventList) {
        int nEvents = List::getSize(env, epollEventList);
        int events = SRT_EPOLL_OPT_NONE;

        for (int i = 0; i < nEvents; i++) {
            jobject epollEvent = List::get(env, epollEventList, i);
            events |= EnumsSingleton::getInstance(env)->epollOpt->getNativeValue(env, epollEvent);
        }

        return events;
    }

    static jobject getJava(JNIEnv *env, int epoll_opts) {
        jobject list = List::newJavaList(env);
        if (!list) {
            LOGE("Can't create EpollEvent List");
            env->DeleteLocalRef(list);
            return nullptr;
        }


        for (int i = 0; i < 32; i++) {
            SRT_EPOLL_OPT epoll_opt = static_cast<SRT_EPOLL_OPT>(epoll_opts & (1 << i));
            if (epoll_opt != 0) {
                jobject epollOpts = EnumsSingleton::getInstance(env)->epollOpt->getJavaValue(env,
                                                                                             epoll_opt);
                if (List::add(env, list, epollOpts) == 0) {
                    LOGE("Can't add epollEvent %d", i);
                }
            }
        }

        return list;
    }
};