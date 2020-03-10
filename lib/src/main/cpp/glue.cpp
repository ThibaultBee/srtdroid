#include <jni.h>

#include "srt/srt.h"
#include "srt/logging_api.h"

#include "log.h"
#include "enums.h"
#include "structs.h"

#ifdef __cplusplus
extern "C" {
#endif

#define TAG "SRTJniGlue"

// SRT Logger callback
void srt_logger_cb(void *opaque, int level, const char *file, int line, const char *area,
                   const char *message) {
    int android_log_level = ANDROID_LOG_UNKNOWN;

    switch (level) {
        case LOG_CRIT:
            android_log_level = ANDROID_LOG_FATAL;
            break;
        case LOG_ERR:
            android_log_level = ANDROID_LOG_ERROR;
            break;
        case LOG_WARNING:
            android_log_level = ANDROID_LOG_WARN;
            break;
        case LOG_NOTICE:
            android_log_level = ANDROID_LOG_INFO;
            break;
        case LOG_DEBUG:
            android_log_level = ANDROID_LOG_DEBUG;
            break;
        default:
            LOGE(TAG, "Unknown log level %d", level);
    }

    __android_log_print(android_log_level, "libsrt", "%s@%d:%s %s", file, line, area, message);
}

// Library Initialization
JNIEXPORT jint JNICALL
nativeStartUp(JNIEnv *env, jobject obj) {
    srt_setloghandler(nullptr, srt_logger_cb);
    return srt_startup();
}

JNIEXPORT jint JNICALL
nativeCleanUp(JNIEnv *env, jobject obj) {
    return srt_cleanup();
}

// Creating and configuring sockets
JNIEXPORT jint JNICALL
nativeSocket(JNIEnv *env, jobject obj,
             jobject addressFamily,
             jint type,
             jint protocol) {
    int af = address_family_j2n(env, addressFamily);
    if (af <= 0) {
        LOGE(TAG, "Bad value for address family");
        return af;
    }

    return srt_socket(af, type, protocol);
}

JNIEXPORT jint JNICALL
nativeCreateSocket(JNIEnv *env, jobject obj) {
    return srt_create_socket();
}

JNIEXPORT jint JNICALL
nativeBind(JNIEnv *env, jobject ju, jobject inetSocketAddress) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    const struct sockaddr_in *sa = sockaddr_inet_j2n(env,
                                                     inetSocketAddress);

    int res = srt_bind(u, (struct sockaddr *) sa, sizeof(*sa));

    if (!sa) {
        free((void *) sa);
    }

    return res;
}

JNIEXPORT jobject JNICALL
nativeGetSockState(JNIEnv *env, jobject ju) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    SRT_SOCKSTATUS sock_status = srt_getsockstate((SRTSOCKET) u);

    return srt_sockstatus_n2j(env, sock_status);
}

JNIEXPORT jint JNICALL
nativeClose(JNIEnv *env, jobject ju) {
    SRTSOCKET u = srt_socket_j2n(env, ju);

    return srt_close((SRTSOCKET) u);
}

// Connecting
JNIEXPORT jint JNICALL
nativeListen(JNIEnv *env, jobject ju, jint backlog) {
    SRTSOCKET u = srt_socket_j2n(env, ju);

    return srt_listen((SRTSOCKET) u, (int) backlog);
}

JNIEXPORT jobject JNICALL
nativeAccept(JNIEnv *env, jobject ju) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    struct sockaddr_in sockaddr = {0};
    int sockaddr_len = 0;

    SRTSOCKET new_u = srt_accept((SRTSOCKET) u, (struct sockaddr *) &sockaddr, &sockaddr_len);
    jobject inetSocketAddress = sockaddr_inet_n2j(env, &sockaddr, sockaddr_len);

    jobject res = new_pair(env, srt_socket_n2j(env, new_u),
                           inetSocketAddress);

    return res;
}

JNIEXPORT jint JNICALL
nativeConnect(JNIEnv *env, jobject ju, jobject inetSocketAddress) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    const struct sockaddr_in *sa = sockaddr_inet_j2n(env,
                                                     inetSocketAddress);
    int res = srt_connect((SRTSOCKET) u, (struct sockaddr *) sa, sizeof(*sa));

    if (!sa) {
        free((void *) sa);
    }

    return res;
}

