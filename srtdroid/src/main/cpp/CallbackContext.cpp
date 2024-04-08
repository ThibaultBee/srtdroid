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
#include "Models/Models.h"
#include "CallbackContext.h"


CallbackContext::CallbackContext(JNIEnv *env, jobject callingSocket) {
    env->GetJavaVM(&(this->vm));

    this->sockAddrClazz = static_cast<jclass>(env->NewGlobalRef(
            env->FindClass(INETSOCKETADDRESS_CLASS)));
    this->callingSocket = env->NewGlobalRef(callingSocket);
}

CallbackContext::~CallbackContext() {
    JNIEnv *env = nullptr;

    vm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (env != nullptr) {
        env->DeleteGlobalRef(this->callingSocket);
        env->DeleteGlobalRef(this->sockAddrClazz);
    }
}

