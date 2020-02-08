#include <string>
#include <cerrno>
#include <cstring>
#include <sys/socket.h>
#include <cstdlib>

#include "log.h"
#include "enums.h"
#include "structs.h"

#ifdef __cplusplus
extern "C" {
#endif

#define TAG "SRTJniStructs"

struct sockaddr_in *
sockaddr_inet_j2n(JNIEnv *env, jobject inetSocketAddress) {
    // Get InetSocketAddress class
    jclass inetSocketAddressClazz = env->GetObjectClass(inetSocketAddress);
    if (!inetSocketAddressClazz) {
        LOGE(TAG, "Can't get InetSocketAddress class");
        return nullptr;
    }

    // Port
    jmethodID inetSocketAddressGetPortMethod = env->GetMethodID(inetSocketAddressClazz, "getPort",
                                                                "()I");
    if (!inetSocketAddressGetPortMethod) {
        LOGE(TAG, "Can't get getPort method");
        env->DeleteLocalRef(inetSocketAddressClazz);
        return nullptr;
    }
    int port = env->CallIntMethod(inetSocketAddress, inetSocketAddressGetPortMethod);

    // Hostname
    jmethodID inetSocketAddressGetHostStringMethod = env->GetMethodID(inetSocketAddressClazz,
                                                                      "getHostString",
                                                                      "()Ljava/lang/String;");
    if (!inetSocketAddressGetHostStringMethod) {
        LOGE(TAG, "Can't get getHostString method");
        env->DeleteLocalRef(inetSocketAddressClazz);
        return nullptr;
    }

    auto hostName = (jstring) env->CallObjectMethod(inetSocketAddress,
                                                    inetSocketAddressGetHostStringMethod);
    if (!hostName) {
        LOGE(TAG, "Can't get Hostname");
        env->DeleteLocalRef(inetSocketAddressClazz);
        return nullptr;
    }

    const char *hostname = env->GetStringUTFChars(hostName, nullptr);

    auto *sa = (struct sockaddr_in *) malloc(sizeof(struct sockaddr_in));

    sa->sin_port = htons(port);
    sa->sin_family = AF_INET;
    if (inet_pton(sa->sin_family, hostname, &sa->sin_addr) != 1) {
        LOGE(TAG, "Can't convert sock addr");
    }

    env->ReleaseStringUTFChars(hostName, hostname);
    env->DeleteLocalRef(inetSocketAddressClazz);

    return sa;
}

jobject
sockaddr_inet_n2j(JNIEnv *env, struct sockaddr_in *sockaddr, int sockaddr_len) {
    // Get InetSocketAddress class
    jclass inetSocketAddressClazz = env->FindClass(INETSOCKETADDRESS_CLASS);
    if (!inetSocketAddressClazz) {
        LOGE(TAG, "Can't get InetSocketAddress class");
        return nullptr;
    }

    jmethodID inetSocketAddressConstructorMethod = env->GetMethodID(inetSocketAddressClazz,
                                                                    "<init>",
                                                                    "(Ljava/lang/String;I)V");
    if (!inetSocketAddressConstructorMethod) {
        LOGE(TAG, "Can't get InetSocketAddress constructor");
        env->DeleteLocalRef(inetSocketAddressClazz);
        return nullptr;
    }

    char ip[INET_ADDRSTRLEN];
    if (inet_ntop(sockaddr->sin_family, (void *) &(sockaddr->sin_addr), ip, sizeof(ip))) {
        LOGE(TAG, "Can't convert ip");
    }

    jstring hostName = env->NewStringUTF(ip);
    jobject inetSocketAddress = env->NewObject(inetSocketAddressClazz,
                                               inetSocketAddressConstructorMethod, hostName,
                                               (jint) htons(sockaddr->sin_port));

    env->DeleteLocalRef(inetSocketAddressClazz);

    return inetSocketAddress;
}

void *
srt_optval_j2n(JNIEnv *env, jobject optVal, int *optval_len) {
    void *srt_optval = nullptr;

    if (optval_len == nullptr) {
        LOGE(TAG, "Can't get optlen");
        return nullptr;
    }

    jclass optValClazz = env->GetObjectClass(optVal);
    if (!optValClazz) {
        LOGE(TAG, "Can't get OptVal class");
        return nullptr;
    }

    // As optVal could be an Int, String,... First step is to get class object.
    jmethodID optValGetClassMethod = env->GetMethodID(optValClazz, "getClass",
                                                      "()Ljava/lang/Class;");
    if (!optValGetClassMethod) {
        LOGE(TAG, "Can't get getClass method");
        env->DeleteLocalRef(optValClazz);
        return nullptr;
    }

    jobject optValClazzObject = env->CallObjectMethod(optVal, optValGetClassMethod);
    if (!optValClazzObject) {
        LOGE(TAG, "Can't get class object");
        env->DeleteLocalRef(optValClazz);
        return nullptr;
    }

    jclass objectClazz = env->GetObjectClass(optValClazzObject);
    if (!objectClazz) {
        LOGE(TAG, "Can't get class");
        env->DeleteLocalRef(optValClazz);
        return nullptr;
    }

    // Then get class name
    jmethodID objectClazzGetNameMethod = env->GetMethodID(objectClazz, "getName",
                                                          "()Ljava/lang/String;");
    if (!objectClazzGetNameMethod) {
        LOGE(TAG, "Can't get getName method");
        env->DeleteLocalRef(objectClazz);
        env->DeleteLocalRef(optValClazz);
        return nullptr;
    }

    auto className = (jstring) env->CallObjectMethod(optValClazzObject, objectClazzGetNameMethod);
    if (!className) {
        LOGE(TAG, "Can't get class name");
        env->DeleteLocalRef(objectClazz);
        env->DeleteLocalRef(optValClazz);
        return nullptr;
    }

    const char *class_name = env->GetStringUTFChars(className, nullptr);
    if (strcmp(class_name, "java.lang.String;") == 0) {
        const char *optval = env->GetStringUTFChars((jstring) optVal, nullptr);
        *optval_len = strlen(optval);
        srt_optval = strdup(optval);
        env->ReleaseStringUTFChars((jstring) optVal, optval);
    } else if (strcmp(class_name, ENUM_PACKAGE".Transtype") == 0) {
        SRT_TRANSTYPE transtype = srt_transtype_j2n(env, optVal);
        *optval_len = sizeof(transtype);
        srt_optval = malloc(static_cast<size_t>(*optval_len));
        *(SRT_TRANSTYPE *) srt_optval = transtype;
    } else if (strcmp(class_name, ENUM_PACKAGE".KMState") == 0) {
        SRT_KM_STATE kmstate = srt_kmstate_j2n(env, optVal);
        *optval_len = sizeof(kmstate);
        srt_optval = malloc(static_cast<size_t>(*optval_len));
        *(SRT_KM_STATE *) srt_optval = kmstate;
    } else if (strcmp(class_name, "java.lang.Long") == 0) {
        jmethodID longValueMethod = env->GetMethodID(optValClazz, "longValue", "()J");
        if (!longValueMethod) {
            LOGE(TAG, "Can't get longValue method");
            return nullptr;
        }
        *optval_len = sizeof(int64_t);
        srt_optval = malloc(static_cast<size_t>(*optval_len));
        *(int64_t *) srt_optval = env->CallLongMethod(optVal, longValueMethod);
    } else if (strcmp(class_name, "java.lang.Integer") == 0) {
        jmethodID intValueMethod = env->GetMethodID(optValClazz, "intValue", "()I");
        if (!intValueMethod) {
            LOGE(TAG, "Can't get intValue method");
            return nullptr;
        }
        *optval_len = sizeof(int);
        srt_optval = malloc(static_cast<size_t>(*optval_len));
        *(int *) srt_optval = env->CallIntMethod(optVal, intValueMethod);
    } else if (strcmp(class_name, "java.lang.Boolean") == 0) {
        jmethodID booleanValueMethod = env->GetMethodID(optValClazz, "booleanValue", "()Z");
        if (!booleanValueMethod) {
            LOGE(TAG, "Can't get booleanValue method");
            return nullptr;
        }
        *optval_len = sizeof(bool);
        srt_optval = malloc(static_cast<size_t>(*optval_len));
        *(bool *) srt_optval = (env->CallBooleanMethod(optVal, booleanValueMethod) == JNI_TRUE);
    } else {
        LOGE(TAG, "OptVal: unknown class %s", class_name);
    }

    env->ReleaseStringUTFChars(className, class_name);
    env->DeleteLocalRef(objectClazz);
    env->DeleteLocalRef(optValClazz);

    return srt_optval;
}

SRT_MSGCTRL *
srt_msgctrl_j2n(JNIEnv *env, jobject msgCtrl) {
    SRT_MSGCTRL *srt_msgctrl = nullptr;

    if (msgCtrl == nullptr)
        return nullptr;

    jclass msgCtrlClazz = env->GetObjectClass(msgCtrl);
    if (!msgCtrlClazz) {
        LOGE(TAG, "Can't get MsgCtrl class");
        return nullptr;
    }

    jfieldID msgCtrlFlagsField = env->GetFieldID(msgCtrlClazz, "flags", "I");
    if (!msgCtrlFlagsField) {
        LOGE(TAG, "Can't get flags field");
        env->DeleteLocalRef(msgCtrlClazz);
        return nullptr;
    }

    jfieldID msgCtrlTtlField = env->GetFieldID(msgCtrlClazz, "ttl", "I");
    if (!msgCtrlTtlField) {
        LOGE(TAG, "Can't get ttl field");
        env->DeleteLocalRef(msgCtrlClazz);
        return nullptr;
    }

    jfieldID msgCtrlInOrderField = env->GetFieldID(msgCtrlClazz, "inOrder", "Z");
    if (!msgCtrlInOrderField) {
        LOGE(TAG, "Can't get inOrder field");
        env->DeleteLocalRef(msgCtrlClazz);
        return nullptr;
    }

    jfieldID msgCtrlBondaryField = env->GetFieldID(msgCtrlClazz, "boundary", "I");
    if (!msgCtrlBondaryField) {
        LOGE(TAG, "Can't get boundary field");
        env->DeleteLocalRef(msgCtrlClazz);
        return nullptr;
    }

    jfieldID msgCtrlSrcTimeField = env->GetFieldID(msgCtrlClazz, "srcTime", "J");
    if (!msgCtrlSrcTimeField) {
        LOGE(TAG, "Can't get srcTime field");
        env->DeleteLocalRef(msgCtrlClazz);
        return nullptr;
    }

    jfieldID msgCtrlPktSeqField = env->GetFieldID(msgCtrlClazz, "pktSeq", "I");
    if (!msgCtrlPktSeqField) {
        LOGE(TAG, "Can't get pktSeq field");
        env->DeleteLocalRef(msgCtrlClazz);
        return nullptr;
    }

    jfieldID msgCtrlNoField = env->GetFieldID(msgCtrlClazz, "no", "I");
    if (!msgCtrlNoField) {
        LOGE(TAG, "Can't get message number field");
        env->DeleteLocalRef(msgCtrlClazz);
        return nullptr;
    }

    srt_msgctrl = (SRT_MSGCTRL *) malloc(sizeof(SRT_MSGCTRL));
    if (srt_msgctrl != nullptr) {
        srt_msgctrl->flags = env->GetIntField(msgCtrl, msgCtrlFlagsField);
        srt_msgctrl->msgttl = env->GetIntField(msgCtrl, msgCtrlTtlField);
        srt_msgctrl->inorder = env->GetBooleanField(msgCtrl, msgCtrlInOrderField);
        srt_msgctrl->boundary = env->GetIntField(msgCtrl, msgCtrlBondaryField);
        srt_msgctrl->srctime = (uint64_t) env->GetLongField(msgCtrl, msgCtrlSrcTimeField);
        srt_msgctrl->pktseq = env->GetIntField(msgCtrl, msgCtrlPktSeqField);
        srt_msgctrl->msgno = env->GetIntField(msgCtrl, msgCtrlNoField);
    }

    env->DeleteLocalRef(msgCtrlClazz);

    return srt_msgctrl;
}

SRTSOCKET srt_socket_j2n(JNIEnv *env, jobject SrtSocket) {
    jclass socketClazz = env->GetObjectClass(SrtSocket);
    if (!socketClazz) {
        LOGE(TAG, "Can't get Socket class");
        return SRT_INVALID_SOCK;
    }

    jfieldID srtSocketField = env->GetFieldID(socketClazz, "srtsocket", "I");
    if (!srtSocketField) {
        LOGE(TAG, "Can't get srtsocket field");
        env->DeleteLocalRef(socketClazz);
        return SRT_INVALID_SOCK;
    }

    jint srtsocket = env->GetIntField(SrtSocket, srtSocketField);

    env->DeleteLocalRef(socketClazz);

    return srtsocket;
}

jobject srt_socket_n2j(JNIEnv *env, SRTSOCKET srtsocket) {
    jclass srtSocketClazz = env->FindClass(SRTSOCKET_CLASS);
    if (!srtSocketClazz) {
        LOGE(TAG, "Can't find Srt Socket class");
        return nullptr;
    }

    jmethodID srtSocketConstructorMethod = env->GetMethodID(srtSocketClazz, "<init>", "(I)V");
    if (!srtSocketConstructorMethod) {
        LOGE(TAG, "Can't get SrtSocket constructor");
        env->DeleteLocalRef(srtSocketClazz);
        return nullptr;
    }

    jobject srtSocket = env->NewObject(srtSocketClazz, srtSocketConstructorMethod, srtsocket);

    env->DeleteLocalRef(srtSocketClazz);

    return srtSocket;
}

jobject new_pair(JNIEnv *env, jobject first, jobject second) {
    jclass pairClazz = env->FindClass(PAIR_CLASS);
    if (!pairClazz) {
        LOGE(TAG, "Can't get Pair class");
        return nullptr;
    }

    jmethodID pairConstructorMethod = env->GetMethodID(pairClazz, "<init>",
                                                       "(Ljava/lang/Object;Ljava/lang/Object;)V");
    if (!pairConstructorMethod) {
        LOGE(TAG, "Can't get Pair constructor");
        env->DeleteLocalRef(pairClazz);
        return nullptr;
    }

    jobject pair = env->NewObject(pairClazz, pairConstructorMethod, first, second);

    env->DeleteLocalRef(pairClazz);

    return pair;
}

#ifdef __cplusplus
}
#endif