JNIEXPORT jint JNICALL
nativeRendezVous(JNIEnv *env, jobject ju, jobject localAddress, jobject remoteAddress) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    const struct sockaddr_in *local_address = sockaddr_inet_j2n(env,
                                                                localAddress);
    const struct sockaddr_in *remote_address = sockaddr_inet_j2n(env,
                                                                 remoteAddress);
    int res = srt_rendezvous((SRTSOCKET) u, (struct sockaddr *) local_address,
                             sizeof(*local_address), (struct sockaddr *) remote_address,
                             sizeof(*remote_address));

    if (!local_address) {
        free((void *) local_address);
    }

    if (!remote_address) {
        free((void *) remote_address);
    }

    return res;
}

// Options and properties
JNIEXPORT jobject JNICALL
nativeGetPeerName(JNIEnv *env, jobject ju) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    struct sockaddr_in sockaddr = {0};
    int sockaddr_len = 0;

    srt_getpeername((SRTSOCKET) u, (struct sockaddr *) &sockaddr, &sockaddr_len);
    jobject inetSocketAddress = sockaddr_inet_n2j(env, &sockaddr, sockaddr_len);

    return inetSocketAddress;
}

JNIEXPORT jobject JNICALL
nativeGetSockName(JNIEnv *env, jobject ju) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    struct sockaddr_in sockaddr = {0};
    int sockaddr_len = 0;

    srt_getsockname((SRTSOCKET) u, (struct sockaddr *) &sockaddr, &sockaddr_len);
    jobject inetSocketAddress = sockaddr_inet_n2j(env, &sockaddr, sockaddr_len);

    return inetSocketAddress;
}

JNIEXPORT jobject JNICALL
nativeGetSockOpt(JNIEnv *env,
                 jobject ju,
                 jint level /*ignored*/,
                 jobject sockOpt) {
    SRTSOCKET u = srt_socket_j2n(env, ju);

    jobject optVal = srt_optval_n2j(env, u, level, sockOpt);

    return optVal;
}

JNIEXPORT jint JNICALL
nativeSetSockOpt(JNIEnv *env,
                 jobject ju,
                 jint level /*ignored*/,
                 jobject sockOpt,
                 jobject optVal) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    int sockopt = srt_sockopt_j2n(env, sockOpt);
    if (sockopt <= 0) {
        return sockopt;
    }
    int optval_len = 0;
    const void *optval = srt_optval_j2n(env, optVal, &optval_len);

    int res = srt_setsockopt((SRTSOCKET) u,
                             level /*ignored*/, (SRT_SOCKOPT) sockopt, optval, optval_len);

    if (!optval) {
        free((void *) optval);
    }

    return
            res;
}

// Transmission
JNIEXPORT jint JNICALL
nativeSend(JNIEnv *env, jobject ju, jbyteArray byteArray) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    int len = env->GetArrayLength(byteArray);
    char *buf = (char *) env->GetByteArrayElements(byteArray, nullptr);

    int res = srt_send(u, buf, len);

    env->ReleaseByteArrayElements(byteArray, (jbyte *) buf, 0);

    return res;
}

JNIEXPORT jint JNICALL
nativeSendMsg(JNIEnv *env,
              jobject ju,
              jbyteArray byteArray,
              jint ttl/* = -1*/,
              jboolean inOrder/* = false*/) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    int len = env->GetArrayLength(byteArray);
    char *buf = (char *) env->GetByteArrayElements(byteArray, nullptr);

    int res = srt_sendmsg(u, buf, len, (int) ttl, inOrder);

    env->ReleaseByteArrayElements(byteArray, (jbyte *) buf, 0);

    return res;
}

JNIEXPORT jint JNICALL
nativeSendMsg2(JNIEnv *env,
               jobject ju,
               jbyteArray byteArray,
               jobject msgCtrl) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    SRT_MSGCTRL *msgctrl = srt_msgctrl_j2n(env, msgCtrl);
    int len = env->GetArrayLength(byteArray);
    char *buf = (char *) env->GetByteArrayElements(byteArray, nullptr);

    int res = srt_sendmsg2(u, buf, len, msgctrl);

    env->ReleaseByteArrayElements(byteArray, (jbyte *) buf, 0);
    if (msgctrl != nullptr) {
        free(msgctrl);
    }

    return res;
}

JNIEXPORT jbyteArray JNICALL
nativeRecv(JNIEnv *env, jobject ju, jint len) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    jbyteArray byteArray = nullptr;
    auto *buf = (char *) malloc(sizeof(char));

    int res = srt_recv(u, buf, len);

    if (res > 0) {
        byteArray = env->NewByteArray(res);
        env->SetByteArrayRegion(byteArray, 0, res, (jbyte *) buf);
    }

    if (buf != nullptr) {
        free(buf);
    }

    return byteArray;

}

