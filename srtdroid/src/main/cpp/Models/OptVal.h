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

#include "Primitive.h"

class OptVal {
private:
    static const char *
    getClassName(JNIEnv *env, jobject object) {
        jclass objectClazz = env->GetObjectClass(object);
        if (!objectClazz) {
            LOGE("Can't get object class");
            return nullptr;
        }

        // As object could be an Int, String,... First step is to get class object.
        jmethodID objectGetClassMethod = env->GetMethodID(objectClazz, "getClass",
                                                          "()Ljava/lang/Class;");
        if (!objectGetClassMethod) {
            LOGE("Can't get getClass method");
            env->DeleteLocalRef(objectClazz);
            return nullptr;
        }

        jobject objectClazzObject = env->CallObjectMethod(object, objectGetClassMethod);
        if (!objectClazzObject) {
            LOGE("Can't get class object");
            env->DeleteLocalRef(objectClazz);
            return nullptr;
        }

        jclass clazzClazz = env->GetObjectClass(objectClazzObject);
        if (!clazzClazz) {
            LOGE("Can't get class");
            env->DeleteLocalRef(objectClazz);
            return nullptr;
        }

        // Then get class name
        jmethodID objectClazzGetNameMethod = env->GetMethodID(clazzClazz, "getName",
                                                              "()Ljava/lang/String;");
        if (!objectClazzGetNameMethod) {
            LOGE("Can't get getName method");
            env->DeleteLocalRef(objectClazz);
            env->DeleteLocalRef(clazzClazz);
            return nullptr;
        }

        auto className = (jstring) env->CallObjectMethod(objectClazzObject,
                                                         objectClazzGetNameMethod);
        if (!className) {
            LOGE("Can't get class name");
            env->DeleteLocalRef(objectClazz);
            env->DeleteLocalRef(clazzClazz);
            return nullptr;
        }

        const char *class_name = env->GetStringUTFChars(className, nullptr);
        const char *dup_class_name = strdup(class_name);

        env->ReleaseStringUTFChars(className, class_name);

        env->DeleteLocalRef(objectClazz);
        env->DeleteLocalRef(clazzClazz);

        return dup_class_name;
    }

public:
    static void *
    getNative(JNIEnv *env, jobject optVal, int *optval_len) {
        void *srt_optval = nullptr;

        if (optval_len == nullptr) {
            LOGE("Can't get optlen");
            return nullptr;
        }

        jclass optValClazz = env->GetObjectClass(optVal);
        if (!optValClazz) {
            LOGE("Can't get OptVal class");
            return nullptr;
        }

        const char *class_name = getClassName(env, optVal);
        if (class_name == nullptr) {
            return nullptr;
        }

        if (strcmp(class_name, "java.lang.String") == 0) {
            const char *optval = env->GetStringUTFChars((jstring) optVal, nullptr);
            *optval_len = strlen(optval);
            srt_optval = strdup(optval);
            env->ReleaseStringUTFChars((jstring) optVal, optval);
        } else if (strcmp(class_name, ENUM_PACKAGE".Transtype") == 0) {
            SRT_TRANSTYPE transtype = EnumsSingleton::getInstance(env)->transType->getNativeValue(
                    env,
                    optVal);
            *optval_len = sizeof(transtype);
            srt_optval = malloc(static_cast<size_t>(*optval_len));
            *(SRT_TRANSTYPE *) srt_optval = transtype;
        } else if (strcmp(class_name, ENUM_PACKAGE".KMState") == 0) {
            SRT_KM_STATE kmstate = EnumsSingleton::getInstance(env)->kmState->getNativeValue(env,
                                                                                             optVal);
            *optval_len = sizeof(kmstate);
            srt_optval = malloc(static_cast<size_t>(*optval_len));
            *(SRT_KM_STATE *) srt_optval = kmstate;
        } else if (strcmp(class_name, "java.lang.Long") == 0) {
            jmethodID longValueMethod = env->GetMethodID(optValClazz, "longValue", "()J");
            if (!longValueMethod) {
                LOGE("Can't get longValue method");
                return nullptr;
            }
            *optval_len = sizeof(int64_t);
            srt_optval = malloc(static_cast<size_t>(*optval_len));
            *(int64_t *) srt_optval = env->CallLongMethod(optVal, longValueMethod);
        } else if (strcmp(class_name, "java.lang.Integer") == 0) {
            jmethodID intValueMethod = env->GetMethodID(optValClazz, "intValue", "()I");
            if (!intValueMethod) {
                LOGE("Can't get intValue method");
                return nullptr;
            }
            *optval_len = sizeof(int);
            srt_optval = malloc(static_cast<size_t>(*optval_len));
            *(int *) srt_optval = env->CallIntMethod(optVal, intValueMethod);
        } else if (strcmp(class_name, "java.lang.Boolean") == 0) {
            jmethodID booleanValueMethod = env->GetMethodID(optValClazz, "booleanValue", "()Z");
            if (!booleanValueMethod) {
                LOGE("Can't get booleanValue method");
                return nullptr;
            }
            *optval_len = sizeof(bool);
            srt_optval = malloc(static_cast<size_t>(*optval_len));
            *(bool *) srt_optval = (env->CallBooleanMethod(optVal, booleanValueMethod) == JNI_TRUE);
        } else {
            LOGE("OptVal: unknown class %s", class_name);
        }

        free((void *) class_name);
        env->DeleteLocalRef(optValClazz);

        return srt_optval;
    }

