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
    if ((sockaddr_len == 0) || (sockaddr == nullptr)) {
        return nullptr;
    }

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

const char *
get_class_name(JNIEnv *env, jobject object) {
    jclass objectClazz = env->GetObjectClass(object);
    if (!objectClazz) {
        LOGE(TAG, "Can't get object class");
        return nullptr;
    }

    // As object could be an Int, String,... First step is to get class object.
    jmethodID objectGetClassMethod = env->GetMethodID(objectClazz, "getClass",
                                                      "()Ljava/lang/Class;");
    if (!objectGetClassMethod) {
        LOGE(TAG, "Can't get getClass method");
        env->DeleteLocalRef(objectClazz);
        return nullptr;
    }

    jobject objectClazzObject = env->CallObjectMethod(object, objectGetClassMethod);
    if (!objectClazzObject) {
        LOGE(TAG, "Can't get class object");
        env->DeleteLocalRef(objectClazz);
        return nullptr;
    }

    jclass clazzClazz = env->GetObjectClass(objectClazzObject);
    if (!clazzClazz) {
        LOGE(TAG, "Can't get class");
        env->DeleteLocalRef(objectClazz);
        return nullptr;
    }

    // Then get class name
    jmethodID objectClazzGetNameMethod = env->GetMethodID(clazzClazz, "getName",
                                                          "()Ljava/lang/String;");
    if (!objectClazzGetNameMethod) {
        LOGE(TAG, "Can't get getName method");
        env->DeleteLocalRef(objectClazz);
        env->DeleteLocalRef(clazzClazz);
        return nullptr;
    }

    auto className = (jstring) env->CallObjectMethod(objectClazzObject, objectClazzGetNameMethod);
    if (!className) {
        LOGE(TAG, "Can't get class name");
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

    const char *class_name = get_class_name(env, optVal);
    if (class_name == nullptr) {
        return nullptr;
    }

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

    free((void *) class_name);
    env->DeleteLocalRef(optValClazz);

    return srt_optval;
}

jobject new_long(JNIEnv *env, int64_t val) {
    jclass longClazz = env->FindClass(LONG_CLASS);
    if (!longClazz) {
        LOGE(TAG, "Can't find Long class");
        return nullptr;
    }
    jmethodID longConstructorMethod = env->GetMethodID(longClazz, "<init>", "(J)V");
    if (!longConstructorMethod) {
        LOGE(TAG, "Can't find Long constructor");
        return nullptr;
    }
    return env->NewObject(longClazz, longConstructorMethod, val);
}

jobject new_bool(JNIEnv *env, bool val) {
    jclass boolClazz = env->FindClass(BOOLEAN_CLASS);
    if (!boolClazz) {
        LOGE(TAG, "Can't find Boolean class");
        return nullptr;
    }
    jmethodID booleanConstructorMethod = env->GetMethodID(boolClazz, "<init>", "(Z)V");
    if (!booleanConstructorMethod) {
        LOGE(TAG, "Can't find Boolean constructor");
        return nullptr;
    }
    return env->NewObject(boolClazz, booleanConstructorMethod, val);
}

jobject new_int(JNIEnv *env, int val) {
    jclass intClazz = env->FindClass(INT_CLASS);
    if (!intClazz) {
        LOGE(TAG, "Can't find Integer class");
        return nullptr;
    }
    jmethodID integerConstructorMethod = env->GetMethodID(intClazz, "<init>", "(I)V");
    if (!integerConstructorMethod) {
        LOGE(TAG, "Can't find Integer constructor");
        return nullptr;
    }
    return env->NewObject(intClazz, integerConstructorMethod, val);
}

jobject srt_optval_n2j(JNIEnv *env, int u, int level, jobject sockOpt) {
    jobject optVal = nullptr;

    int sockopt = srt_sockopt_j2n(env, sockOpt);
    if (sockopt < 0) {
        return nullptr;
    }

    switch (sockopt) {
        case SRTO_INPUTBW:
        case SRTO_MAXBW: {
            // Int64
            int64_t optval = 0;
            int optlen = sizeof(optval);
            if (srt_getsockopt(u, level, (SRT_SOCKOPT) sockopt, &optval, &optlen) != 0) {
                LOGE(TAG, "Can't execute long getsockopt");
                return nullptr;
            }
            optVal = new_long(env, optval);
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
        case SRTO_TSBPDMODE: {
            // Boolean
            bool optval = false;
            int optlen = sizeof(bool);
            if (srt_getsockopt(u, level, (SRT_SOCKOPT) sockopt, (void *) &optval, &optlen) != 0) {
                LOGE(TAG, "Can't execute bool getsockopt");
                return nullptr;
            }
            optVal = new_bool(env, optval);
            break;
        }
        case SRTO_PACKETFILTER:
        case SRTO_PASSPHRASE:
        case SRTO_STREAMID: {
            // String
            const char optval[512] = {0};
            int optlen = sizeof(optval);
            if (srt_getsockopt(u, level, (SRT_SOCKOPT) sockopt, (void *) &optval, &optlen) != 0) {
                LOGE(TAG, "Can't execute string getsockopt");
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
            if (srt_getsockopt(u, level, (SRT_SOCKOPT) sockopt, (void *) &optval, &optlen) != 0) {
                LOGE(TAG, "Can't execute SRT_KM_STATE getsockopt");
                return nullptr;
            }
            optVal = srt_kmstate_n2j(env, optval);
            break;
        }
        case SRTO_TRANSTYPE: {
            // Transtype
            SRT_TRANSTYPE optval;
            int optlen = sizeof(SRT_TRANSTYPE);
            if (srt_getsockopt(u, level, (SRT_SOCKOPT) sockopt, (void *) &optval, &optlen) != 0) {
                LOGE(TAG, "Can't execute SRT_TRANSTYPE getsockopt");
                return nullptr;
            }
            optVal = srt_transtype_n2j(env, optval);
            break;
        }
        default: {
            // Int
            int optval = 0;
            int optlen = sizeof(int);
            if (srt_getsockopt(u, level, (SRT_SOCKOPT) sockopt, (void *) &optval, &optlen) != 0) {
                LOGE(TAG, "Can't execute int getsockopt");
                return nullptr;
            }
            optVal = new_int(env, optval);
            break;
        }
    }

    return optVal;
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

SRTSOCKET srt_socket_j2n(JNIEnv *env, jobject srtSocket) {
    jclass socketClazz = env->GetObjectClass(srtSocket);
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

    jint srtsocket = env->GetIntField(srtSocket, srtSocketField);

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

SRTSOCKET *srt_sockets_j2n(JNIEnv *env, jobjectArray srtSockets, int *nSockets) {
    *nSockets = env->GetArrayLength(srtSockets);
    if (*nSockets == 0)
        return nullptr;

    SRTSOCKET *srtsocket = (SRTSOCKET *) malloc(
            reinterpret_cast<size_t>(*nSockets * sizeof(SRTSOCKET)));

    for (int i = 0; i < *nSockets; i++) {
        jobject srtSocket = env->GetObjectArrayElement(srtSockets, i);
        srtsocket[i] = srt_socket_j2n(env, srtSocket);
    }

    return srtsocket;
}

int srt_epoll_j2n(JNIEnv *env, jobject epoll) {
    jclass epollClazz = env->GetObjectClass(epoll);
    if (!epollClazz) {
        LOGE(TAG, "Can't get Epoll class");
        return -1;
    }

    jfieldID eidField = env->GetFieldID(epollClazz, "eid", "I");
    if (!eidField) {
        LOGE(TAG, "Can't get eid field");
        env->DeleteLocalRef(epollClazz);
        return -1;
    }

    jint eid = env->GetIntField(epoll, eidField);

    env->DeleteLocalRef(epollClazz);

    return eid;
}

jobject srt_epoll_n2j(JNIEnv *env, int eid) {
    jclass epollClazz = env->FindClass(EPOLL_CLASS);
    if (!epollClazz) {
        LOGE(TAG, "Can't find Epoll class");
        return nullptr;
    }

    jmethodID epollConstructorMethod = env->GetMethodID(epollClazz, "<init>", "(I)V");
    if (!epollConstructorMethod) {
        LOGE(TAG, "Can't get Epoll constructor");
        env->DeleteLocalRef(epollClazz);
        return nullptr;
    }

    jobject epoll = env->NewObject(epollClazz, epollConstructorMethod, eid);

    env->DeleteLocalRef(epollClazz);

    return epoll;
}

int srt_epoll_opts_j2n(JNIEnv *env, jobjectArray epollEvents) {
    int nEvents = env->GetArrayLength(epollEvents);
    int events = 0;

    for (int i = 0; i < nEvents; i++) {
        jobject epollEvent = env->GetObjectArrayElement(epollEvents, i);
        events |= srt_epoll_opt_j2n(env, epollEvent);
    }

    return events;
}

int srt_epoll_flags_j2n(JNIEnv *env, jobjectArray epollFlags) {
    int nFlags = env->GetArrayLength(epollFlags);
    int flags = 0;

    for (int i = 0; i < nFlags; i++) {
        jobject epollFlag = env->GetObjectArrayElement(epollFlags, i);
        flags |= srt_epoll_flag_j2n(env, epollFlag);
    }

    return flags;
}

jobjectArray srt_epoll_flags_n2j(JNIEnv *env, int epoll_flags) {
    int max = SRT_EPOLL_ENABLE_OUTPUTCHECK;
    int nFlags = 0;
    int epoll_flag;

    // Find EpollFlag array size
    for (int i = 0; i < max; i++) {
        epoll_flag = epoll_flags & 1 << i;
        if (epoll_flag != 0) {
            nFlags++;
        }
    }

    jclass epollFlagClazz = env->FindClass(EPOLLFLAG_CLASS);
    if (!epollFlagClazz) {
        LOGE(TAG, "Can't find EpollFlag class");
        return nullptr;
    }

    jobjectArray epollFlags = env->NewObjectArray(nFlags, epollFlagClazz, nullptr);
    if (!epollFlags) {
        LOGE(TAG, "Can't create EpollFlag Array");
        env->DeleteLocalRef(epollFlagClazz);
        return nullptr;
    }

    jobject epollFlag;

    for (int i = 0; i < max; i++) {
        epoll_flag = epoll_flags & 1 << i;
        if (epoll_flag != 0) {
            epollFlag = srt_epoll_flag_n2j(env, epoll_flag);
            env->SetObjectArrayElement(epollFlags, i, epollFlag);
        }
    }

    env->DeleteLocalRef(epollFlagClazz);

    return epollFlags;
}

SRT_EPOLL_EVENT *srt_epoll_event_j2n(JNIEnv *env, jobject epollEvent, SRT_EPOLL_EVENT *srt_event) {
    jclass epollEventClazz = env->GetObjectClass(epollEvent);
    if (!epollEventClazz) {
        LOGE(TAG, "Can't get EpollEvent class");
        return nullptr;
    }

    jfieldID socketField = env->GetFieldID(epollEventClazz, "socket", "L" SRTSOCKET_CLASS ";");
    if (!socketField) {
        LOGE(TAG, "Can't get Socket field");
        env->DeleteLocalRef(epollEventClazz);
        return nullptr;
    }
    jobject srtSocket = env->GetObjectField(epollEvent, socketField);
    srt_event->fd = srt_socket_j2n(env, srtSocket);

    jfieldID epollOptsField = env->GetFieldID(epollEventClazz, "events", "[L" EPOLLOPT_CLASS ";");
    if (!epollOptsField) {
        LOGE(TAG, "Can't get events field");
        env->DeleteLocalRef(epollEventClazz);
        return nullptr;
    }
    jobjectArray epollOpts = static_cast<jobjectArray>(env->GetObjectField(epollEvent,
                                                                           epollOptsField));
    srt_event->events = srt_epoll_opts_j2n(env, epollOpts);

    env->DeleteLocalRef(epollEventClazz);

    return srt_event;
}

SRT_EPOLL_EVENT *srt_epoll_events_j2n(JNIEnv *env, jobjectArray epollEvents, int *nEvents) {
    *nEvents = env->GetArrayLength(epollEvents);

    SRT_EPOLL_EVENT *epoll_events = static_cast<SRT_EPOLL_EVENT *>(malloc(
            *nEvents * sizeof(SRT_EPOLL_EVENT)));

    for (int i = 0; i < *nEvents; i++) {
        jobject epollEvent = env->GetObjectArrayElement(epollEvents, i);
        srt_epoll_event_j2n(env, epollEvent, &epoll_events[i]);
    }

    return epoll_events;
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
