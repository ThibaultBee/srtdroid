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

#include <jni.h>

#include <memory>
#include <mutex>
#include <unordered_map>

#include "srt/srt.h"

/**
 * Gets the JNIEnv of the current thread, attaching it if needed. Only detaches a thread it attached.
 */
class ScopedJniEnv {
public:
    explicit ScopedJniEnv(JavaVM *vm);

    ~ScopedJniEnv();

    ScopedJniEnv(const ScopedJniEnv &) = delete;

    ScopedJniEnv &operator=(const ScopedJniEnv &) = delete;

    JNIEnv *get() const { return env; }

private:
    JavaVM *vm;
    JNIEnv *env = nullptr;
    bool attached = false;
};

class CallbackContext {
public:
    JavaVM *vm;
    jobject callingSocket;
    jclass sockAddrClazz;


    /**
     * Initializes a CallbackContext. It purpores is to be pass as an opaque pointer for SRT callback.
     *
     * @param env JNI environment
     * @return a CallbackContext structure
     */
    CallbackContext(JNIEnv *env, jobject callingSocket);

    ~CallbackContext();
};

/**
 * Owns the callback contexts. libsrt only gets an opaque token, because it never tells when it
 * stops using it: the connect callback is not called on success. A context is freed by the connect
 * callback, by close() or when replaced, whichever comes first.
 */
class CallbackRegistry {
public:
    static void *newToken();

    void install(SRTSOCKET socket, void *token, std::shared_ptr<CallbackContext> context);

    std::shared_ptr<CallbackContext> find(void *token);

    std::shared_ptr<CallbackContext> take(void *token);

    void release(SRTSOCKET socket);

private:
    struct Entry {
        SRTSOCKET socket;
        std::shared_ptr<CallbackContext> context;
    };

    std::mutex mutex;
    std::unordered_map<void *, Entry> byToken;
    std::unordered_map<SRTSOCKET, void *> tokenBySocket;
};
