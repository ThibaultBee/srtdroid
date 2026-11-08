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

/**
 * Returns `exception.toString()`, or nullptr when it cannot be had. Must be called with no
 * exception pending, and leaves none pending: FindClass, GetMethodID and toString() itself can all
 * throw.
 */
static jstring describeException(JNIEnv *env, jthrowable exception) {
    jstring description = nullptr;
    jclass objectClazz = env->FindClass("java/lang/Object");
    if (objectClazz != nullptr) {
        jmethodID toStringID = env->GetMethodID(objectClazz, "toString", "()Ljava/lang/String;");
        if (toStringID != nullptr) {
            description = static_cast<jstring>(env->CallObjectMethod(exception, toStringID));
        }
    }
    if (env->ExceptionCheck()) {
        env->ExceptionClear();
        // Only reachable with a result when toString() threw, and a call that throws gives no
        // usable return value, so it is dropped.
        description = nullptr;
    }
    if (objectClazz != nullptr) {
        env->DeleteLocalRef(objectClazz);
    }
    return description;
}

void clearPendingException(JNIEnv *env, const char *where) {
    if (!env->ExceptionCheck()) {
        return;
    }
    jthrowable exception = env->ExceptionOccurred();
    // Describing the exception calls into the JVM, which is not allowed while it is pending.
    env->ExceptionClear();

    jstring description = describeException(env, exception);
    const char *chars = nullptr;
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
    env->DeleteLocalRef(exception);
}
