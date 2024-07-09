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

class Pair {
public:
    static jobject newJavaPair(JNIEnv *env, jobject first, jobject second) {
        jclass pairClazz = env->FindClass(PAIR_CLASS);
        if (!pairClazz) {
            LOGE("Can't get Pair class");
            return nullptr;
        }

        jmethodID pairConstructorMethod = env->GetMethodID(pairClazz, "<init>",
                                                           "(Ljava/lang/Object;Ljava/lang/Object;)V");
        if (!pairConstructorMethod) {
            LOGE("Can't get Pair constructor");
            env->DeleteLocalRef(pairClazz);
            return nullptr;
        }

        jobject pair = env->NewObject(pairClazz, pairConstructorMethod, first, second);

        env->DeleteLocalRef(pairClazz);

        return pair;
    }
};