JNIEXPORT jbyteArray JNICALL
nativeRecvMsg2(JNIEnv *env,
               jobject ju,
               jint len,
               jobject msgCtrl) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    SRT_MSGCTRL *msgctrl = srt_msgctrl_j2n(env, msgCtrl);
    jbyteArray byteArray = nullptr;
    auto *buf = (char *) malloc(sizeof(char));

    int res = srt_recvmsg2(u, buf, len, msgctrl);

    if (res > 0) {
        byteArray = env->NewByteArray(res);
        env->SetByteArrayRegion(byteArray, 0, res, (jbyte *) buf);
    }

    if (buf != nullptr) {
        free(buf);
    }
    if (msgctrl != nullptr) {
        free(msgctrl);
    }

    return byteArray;

}

JNIEXPORT jlong JNICALL
nativeSendFile(JNIEnv *env,
               jobject ju,
               jstring filePath,
               jlong fileOffset,
               jlong size,
               jint block) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    const char *path = env->GetStringUTFChars(filePath, nullptr);
    auto offset = (int64_t) fileOffset;
    int64_t res = srt_sendfile(u, path, &offset, (int64_t) size, block);

    env->ReleaseStringUTFChars(filePath, path);

    return (jlong) res;
}

JNIEXPORT jlong JNICALL
nativeRecvFile(JNIEnv *env,
               jobject ju,
               jstring filePath,
               jlong fileOffset,
               jlong size,
               jint block) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    const char *path = env->GetStringUTFChars(filePath, nullptr);
    auto offset = (int64_t) fileOffset;
    int64_t res = srt_recvfile(u, path, &offset, (int64_t) size, block);

    env->ReleaseStringUTFChars(filePath, path);

    return (jlong) res;
}

// Errors
JNIEXPORT jstring JNICALL
nativeGetLastErrorStr(JNIEnv *env, jobject obj) {
    return env->NewStringUTF(srt_getlasterror_str());
}

JNIEXPORT jobject JNICALL
nativeGetLastError(JNIEnv *env, jobject obj) {
    int err = srt_getlasterror(nullptr);

    return srt_error_n2j(env, err);
}

JNIEXPORT jstring JNICALL
nativeStrError(JNIEnv *env, jobject obj) {
    int error_type = srt_error_j2n(env, obj);
    return env->NewStringUTF(srt_strerror(error_type, 0));
}

JNIEXPORT void JNICALL
nativeClearLastError(JNIEnv *env, jobject obj) {
    srt_clearlasterror();
}

// Asynchronous operations (epoll)
JNIEXPORT jint JNICALL
nativeEpollCreate(JNIEnv *env, jobject epoll) {
    return srt_epoll_create();
}

JNIEXPORT jint JNICALL
nativeEpollAddUSock(JNIEnv *env, jobject epoll, jobject ju, jobjectArray epollEvents) {
    int eid = srt_epoll_j2n(env, epoll);
    SRTSOCKET u = srt_socket_j2n(env, ju);

    if (epollEvents) {
        int events = srt_epoll_opts_j2n(env, epollEvents);
        return srt_epoll_add_usock(eid, u, &events);
    } else {
        return srt_epoll_add_usock(eid, u, nullptr);
    }
}

JNIEXPORT jint JNICALL
nativeEpollUpdateUSock(JNIEnv *env, jobject epoll, jobject ju, jobjectArray epollEvents) {
    int eid = srt_epoll_j2n(env, epoll);
    SRTSOCKET u = srt_socket_j2n(env, ju);

    if (epollEvents) {
        int events = srt_epoll_opts_j2n(env, epollEvents);
        return srt_epoll_update_usock(eid, u, &events);
    } else {
        return srt_epoll_update_usock(eid, u, nullptr);
    }
}

JNIEXPORT jint JNICALL
nativeEpollRemoveUSock(JNIEnv *env, jobject epoll, jobject ju) {
    int eid = srt_epoll_j2n(env, epoll);
    SRTSOCKET u = srt_socket_j2n(env, ju);

    return srt_epoll_remove_usock(eid, u);
}

JNIEXPORT jint JNICALL
nativeEpollWait(JNIEnv *env, jobject epoll, jobjectArray readFds, jobjectArray writeFds,
                jlong timeOut) {
    int eid = srt_epoll_j2n(env, epoll);
    int nReadFds = 0;
    int nWriteFds = 0;
    SRTSOCKET *readfds = nullptr;
    SRTSOCKET *writefds = nullptr;

    if (readFds) {
        readfds = srt_sockets_j2n(env, readFds, &nReadFds);
    }
    if (writeFds) {
        writefds = srt_sockets_j2n(env, writeFds, &nWriteFds);
    }

    int res = srt_epoll_wait(eid, readfds, &nReadFds, writefds, &nWriteFds, timeOut, nullptr, 0,
                             nullptr, 0);

    if (readfds != nullptr) {
        free(readfds);
    }
    if (writefds != nullptr) {
        free(writefds);
    }

    return res;
}

