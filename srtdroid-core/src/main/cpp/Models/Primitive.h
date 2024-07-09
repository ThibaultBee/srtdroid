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

class Primitive {
public:
    static jobject newJavaLong(JNIEnv *env, int64_t val) {
        jclass longClazz = env->FindClass(LONG_CLASS);
        if (!longClazz) {
            LOGE("Can't find Long class");
            return nullptr;
        }
        jmethodID longConstructorMethod = env->GetMethodID(longClazz, "<init>", "(J)V");
        if (!longConstructorMethod) {
            LOGE("Can't find Long constructor");
            return nullptr;
        }
        return env->NewObject(longClazz, longConstructorMethod, val);
    }

    static jobject newJavaBoolean(JNIEnv *env, bool val) {
        jclass boolClazz = env->FindClass(BOOLEAN_CLASS);
        if (!boolClazz) {
            LOGE("Can't find Boolean class");
            return nullptr;
        }
        jmethodID booleanConstructorMethod = env->GetMethodID(boolClazz, "<init>", "(Z)V");
        if (!booleanConstructorMethod) {
            LOGE("Can't find Boolean constructor");
            return nullptr;
        }
        return env->NewObject(boolClazz, booleanConstructorMethod, val);
    }

    static jobject newJavaInt(JNIEnv *env, int val) {
        jclass intClazz = env->FindClass(INT_CLASS);
        if (!intClazz) {
            LOGE("Can't find Integer class");
            return nullptr;
        }
        jmethodID integerConstructorMethod = env->GetMethodID(intClazz, "<init>", "(I)V");
        if (!integerConstructorMethod) {
            LOGE("Can't find Integer constructor");
            return nullptr;
        }
        return env->NewObject(intClazz, integerConstructorMethod, val);
    }

};