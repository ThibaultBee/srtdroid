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

class InetSocketAddress {
public:
    static void
    getNative(JNIEnv *env, jobject inetSocketAddress, struct sockaddr_storage *iss) {
        int size = 0;
        struct sockaddr_storage *ss = getNative(env, inetSocketAddress, &size);
        if (ss != nullptr) {
            memcpy(iss, ss, size);
            free(ss);
        }
    }

    static struct sockaddr_storage *
    getNative(JNIEnv *env, jobject inetSocketAddress, int *size) {
        // Get InetSocketAddress class
        jclass inetSocketAddressClazz = env->GetObjectClass(inetSocketAddress);
        if (!inetSocketAddressClazz) {
            LOGE("Can't get InetSocketAddress class");
            return nullptr;
        }

        // Port
        jmethodID inetSocketAddressGetPortMethod = env->GetMethodID(inetSocketAddressClazz,
                                                                    "getPort",
                                                                    "()I");
        if (!inetSocketAddressGetPortMethod) {
            LOGE("Can't get getPort method");
            env->DeleteLocalRef(inetSocketAddressClazz);
            return nullptr;
        }
        int port = env->CallIntMethod(inetSocketAddress, inetSocketAddressGetPortMethod);

        // Hostname
        jmethodID inetSocketAddressGetAddressMethod = env->GetMethodID(inetSocketAddressClazz,
                                                                       "getAddress",
                                                                       "()Ljava/net/InetAddress;");
        if (!inetSocketAddressGetAddressMethod) {
            LOGE("Can't get getAddress method");
            env->DeleteLocalRef(inetSocketAddressClazz);
            return nullptr;
        }

        jobject inetAddress = env->CallObjectMethod(inetSocketAddress,
                                                    inetSocketAddressGetAddressMethod);
        if (!inetAddress) {
            LOGE("Can't get InetAddress");
            env->DeleteLocalRef(inetSocketAddressClazz);
            return nullptr;
        }

        // Get InetAddress class
        jclass inetAddressClazz = env->GetObjectClass(inetAddress);
        if (!inetAddressClazz) {
            LOGE("Can't get InetSocketAddress class");
            env->DeleteLocalRef(inetSocketAddressClazz);
            return nullptr;
        }

        jmethodID inetAddressGetHostAddressMethod = env->GetMethodID(inetAddressClazz,
                                                                     "getHostAddress",
                                                                     "()Ljava/lang/String;");
        if (!inetAddressGetHostAddressMethod) {
            LOGE("Can't get getHostAddress method");
            env->DeleteLocalRef(inetSocketAddressClazz);
            env->DeleteLocalRef(inetAddressClazz);
            return nullptr;
        }

        auto hostName = (jstring) env->CallObjectMethod(inetAddress,
                                                        inetAddressGetHostAddressMethod);
        if (!hostName) {
            LOGE("Can't get Hostname");
            env->DeleteLocalRef(inetSocketAddressClazz);
            env->DeleteLocalRef(inetAddressClazz);
            return nullptr;
        }

        env->DeleteLocalRef(inetSocketAddressClazz);
        env->DeleteLocalRef(inetAddressClazz);

        const char *hostname = env->GetStringUTFChars(hostName, nullptr);

        // Get hostname type: IPv4 or IPv6
        struct addrinfo hint = {0};
        struct addrinfo *ai = nullptr;
        hint.ai_family = PF_UNSPEC;
        hint.ai_flags = AI_NUMERICHOST;
        char service[11] = {0};
        sprintf(service, "%d", port);

        if (getaddrinfo(hostname, service, &hint, &ai)) {
            LOGE("Invalid address %s", hostname);
            env->ReleaseStringUTFChars(hostName, hostname);
            return nullptr;
        }

        if ((ai->ai_family != AF_INET) && (ai->ai_family != AF_INET6)) {
            LOGE("Unknown family %d", ai->ai_family);
            env->ReleaseStringUTFChars(hostName, hostname);
            return nullptr;
        }

        struct sockaddr_storage *ss = static_cast<sockaddr_storage *>(malloc(
                static_cast<size_t>(ai->ai_addrlen)));
        *size = ai->ai_addrlen;
        memcpy(ss, ai->ai_addr, static_cast<size_t>(ai->ai_addrlen));

        freeaddrinfo(ai);

        env->ReleaseStringUTFChars(hostName, hostname);

        return ss;
    }

    static jobject
    getJava(JNIEnv *env, struct sockaddr_storage *ss) {
        jclass inetSocketAddressClazz = env->FindClass(INETSOCKETADDRESS_CLASS);
        jobject inetSocketAddress = getJava(env, inetSocketAddressClazz, ss);
        env->DeleteLocalRef(inetSocketAddressClazz);
        return inetSocketAddress;
    }

    static jobject
    getJava(JNIEnv *env, jclass clazz, struct sockaddr_storage *ss) {
        if (ss == nullptr) {
            return nullptr;
        }

        if (!clazz) {
            LOGE("Can't get InetSocketAddress class");
            return nullptr;
        }

        jmethodID inetSocketAddressConstructorMethod = env->GetMethodID(clazz,
                                                                        "<init>",
                                                                        "(Ljava/lang/String;I)V");
        if (!inetSocketAddressConstructorMethod) {
            LOGE("Can't get InetSocketAddress constructor");
            return nullptr;
        }

        char ip[INET6_ADDRSTRLEN] = {0};
        int port = 0;
        if (ss->ss_family == AF_INET) {
            struct sockaddr_in *sa = (struct sockaddr_in *) ss;
            if (inet_ntop(sa->sin_family, (void *) &(sa->sin_addr), ip, sizeof(ip)) ==
                nullptr) {
                LOGE("Can't convert ipv4");
            }
            port = ntohs(sa->sin_port);
        } else if (ss->ss_family == AF_INET6) {
            struct sockaddr_in6 *sa = (struct sockaddr_in6 *) ss;
            if (inet_ntop(sa->sin6_family, (void *) &(sa->sin6_addr), ip, sizeof(ip)) ==
                nullptr) {
                LOGE("Can't convert ipv6");
            }
            port = ntohs(sa->sin6_port);
        } else {
            LOGE("Unknown socket family %d", ss->ss_family);
        }

        jstring hostName = env->NewStringUTF(ip);
        jobject inetSocketAddress = env->NewObject(clazz,
                                                   inetSocketAddressConstructorMethod, hostName,
                                                   (jint) port);

        return inetSocketAddress;
    }
};