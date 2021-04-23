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

class CallbackContext {
public:
    JavaVM *vm;
    jobject callingSocket;
    jclass sockAddrClazz;
    jclass errorTypeClazz;


    /**
     * Initializes a CallbackContext. It purpores is to be pass as an opaque pointer for SRT callback.
     *
     * @param env JNI environment
     * @return a CallbackContext structure
     */
    CallbackContext(JNIEnv *env, jobject callingSocket);

    ~CallbackContext();
};