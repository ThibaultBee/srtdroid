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
#include "log.h"
#include "JniUtils.h"

void clearPendingException(JNIEnv *env, const char *where) {
    jthrowable exception = env->ExceptionOccurred();
    if (exception == nullptr) {
        return;
    }
    // Calling into the JVM with the exception still pending is not allowed.
    env->ExceptionClear();

    jclass objectClazz = nullptr;
    jstring description = nullptr;
    const char *chars = nullptr;

    objectClazz = env->FindClass("java/lang/Object");
    if (objectClazz != nullptr) {
        jmethodID toStringID = env->GetMethodID(objectClazz, "toString", "()Ljava/lang/String;");
        if (toStringID != nullptr) {
            description = static_cast<jstring>(env->CallObjectMethod(exception, toStringID));
        }
    }
    // FindClass, GetMethodID and toString() itself can throw: never leave that one pending.
    if (env->ExceptionCheck()) {
        env->ExceptionClear();
        description = nullptr;
    }
    if (description != nullptr) {
        chars = env->GetStringUTFChars(description, nullptr);
        if (chars == nullptr && env->ExceptionCheck()) {
            env->ExceptionClear();
        }
    }

    if (chars != nullptr) {
        LOGE("%s: Java exception, cleared: %s", where, chars);
        env->ReleaseStringUTFChars(description, chars);
    } else {
        LOGE("%s: Java exception, cleared (description unavailable)", where);
    }

    if (description != nullptr) {
        env->DeleteLocalRef(description);
    }
    if (objectClazz != nullptr) {
        env->DeleteLocalRef(objectClazz);
    }
    env->DeleteLocalRef(exception);
}
