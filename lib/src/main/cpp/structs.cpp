#include <jni.h>
#include <string>
#include <cerrno>
#include <cstring>
#include <sys/socket.h>
#include <cstdlib>

#include "srt/srt.h"

#include "log.h"
#include "enums.h"
#include "structs.h"

#ifdef __cplusplus
extern "C" {
#endif

#define TAG "SRTJniStructs"

struct sockaddr_in *
inet_socket_address_from_java_to_native(JNIEnv *env, jobject jinet_socket_address) {
    // Get InetSocketAddress class
    jclass jinet_socket_address_class = env->GetObjectClass(jinet_socket_address);
    if (!jinet_socket_address_class) {
        LOGE(TAG, "Can't get InetSocketAddress class");
        return nullptr;
    }

    // Port
    jmethodID jgetPort_method = env->GetMethodID(jinet_socket_address_class, "getPort", "()I");
    if (!jgetPort_method) {
        LOGE(TAG, "Can't get getPort method");
        env->DeleteLocalRef(jinet_socket_address_class);
        return nullptr;
    }
    int port = env->CallIntMethod(jinet_socket_address, jgetPort_method);

    // Hostname
    jmethodID jgetHostName_method = env->GetMethodID(jinet_socket_address_class, "getHostString",
                                                     "()Ljava/lang/String;");
    if (!jgetHostName_method) {
        LOGE(TAG, "Can't get getHostString method");
        env->DeleteLocalRef(jinet_socket_address_class);
        return nullptr;
    }

    auto jhostname = (jstring) env->CallObjectMethod(jinet_socket_address,
                                                     jgetHostName_method);
    if (!jhostname) {
        LOGE(TAG, "Can't get Hostname");
        env->DeleteLocalRef(jinet_socket_address_class);
        return nullptr;
    }

    const char *hostname = env->GetStringUTFChars(jhostname, nullptr);

    auto *sa = (struct sockaddr_in *) malloc(sizeof(struct sockaddr_in));

    sa->sin_port = htons(port);
    sa->sin_family = AF_INET;
    if(inet_pton(sa->sin_family, hostname, &sa->sin_addr) != 1) {
        LOGE(TAG, "Can't convert sock addr");
    }

    env->ReleaseStringUTFChars(jhostname, hostname);
    env->DeleteLocalRef(jinet_socket_address_class);

    return sa;
}

jobject
inet_socket_address_from_native_to_java(JNIEnv *env, struct sockaddr_in * sa, int addrlen) {
    // Get InetSocketAddress class
    jclass jinet_socket_address_class = env->FindClass(INETSOCKETADDRESS_CLASS);
    if (!jinet_socket_address_class) {
        LOGE(TAG, "Can't get InetSocketAddress class");
        return nullptr;
    }

    jmethodID jinit_method = env->GetMethodID(jinet_socket_address_class, "<init>", "(Ljava/lang/String;I)V");
    if (!jinit_method) {
        LOGE(TAG, "Can't get InetSocketAddress constructor field");
        env->DeleteLocalRef(jinet_socket_address_class);
        return nullptr;
    }

    char ip[INET_ADDRSTRLEN];
    if(inet_ntop(sa->sin_family, (void *)&(sa->sin_addr), ip, sizeof(ip))) {
            LOGE(TAG, "Can't convert ip");
    }

    jstring jhostname = env->NewStringUTF(ip);
    jobject jinet_socket_address = env->NewObject(jinet_socket_address_class, jinit_method, jhostname, (jint) htons(sa->sin_port));

    env->DeleteLocalRef(jinet_socket_address_class);

    return jinet_socket_address;
}

void *
srt_optval_from_java_to_native(JNIEnv *env, jobject jopval, int *optlen) {
    void *res = nullptr;

    if (optlen == nullptr) {
        LOGE(TAG, "Can't get optlen");
        return nullptr;
    }

    jclass jopval_class = env->GetObjectClass(jopval);
    if (!jopval_class) {
        LOGE(TAG, "Can't get optval class");
        return nullptr;
    }

    // As joptval could be an Int, String,... First step is to get class object.
    // First get the class object
    jmethodID jgetClass_method = env->GetMethodID(jopval_class, "getClass", "()Ljava/lang/Class;");
    if (!jgetClass_method) {
        LOGE(TAG, "Can't get getClass method");
        env->DeleteLocalRef(jopval_class);
        return nullptr;
    }

    jobject jclassObject = env->CallObjectMethod(jopval, jgetClass_method);
    if (!jclassObject) {
        LOGE(TAG, "Can't get object class");
        env->DeleteLocalRef(jopval_class);
        return nullptr;
    }

    jclass jclassClass = env->GetObjectClass(jclassObject);
    if (!jclassClass) {
        LOGE(TAG, "Can't get class");
        env->DeleteLocalRef(jopval_class);
        return nullptr;
    }

    // Then get class name
    jmethodID jgetName_method = env->GetMethodID(jclassClass, "getName", "()Ljava/lang/String;");
    if (!jgetName_method) {
        LOGE(TAG, "Can't get getName method");
        env->DeleteLocalRef(jopval_class);
        return nullptr;
    }

    auto jclassName = (jstring) env->CallObjectMethod(jclassObject, jgetName_method);
    if (!jclassName) {
        LOGE(TAG, "Can't get class name");
        env->DeleteLocalRef(jopval_class);
        return nullptr;
    }

    const char *class_name = env->GetStringUTFChars(jclassName, nullptr);

    if (strcmp(class_name, "java.lang.String;") == 0) {
        const char *optval = env->GetStringUTFChars((jstring) jopval, nullptr);
        *optlen = strlen(optval);
        res = strdup(optval);
        env->ReleaseStringUTFChars((jstring) jopval, optval);
    } else if (strcmp(class_name, ENUM_PACKAGE".Transtype") == 0) {
        SRT_TRANSTYPE transtype = srt_transtype_from_java_to_native(env, jopval);
        *optlen = sizeof(transtype);
        res = malloc(static_cast<size_t>(*optlen));
        *(SRT_TRANSTYPE *)res = transtype;
    } else if (strcmp(class_name, ENUM_PACKAGE".KMState") == 0) {
        SRT_KM_STATE kmstate = srt_kmstate_from_java_to_native(env, jopval);
        *optlen = sizeof(kmstate);
        res = malloc(static_cast<size_t>(*optlen));
        *(SRT_KM_STATE *)res = kmstate;
        LOGE(TAG, ">> res %d size %d", kmstate,  *optlen);
    } else if (strcmp(class_name, "java.lang.Long") == 0) {
        jmethodID jlongValue_method = env->GetMethodID(jopval_class, "longValue", "()J");
        if (!jlongValue_method) {
            LOGE(TAG, "Can't get longValue method");
            return nullptr;
        }
        *optlen = sizeof(int64_t);
        res = malloc(static_cast<size_t>(*optlen));
        *(int64_t *)res = env->CallLongMethod(jopval, jlongValue_method);
    } else if (strcmp(class_name, "java.lang.Integer") == 0) {
        jmethodID jintValue_method = env->GetMethodID(jopval_class, "intValue", "()I");
        if (!jintValue_method) {
            LOGE(TAG, "Can't get intValue method");
            return nullptr;
        }
        *optlen = sizeof(int);
        res = malloc(static_cast<size_t>(*optlen));
        *(int *)res = env->CallIntMethod(jopval, jintValue_method);
    } else if (strcmp(class_name, "java.lang.Boolean") == 0) {
        jmethodID jbooleanValue_method = env->GetMethodID(jopval_class, "booleanValue", "()Z");
        if (!jbooleanValue_method) {
            LOGE(TAG, "Can't get booleanValue method");
            return nullptr;
        }
        *optlen = sizeof(bool);
        res = malloc(static_cast<size_t>(*optlen));
        *(bool *)res = (env->CallBooleanMethod(jopval, jbooleanValue_method) == JNI_TRUE);
    } else {
        LOGE(TAG, "Unknown class %s", class_name);
    }

    env->ReleaseStringUTFChars(jclassName, class_name);
    env->DeleteLocalRef(jopval_class);

    return res;
}

SRT_MSGCTRL *
srt_msgctrl_from_java_to_native(JNIEnv *env, jobject jmsgCtrl) {
    SRT_MSGCTRL *res = nullptr;

    if (jmsgCtrl == nullptr)
        return nullptr;

    jclass jmsgctrl_class = env->GetObjectClass(jmsgCtrl);
    if (!jmsgctrl_class) {
        LOGE(TAG, "Can't get MsgCtrl class");
        return nullptr;
    }

    jfieldID jflags_field = env->GetFieldID(jmsgctrl_class, "flags", "I");
    if (!jflags_field) {
        LOGE(TAG, "Can't get flags field");
        env->DeleteLocalRef(jmsgctrl_class);
        return nullptr;
    }

    jfieldID jttl_field = env->GetFieldID(jmsgctrl_class, "ttl", "I");
    if (!jttl_field) {
        LOGE(TAG, "Can't get ttl field");
        env->DeleteLocalRef(jmsgctrl_class);
        return nullptr;
    }

    jfieldID jinOrder_field = env->GetFieldID(jmsgctrl_class, "inOrder", "Z");
    if (!jinOrder_field) {
        LOGE(TAG, "Can't get inOrder field");
        env->DeleteLocalRef(jmsgctrl_class);
        return nullptr;
    }

    jfieldID jboundary_field = env->GetFieldID(jmsgctrl_class, "boundary", "I");
    if (!jboundary_field) {
        LOGE(TAG, "Can't get boundary field");
        env->DeleteLocalRef(jmsgctrl_class);
        return nullptr;
    }

    jfieldID jsrcTime_field = env->GetFieldID(jmsgctrl_class, "srcTime", "J");
    if (!jsrcTime_field) {
        LOGE(TAG, "Can't get srcTime field");
        env->DeleteLocalRef(jmsgctrl_class);
        return nullptr;
    }

    jfieldID jpktSeq_field = env->GetFieldID(jmsgctrl_class, "pktSeq", "I");
    if (!jpktSeq_field) {
        LOGE(TAG, "Can't get pktSeq field");
        env->DeleteLocalRef(jmsgctrl_class);
        return nullptr;
    }

    jfieldID jmsgno_field = env->GetFieldID(jmsgctrl_class, "no", "I");
    if (!jmsgno_field) {
        LOGE(TAG, "Can't get message number field");
        env->DeleteLocalRef(jmsgctrl_class);
        return nullptr;
    }

    res = (SRT_MSGCTRL *) malloc(sizeof(SRT_MSGCTRL));
    if (res != nullptr) {
        res->flags = env->GetIntField(jmsgCtrl, jflags_field);
        res->msgttl = env->GetIntField(jmsgCtrl, jttl_field);
        res->inorder = env->GetBooleanField(jmsgCtrl, jinOrder_field);
        res->boundary = env->GetIntField(jmsgCtrl, jboundary_field);
        res->srctime = (uint64_t )env->GetLongField(jmsgCtrl, jsrcTime_field);
        res->pktseq = env->GetIntField(jmsgCtrl, jpktSeq_field);
        res->msgno = env->GetIntField(jmsgCtrl, jmsgno_field);
    }

    env->DeleteLocalRef(jmsgctrl_class);

    return res;
}

SRTSOCKET srt_socket_from_java_to_native(JNIEnv *env, jobject ju) {
    jclass jsocket_class = env->GetObjectClass(ju);
    if (!jsocket_class) {
        LOGE(TAG, "Can't get socket class");
        return SRT_INVALID_SOCK;
    }

    jfieldID jsrtsocket_field = env->GetFieldID(jsocket_class, "srtsocket", "I");
    if (!jsrtsocket_field) {
        LOGE(TAG, "Can't get srtsocket field");
        env->DeleteLocalRef(jsocket_class);
        return SRT_INVALID_SOCK;
    }

    jint srtsocket = env->GetIntField(ju, jsrtsocket_field);

    env->DeleteLocalRef(jsocket_class);
    return srtsocket;
}

jobject srt_socket_from_native_to_java(JNIEnv *env, SRTSOCKET u) {
    jclass jsocket_class = env->FindClass(SRTSOCKET_CLASS);
    if (!jsocket_class) {
        LOGE(TAG, "Can't find socket class");
        return nullptr;
    }

    jmethodID jinit_method = env->GetMethodID(jsocket_class, "<init>", "(I)V");
    if (!jinit_method) {
        LOGE(TAG, "Can't get SrtSocket constructor field");
        env->DeleteLocalRef(jsocket_class);
        return nullptr;
    }

    jobject ju = env->NewObject(jsocket_class, jinit_method, u);

    env->DeleteLocalRef(jsocket_class);
    return ju;
}

jobject create_java_pair(JNIEnv *env, jobject first, jobject second) {
    jclass jpair_class = env->FindClass(PAIR_CLASS);
    if (!jpair_class) {
        LOGE(TAG, "Can't get Pair class");
        return nullptr;
    }

    jmethodID jinit_method = env->GetMethodID(jpair_class, "<init>", "(Ljava/lang/Object;Ljava/lang/Object;)V");
    if (!jinit_method) {
        LOGE(TAG, "Can't get Pair constructor field");
        env->DeleteLocalRef(jpair_class);
        return nullptr;
    }

    jobject jpair = env->NewObject(jpair_class, jinit_method, first, second);

    env->DeleteLocalRef(jpair_class);
    return jpair;
}

#ifdef __cplusplus
}
#endif