JNIEXPORT jint JNICALL
nativeEpollUWait(JNIEnv *env, jobject epoll, jobjectArray fdsSet, jlong timeOut) {
    int eid = srt_epoll_j2n(env, epoll);
    SRT_EPOLL_EVENT *epoll_events = nullptr;
    int nEpollEvents = 0;

    if (fdsSet) {
        epoll_events = srt_epoll_events_j2n(env, fdsSet, &nEpollEvents);
    }

    int res = srt_epoll_uwait(eid, epoll_events, nEpollEvents, timeOut);

    if (epoll_events != nullptr) {
        free(epoll_events);
    }

    return res;
}


JNIEXPORT jobject JNICALL
nativeEpollSet(JNIEnv *env, jobject epoll, jobjectArray epollFlags) {
    int eid = srt_epoll_j2n(env, epoll);
    int32_t flags = srt_epoll_flags_j2n(env, epollFlags);

    flags = srt_epoll_set(eid, flags);

    return srt_epoll_flags_n2j(env, flags);
}

JNIEXPORT jobject JNICALL
nativeEpollGet(JNIEnv *env, jobject epoll) {
    int eid = srt_epoll_j2n(env, epoll);

    int32_t flags = srt_epoll_set(eid, -1);

    return srt_epoll_flags_n2j(env, flags);
}

JNIEXPORT jint JNICALL
nativeEpollRelease(JNIEnv *env, jobject epoll) {
    int eid = srt_epoll_j2n(env, epoll);

    return srt_epoll_release(eid);
}


// Logging control
JNIEXPORT void JNICALL
nativeSetLogLevel(JNIEnv *env, jobject obj, jint level) {
    srt_setloglevel((int) level);
}

// Register natives API
static JNINativeMethod srtMethods[] = {
        {"nativeStartUp",     "()I",  (void *) &nativeStartUp},
        {"nativeCleanUp",     "()I",  (void *) &nativeCleanUp},
        {"nativeSetLogLevel", "(I)V", (void *) &nativeSetLogLevel}
};

static JNINativeMethod socketMethods[] = {
        {"nativeSocket",       "(Ljava/net/StandardProtocolFamily;II)I",                        (void *) &nativeSocket},
        {"nativeCreateSocket", "()I",                                                           (void *) &nativeCreateSocket},
        {"nativeBind",         "(L" INETSOCKETADDRESS_CLASS ";)I",                              (void *) &nativeBind},
        {"nativeGetSockState", "()L" SOCKSTATUS_CLASS ";",                                      (void *) &nativeGetSockState},
        {"nativeClose",        "()I",                                                           (void *) &nativeClose},
        {"nativeListen",       "(I)I",                                                          (void *) &nativeListen},
        {"nativeAccept",       "()L" PAIR_CLASS ";",                                            (void *) &nativeAccept},
        {"nativeConnect",      "(L" INETSOCKETADDRESS_CLASS ";)I",                              (void *) &nativeConnect},
        {"nativeRendezVous",   "(L" INETSOCKETADDRESS_CLASS ";L" INETSOCKETADDRESS_CLASS ";)I", (void *) &nativeRendezVous},
        {"nativeGetPeerName",  "()L" INETSOCKETADDRESS_CLASS ";",                               (void *) &nativeGetPeerName},
        {"nativeGetSockName",  "()L" INETSOCKETADDRESS_CLASS ";",                               (void *) &nativeGetSockName},
        {"nativeGetSockOpt",   "(IL" SOCKOPT_CLASS ";)Ljava/lang/Object;",                      (void *) &nativeGetSockOpt},
        {"nativeSetSockOpt",   "(IL" SOCKOPT_CLASS ";Ljava/lang/Object;)I",                     (void *) &nativeSetSockOpt},
        {"nativeSend",         "([B)I",                                                         (void *) &nativeSend},
        {"nativeSendMsg",      "([BIZ)I",                                                       (void *) &nativeSendMsg},
        {"nativeSendMsg2",     "([BL" MSGCTRL_CLASS ";)I",                                      (void *) &nativeSendMsg2},
        {"nativeRecv",         "(I)[B",                                                         (void *) &nativeRecv},
        {"nativeRecvMsg2",     "(IL" MSGCTRL_CLASS ";)[B",                                      (void *) &nativeRecvMsg2},
        {"nativeSendFile",     "(Ljava/lang/String;JJI)J",                                      (void *) &nativeSendFile},
        {"nativeRecvFile",     "(Ljava/lang/String;JJI)J",                                      (void *) &nativeRecvFile}
};

