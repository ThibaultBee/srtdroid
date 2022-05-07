/*
 * Copyright (C) 2022 Thibault B.
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

#include "../Enums/Enums.h"
#include "Models.h"
#include "InetSocketAddress.h"

class OptionConfig {
public:
    static SRT_SOCKOPT_CONFIG *getNative(JNIEnv *env, jobject optionConfig) {
        SRT_SOCKOPT_CONFIG *srt_optionconfig = nullptr;

        if (optionConfig == nullptr)
            return nullptr;

        jclass optionConfigClazz = env->GetObjectClass(optionConfig);
        if (!optionConfigClazz) {
            LOGE("Can't get OptionConfig class");
            return nullptr;
        }

        jfieldID ptrField = env->GetFieldID(optionConfigClazz, "ptr", "J");
        if (!ptrField) {
            LOGE("Can't get pointer field");
            env->DeleteLocalRef(optionConfigClazz);
            return nullptr;
        }

        srt_optionconfig = reinterpret_cast<SRT_SOCKOPT_CONFIG *>(env->GetLongField(optionConfig,
                                                                                    ptrField));

        env->DeleteLocalRef(optionConfigClazz);

        return srt_optionconfig;
    }
};