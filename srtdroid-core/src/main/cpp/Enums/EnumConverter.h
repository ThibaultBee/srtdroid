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
#include <string>
#include "Bimap.h"
#include "../log.h"
#include <cerrno>

using namespace std;

template<typename ValueType>
class EnumConverter {
private:
    Bimap<string, ValueType> biMap;
    ValueType defaultError;
    const char *clazzIdentifier;
    const char *clazzSignature;
    jclass clazz;
    JavaVM *vm;

    const char *getJavaFieldName(JNIEnv *env, jobject enumValue) {
        if (!clazz) {
            LOGE("Can't get enum class for %s", clazzIdentifier);
            return nullptr;
        }

        jmethodID enumNameMethod = env->GetMethodID(clazz, "name", "()Ljava/lang/String;");
        if (!enumNameMethod) {
            LOGE("Can't get enum name method");
            return nullptr;
        }


        auto enumField = (jstring) env->CallObjectMethod(enumValue, enumNameMethod);
        if (!enumField) {
            LOGE("Can't get Java enum field");
            return nullptr;
        }

        const char *enum_field = env->GetStringUTFChars(enumField, nullptr);
        if (!enum_field) {
            LOGE("Can't get native enum field");
            return nullptr;
        }

        const char *dup_enum_field = strdup(enum_field);

        env->ReleaseStringUTFChars(enumField, enum_field);

        return dup_enum_field;
    }

    void populatesBimap(map<string, ValueType> map) {
        for (const auto &pair : map) {
            biMap.set(pair.first, pair.second);
        }
    }

public:
    EnumConverter(JNIEnv *env, map<string, ValueType> map, ValueType fallbackError,
            const char *clazzIdentifier) : EnumConverter(map, fallbackError) {
        this->clazz = static_cast<jclass>(env->NewGlobalRef(env->FindClass(clazzIdentifier)));
        env->GetJavaVM(&(this->vm));

        this->clazzIdentifier = clazzIdentifier;
        this->clazzSignature = (char *) malloc(strlen(clazzIdentifier) + 3);
        sprintf((char *) this->clazzSignature, "L%s;", clazzIdentifier);
    }

    EnumConverter(map <string, ValueType> map, ValueType fallbackError) {
        populatesBimap(map);
        this->defaultError = fallbackError;
    }

    ~EnumConverter() {
        JNIEnv *env = nullptr;

        vm->GetEnv((void **) &env, JNI_VERSION_1_6);
        if (env != nullptr) {
            env->DeleteGlobalRef(this->clazz);
        }
        if (this->clazzSignature) {
            free((void *) this->clazzSignature);
        }
    }

    ValueType getNativeValue(JNIEnv *env, const jobject object) {
        const char *field_name = getJavaFieldName(env, object);
        if (!field_name) {
            LOGE("Can't get Java field for %s", clazzIdentifier);
            return defaultError;
        }

        auto value = biMap.valueForKey(field_name);
        free((void *) field_name);

        return value;
    }

    jobject getJavaValue(JNIEnv *env, const ValueType value) {
        if (!clazz) {
            LOGE("Can't get %s class", clazzIdentifier);
            return nullptr;
        }

        auto fieldName = biMap.keysForValue(value).begin();
        jfieldID enumField = env->GetStaticFieldID(clazz, fieldName->c_str(),
                clazzSignature);
        if (!enumField) {
            LOGE("Can't get field for %s", clazzIdentifier);
            return nullptr;
        }

        jobject object = env->GetStaticObjectField(clazz, enumField);

        return object;
    }
};