static JNINativeMethod errorMethods[] = {
        {"nativeGetLastErrorStr", "()Ljava/lang/String;",    (void *) &nativeGetLastErrorStr},
        {"nativeGetLastError",    "()L" ERRORTYPE_CLASS ";", (void *) &nativeGetLastError},
        {"nativeClearLastError",  "()V",                     (void *) &nativeClearLastError}
};

static JNINativeMethod errorTypeMethods[] = {
        {"nativeStrError", "()Ljava/lang/String;", (void *) &nativeStrError}
};

static JNINativeMethod epollMethods[] = {
        {"nativeEpollCreate",      "()I",                                              (void *) &nativeEpollCreate},
        {"nativeEpollAddUSock",    "(L" SRTSOCKET_CLASS ";[L" EPOLLOPT_CLASS ";)I",    (void *) &nativeEpollAddUSock},
        {"nativeEpollUpdateUSock", "(L" SRTSOCKET_CLASS ";[L" EPOLLOPT_CLASS ";)I",    (void *) &nativeEpollUpdateUSock},
        {"nativeEpollRemoveUSock", "(L" SRTSOCKET_CLASS ";)I",                         (void *) &nativeEpollRemoveUSock},
        {"nativeEpollWait",        "([L" SRTSOCKET_CLASS ";[L" SRTSOCKET_CLASS ";J)I", (void *) &nativeEpollWait},
        {"nativeEpollUWait",       "([L" EPOLLEVENT_CLASS ";J)I",                      (void *) &nativeEpollUWait},
        {"nativeEpollSet",         "([L" EPOLLFLAG_CLASS ";)[L" EPOLLFLAG_CLASS ";",   (void *) &nativeEpollSet},
        {"nativeEpollGet",         "()[L" EPOLLFLAG_CLASS ";",                         (void *) &nativeEpollGet},
        {"nativeEpollRelease",     "()I",                                              (void *) &nativeEpollRelease}

};

static int registerNativeForClassName(JNIEnv *env, const char *className,
                                      JNINativeMethod *methods, int methodsSize) {
    jclass clazz = env->FindClass(className);
    if (clazz == nullptr) {
        LOGE(TAG, "Unable to find class '%s'", className);
        return JNI_FALSE;
    }

    if (env->RegisterNatives(clazz, methods, methodsSize) < 0) {
        LOGE(TAG, "RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

jint JNI_OnLoad(JavaVM *vm, void * /*reserved*/) {
    JNIEnv *env = nullptr;
    jint result;

    if ((result = vm->GetEnv((void **) &env, JNI_VERSION_1_4)) != JNI_OK) {
        LOGE(TAG, "GetEnv failed");
        return result;
    }

    if ((registerNativeForClassName(env, SRT_CLASS, srtMethods,
                                    sizeof(srtMethods) / sizeof(srtMethods[0])) != JNI_TRUE)) {
        LOGE(TAG, "SRT RegisterNatives failed");
        return -1;
    }

    if ((registerNativeForClassName(env, SRTSOCKET_CLASS, socketMethods,
                                    sizeof(socketMethods) / sizeof(socketMethods[0])) !=
         JNI_TRUE)) {
        LOGE(TAG, "Socket RegisterNatives failed");
        return -1;
    }

    if ((registerNativeForClassName(env, ERROR_CLASS, errorMethods,
                                    sizeof(errorMethods) / sizeof(errorMethods[0])) != JNI_TRUE)) {
        LOGE(TAG, "Error RegisterNatives failed");
        return -1;
    }

    if ((registerNativeForClassName(env, ERRORTYPE_CLASS, errorTypeMethods,
                                    sizeof(errorTypeMethods) / sizeof(errorTypeMethods[0])) !=
         JNI_TRUE)) {
        LOGE(TAG, "ErrorType RegisterNatives failed");
        return -1;
    }

    if ((registerNativeForClassName(env, EPOLL_CLASS, epollMethods,
                                    sizeof(epollMethods) / sizeof(epollMethods[0])) !=
         JNI_TRUE)) {
        LOGE(TAG, "Epoll RegisterNatives failed");
        return -1;
    }

    return JNI_VERSION_1_6;
}

#ifdef __cplusplus
}
#endif
