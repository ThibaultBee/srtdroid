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

class List {
public:
    static jobject newJavaList(JNIEnv *env) {
        jclass listClazz = env->FindClass("java/util/ArrayList");
        if (!listClazz) {
            LOGE("Can't get List class");
            return nullptr;
        }

        jmethodID emptyListMethodID = env->GetMethodID(listClazz, "<init>", "()V");
        if (!emptyListMethodID) {
            LOGE("Can't get emptyList()");
            env->DeleteLocalRef(listClazz);
            return nullptr;
        }

        jobject list = env->NewObject(listClazz, emptyListMethodID);

        env->DeleteLocalRef(listClazz);

        return list;
    }

    static int getSize(JNIEnv *env, jobject list) {
        jclass listClazz = env->GetObjectClass(list);
        if (!listClazz) {
            LOGE("Can't get List object class");
            return 0;
        }

        jmethodID listSizeMethodID = env->GetMethodID(listClazz, "size", "()I");
        if (!listSizeMethodID) {
            LOGE("Can't get List size methodID");
            env->DeleteLocalRef(listClazz);
            return 0;
        }

        env->DeleteLocalRef(listClazz);

        return reinterpret_cast<int>(env->CallIntMethod(list, listSizeMethodID));
    }

    static jobject get(JNIEnv *env, jobject list, int i) {
        jclass listClazz = env->GetObjectClass(list);
        if (!listClazz) {
            LOGE("Can't get List object class");
            return nullptr;
        }

        jmethodID listGetMethodID = env->GetMethodID(listClazz, "get", "(I)Ljava/lang/Object;");
        if (!listGetMethodID) {
            LOGE("Can't get List get method");
            env->DeleteLocalRef(listClazz);
            return nullptr;
        }

        env->DeleteLocalRef(listClazz);

        return env->CallObjectMethod(list, listGetMethodID, i);
    }

    static jboolean add(JNIEnv *env, jobject list, jobject object) {
        jclass listClazz = env->GetObjectClass(list);
        if (!listClazz) {
            LOGE("Can't get List object class");
            return static_cast<jboolean>(false);
        }

        jmethodID listAddMethodID = env->GetMethodID(listClazz, "add", "(Ljava/lang/Object;)Z");
        if (!listAddMethodID) {
            LOGE("Can't get List add method");
            env->DeleteLocalRef(listClazz);
            return static_cast<jboolean>(false);
        }

        env->DeleteLocalRef(listClazz);

        return env->CallBooleanMethod(list, listAddMethodID, object);
    }
};