    static jobject getJava(JNIEnv *env, int u, int level, jobject sockOpt) {
        jobject optVal = nullptr;

        int sockopt = EnumsSingleton::getInstance(env)->sockOpt->getNativeValue(env, sockOpt);
        if (sockopt < 0) {
            return nullptr;
        }

        switch (sockopt) {
            case SRTO_INPUTBW:
            case SRTO_MININPUTBW:
            case SRTO_MAXBW: {
                // Int64
                int64_t optval = 0;
                int optlen = sizeof(optval);
                if (srt_getsockopt(u, level, (SRT_SOCKOPT) sockopt, &optval, &optlen) != 0) {
                    LOGE("Can't execute long getsockopt");
                    return nullptr;
                }
                optVal = Primitive::newJavaLong(env, optval);
                break;
            }
            case SRTO_MESSAGEAPI:
            case SRTO_NAKREPORT:
            case SRTO_RCVSYN:
            case SRTO_RENDEZVOUS:
            case SRTO_REUSEADDR:
            case SRTO_SENDER:
            case SRTO_SNDSYN:
            case SRTO_ENFORCEDENCRYPTION:
            case SRTO_TLPKTDROP:
            case SRTO_DRIFTTRACER:
            case SRTO_TSBPDMODE: {
                // Boolean
                bool optval = false;
                int optlen = sizeof(bool);
                if (srt_getsockopt(u, level, (SRT_SOCKOPT) sockopt, (void *) &optval, &optlen) !=
                    0) {
                    LOGE("Can't execute bool getsockopt");
                    return nullptr;
                }
                optVal = Primitive::newJavaBoolean(env, optval);
                break;
            }
            case SRTO_PACKETFILTER:
            case SRTO_PASSPHRASE:
            case SRTO_BINDTODEVICE:
            case SRTO_STREAMID: {
                // String
                const char optval[512] = {0};
                int optlen = sizeof(optval);
                if (srt_getsockopt(u, level, (SRT_SOCKOPT) sockopt, (void *) &optval, &optlen) !=
                    0) {
                    LOGE("Can't execute string getsockopt");
                    return nullptr;
                }
                optVal = env->NewStringUTF(optval);
                break;
            }
            case SRTO_KMSTATE:
            case SRTO_RCVKMSTATE:
            case SRTO_SNDKMSTATE: {
                // KMState
                SRT_KM_STATE optval;
                int optlen = sizeof(SRT_KM_STATE);
                if (srt_getsockopt(u, level, (SRT_SOCKOPT) sockopt, (void *) &optval, &optlen) !=
                    0) {
                    LOGE("Can't execute SRT_KM_STATE getsockopt");
                    return nullptr;
                }
                optVal = EnumsSingleton::getInstance(env)->kmState->getJavaValue(env, optval);
                break;
            }
            case SRTO_TRANSTYPE: {
                // Transtype
                SRT_TRANSTYPE optval;
                int optlen = sizeof(SRT_TRANSTYPE);
                if (srt_getsockopt(u, level, (SRT_SOCKOPT) sockopt, (void *) &optval, &optlen) !=
                    0) {
                    LOGE("Can't execute SRT_TRANSTYPE getsockopt");
                    return nullptr;
                }
                optVal = EnumsSingleton::getInstance(env)->transType->getJavaValue(env, optval);
                break;
            }
            default: {
                // Int
                int optval = 0;
                int optlen = sizeof(int);
                if (srt_getsockopt(u, level, (SRT_SOCKOPT) sockopt, (void *) &optval, &optlen) !=
                    0) {
                    LOGE("Can't execute int getsockopt");
                    return nullptr;
                }
                optVal = Primitive::newJavaInt(env, optval);
                break;
            }
        }

        return optVal;
    }
};