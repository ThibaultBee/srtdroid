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
#include <atomic>
#include <cstdint>

#include "log.h"
#include "Models/Models.h"
#include "CallbackContext.h"
#include "JniUtils.h"


ScopedJniEnv::ScopedJniEnv(JavaVM *vm) : vm(vm) {
    jint res = vm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (res == JNI_EDETACHED) {
        if (vm->AttachCurrentThread(&env, nullptr) == JNI_OK) {
            attached = true;
        } else {
            LOGE("Failed to attach current thread");
            env = nullptr;
        }
    } else if (res != JNI_OK) {
        LOGE("Failed to get env (%d)", res);
        env = nullptr;
    }
}

ScopedJniEnv::~ScopedJniEnv() {
    if (attached) {
        // Detaching with an exception pending aborts under CheckJNI.
        if (env != nullptr) {
            clearPendingException(env, "ScopedJniEnv");
        }
        vm->DetachCurrentThread();
    }
}

CallbackContext::CallbackContext(JNIEnv *env, jobject callingSocket) {
    env->GetJavaVM(&(this->vm));

    this->sockAddrClazz = static_cast<jclass>(env->NewGlobalRef(
            env->FindClass(INETSOCKETADDRESS_CLASS)));
    this->callingSocket = env->NewGlobalRef(callingSocket);
}

CallbackContext::~CallbackContext() {
    ScopedJniEnv scopedEnv(vm);
    JNIEnv *env = scopedEnv.get();
    if (env != nullptr) {
        env->DeleteGlobalRef(this->callingSocket);
        env->DeleteGlobalRef(this->sockAddrClazz);
    } else {
        LOGE("CallbackContext: no env, global references leaked");
    }
}

void *CallbackRegistry::newToken() {
    // Starts at 1 so a token is never nullptr.
    static std::atomic<uintptr_t> nextToken{1};
    return reinterpret_cast<void *>(nextToken.fetch_add(1));
}

void CallbackRegistry::install(SRTSOCKET socket, void *token,
                               std::shared_ptr<CallbackContext> context) {
    std::shared_ptr<CallbackContext> replaced;
    {
        std::lock_guard<std::mutex> lock(mutex);
        auto previous = tokenBySocket.find(socket);
        if (previous != tokenBySocket.end()) {
            auto entry = byToken.find(previous->second);
            if (entry != byToken.end()) {
                replaced = std::move(entry->second.context);
                byToken.erase(entry);
            }
            previous->second = token;
        } else {
            tokenBySocket.emplace(socket, token);
        }
        byToken.emplace(token, Entry{socket, std::move(context)});
    }
    // The replaced context, if any, is deleted outside the lock.
}

std::shared_ptr<CallbackContext> CallbackRegistry::find(void *token) {
    std::lock_guard<std::mutex> lock(mutex);
    auto entry = byToken.find(token);
    if (entry == byToken.end()) {
        return nullptr;
    }
    return entry->second.context;
}

std::shared_ptr<CallbackContext> CallbackRegistry::take(void *token) {
    std::lock_guard<std::mutex> lock(mutex);
    auto entry = byToken.find(token);
    if (entry == byToken.end()) {
        return nullptr;
    }
    std::shared_ptr<CallbackContext> context = std::move(entry->second.context);
    tokenBySocket.erase(entry->second.socket);
    byToken.erase(entry);
    return context;
}

void CallbackRegistry::release(SRTSOCKET socket) {
    std::shared_ptr<CallbackContext> released;
    {
        std::lock_guard<std::mutex> lock(mutex);
        auto previous = tokenBySocket.find(socket);
        if (previous == tokenBySocket.end()) {
            return;
        }
        auto entry = byToken.find(previous->second);
        if (entry != byToken.end()) {
            released = std::move(entry->second.context);
            byToken.erase(entry);
        }
        tokenBySocket.erase(previous);
    }
    // Deleted outside the lock.
}
