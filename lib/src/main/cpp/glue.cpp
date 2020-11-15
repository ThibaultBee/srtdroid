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

typedef struct callback_context {
    JavaVM *vm;
    jobject callingSocket;
    jclass sockAddrClazz;
} CallbackContext;

int onListenCallback(JNIEnv *env, jobject ju, jclass sockAddrClazz, SRTSOCKET ns, int hs_version,
                     const struct sockaddr *peeraddr, const char *streamid) {
    jclass socketClazz = env->GetObjectClass(ju);
    if (!socketClazz) {
        LOGE(TAG, "Can't get Socket class");
        return 0;
    }

    jmethodID onListenID = env->GetMethodID(socketClazz, "onListen",
                                            "(L" SRTSOCKET_CLASS ";IL" INETSOCKETADDRESS_CLASS ";Ljava/lang/String;)I");
    if (!onListenID) {
        LOGE(TAG, "Can't get onListen methodID");
        env->DeleteLocalRef(socketClazz);
        return 0;
    }

    jobject nsSocket = srt_socket_n2j(env, socketClazz, ns);
    jobject peerAddress = sockaddr_inet_n2j(env, sockAddrClazz,
                                            (sockaddr_storage *) peeraddr);
    jstring streamId = env->NewStringUTF(streamid);

    int res = env->CallIntMethod(ju, onListenID, nsSocket, (jint) hs_version, peerAddress,
                                 streamId);

    env->DeleteLocalRef(socketClazz);

    return res;
}

int srt_listen_cb(void *opaque, SRTSOCKET ns, int hs_version,
                  const struct sockaddr *peeraddr, const char *streamid) {
    CallbackContext *cbCtx = static_cast<CallbackContext *>(opaque);

    if (cbCtx == nullptr) {
        LOGE(TAG, "Failed to get CallbackContext");
        return 0;
    }

    JavaVM *vm = cbCtx->vm;
    JNIEnv *env = nullptr;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) == JNI_EDETACHED) {
        if (vm->AttachCurrentThread(&env, NULL) != JNI_OK) {
            LOGE(TAG, "Failed to attach current thread");
        }
    } else {
        LOGE(TAG, "Failed to get env");
    }


    int res = onListenCallback(env, cbCtx->callingSocket, cbCtx->sockAddrClazz, ns, hs_version,
                               peeraddr, streamid);

    vm->DetachCurrentThread();

    return res;
}

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
jint JNICALL
nativeStartUp(JNIEnv *env, jobject obj) {
    srt_setloghandler(nullptr, srt_logger_cb);
    return srt_startup();
}

jint JNICALL
nativeCleanUp(JNIEnv *env, jobject obj) {
    return srt_cleanup();
}

jint JNICALL
nativeGetVersion(JNIEnv *env, jobject obj) {
    return srt_getversion();
}

// Creating and configuring sockets
jboolean JNICALL
nativeIsValid(JNIEnv *env, jobject ju) {
    SRTSOCKET u = srt_socket_j2n(env, ju);

    return static_cast<jboolean>(u != SRT_INVALID_SOCK);
}

jint JNICALL SRT_ATR_DEPRECATED
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

jint JNICALL
nativeCreateSocket(JNIEnv *env, jobject obj) {
    return srt_create_socket();
}

jint JNICALL
nativeBind(JNIEnv *env, jobject ju, jobject inetSocketAddress) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    int size = 0;
    const struct sockaddr_storage *ss = sockaddr_inet_j2n(env, inetSocketAddress, &size);

    int res = srt_bind(u, reinterpret_cast<const struct sockaddr *>(ss), size);

    if (ss) {
        free((void *) ss);
    }

    return res;
}

jobject JNICALL
nativeGetSockState(JNIEnv *env, jobject ju) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    SRT_SOCKSTATUS sock_status = srt_getsockstate((SRTSOCKET) u);

    return srt_sockstatus_n2j(env, sock_status);
}

void JNICALL
nativeClose(JNIEnv *env, jobject ju) {
    SRTSOCKET u = srt_socket_j2n(env, ju);

    int res = srt_close((SRTSOCKET) u);
    if (res != 0) {
        LOGE(TAG, "Failed to close socket %d", u);
    }
}

// Connecting
jint JNICALL
nativeListen(JNIEnv *env, jobject ju, jint backlog) {
    SRTSOCKET u = srt_socket_j2n(env, ju);

    CallbackContext *cbCtx = static_cast<CallbackContext *>(malloc(sizeof(CallbackContext)));
    env->GetJavaVM(&(cbCtx->vm));
    cbCtx->sockAddrClazz = static_cast<jclass>(env->NewGlobalRef(
            env->FindClass(INETSOCKETADDRESS_CLASS)));
    cbCtx->callingSocket = env->NewGlobalRef(ju);
    srt_listen_callback(u, srt_listen_cb,
                        (void *) cbCtx); // TODO: free cbCtx but could not find a way to free callback opaque parameter
    return srt_listen((SRTSOCKET) u, (int) backlog);
}

jobject JNICALL
nativeAccept(JNIEnv *env, jobject ju) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    struct sockaddr_storage ss = {0};
    int sockaddr_len = sizeof(ss);
    jobject inetSocketAddress = nullptr;

    SRTSOCKET new_u = srt_accept((SRTSOCKET) u, reinterpret_cast<struct sockaddr *>(&ss),
                                 &sockaddr_len);
    if (new_u != -1) {
        inetSocketAddress = sockaddr_inet_n2j(env, nullptr, &ss);
    }

    jobject res = pair_new(env, srt_socket_n2j(env, nullptr, new_u),
                           inetSocketAddress);

    return res;
}

jint JNICALL
nativeConnect(JNIEnv *env, jobject ju, jobject inetSocketAddress) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    int size = 0;
    const struct sockaddr_storage *ss = sockaddr_inet_j2n(env, inetSocketAddress, &size);

    int res = srt_connect((SRTSOCKET) u, reinterpret_cast<const sockaddr *>(ss), size);

    if (ss) {
        free((void *) ss);
    }

    return res;
}

jint JNICALL
nativeRendezVous(JNIEnv *env, jobject ju, jobject localAddress, jobject remoteAddress) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    int local_addr_size = 0, remote_addr_size = 0;
    const struct sockaddr_storage *local_ss = sockaddr_inet_j2n(env, localAddress,
                                                                &local_addr_size);
    const struct sockaddr_storage *remote_ss = sockaddr_inet_j2n(env, remoteAddress,
                                                                 &remote_addr_size);
    int res = srt_rendezvous((SRTSOCKET) u, reinterpret_cast<const sockaddr *>(local_ss),
                             local_addr_size, reinterpret_cast<const sockaddr *>(remote_ss),
                             remote_addr_size);

    if (local_ss) {
        free((void *) local_ss);
    }

    if (remote_ss) {
        free((void *) remote_ss);
    }

    return res;
}

// Options and properties
jobject JNICALL
nativeGetPeerName(JNIEnv *env, jobject ju) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    struct sockaddr_storage ss = {0};
    int sockaddr_len = sizeof(ss);
    jobject inetSocketAddress = nullptr;

    int res = srt_getpeername((SRTSOCKET) u, reinterpret_cast<struct sockaddr *>(&ss), &sockaddr_len);
    if (res == 0) {
        inetSocketAddress = sockaddr_inet_n2j(env, nullptr, &ss);
    }

    return inetSocketAddress;
}

jobject JNICALL
nativeGetSockName(JNIEnv *env, jobject ju) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    struct sockaddr_storage ss = {0};
    int sockaddr_len = sizeof(ss);
    jobject inetSocketAddress = nullptr;

    int res = srt_getsockname((SRTSOCKET) u, reinterpret_cast<struct sockaddr *>(&ss), &sockaddr_len);
    if (res == 0) {
        inetSocketAddress = sockaddr_inet_n2j(env, nullptr, &ss);
    }

    return inetSocketAddress;
}

jobject JNICALL
nativeGetSockOpt(JNIEnv *env,
                 jobject ju,
                 jobject sockOpt) {
    SRTSOCKET u = srt_socket_j2n(env, ju);

    jobject optVal = srt_optval_n2j(env, u, 0 /*level: ignored*/, sockOpt);

    return optVal;
}

jint JNICALL
nativeSetSockOpt(JNIEnv *env,
                 jobject ju,
                 jobject sockOpt,
                 jobject optVal) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    int sockopt = srt_sockopt_j2n(env, sockOpt);
    if (sockopt <= 0) {
        return sockopt;
    }
    int optval_len = 0;
    const void *optval = srt_optval_j2n(env, optVal, &optval_len);

    if (!optval) {
        return -EFAULT;
    }

    int res = srt_setsockopt((SRTSOCKET) u,
                             0 /*level: ignored*/, (SRT_SOCKOPT) sockopt, optval, optval_len);
    free((void *) optval);

    return
            res;
}

// Transmission
jint JNICALL
nativeSend(JNIEnv *env, jobject ju, jbyteArray byteArray) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    int len = env->GetArrayLength(byteArray);
    char *buf = (char *) env->GetByteArrayElements(byteArray, nullptr);

    int res = srt_send(u, buf, len);

    env->ReleaseByteArrayElements(byteArray, (jbyte *) buf, 0);

    return res;
}

jint JNICALL
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

jint JNICALL
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

jobject JNICALL
nativeRecv(JNIEnv *env, jobject ju, jint len) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    jbyteArray byteArray;
    auto *buf = (char *) malloc(sizeof(char) * len);

    int res = srt_recv(u, buf, len);

    if (res > 0) {
        byteArray = env->NewByteArray(res);
        env->SetByteArrayRegion(byteArray, 0, res, (jbyte *) buf);
    } else {
        byteArray = env->NewByteArray(0);
    }

    if (buf != nullptr) {
        free(buf);
    }

    return pair_new(env, int_new(env, res), byteArray);
}

jobject JNICALL
nativeRecvMsg2(JNIEnv *env,
               jobject ju,
               jint len,
               jobject msgCtrl) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    SRT_MSGCTRL *msgctrl = srt_msgctrl_j2n(env, msgCtrl);
    jbyteArray byteArray;
    auto *buf = (char *) malloc(sizeof(char) * len);

    int res = srt_recvmsg2(u, buf, len, msgctrl);

    if (res > 0) {
        byteArray = env->NewByteArray(res);
        env->SetByteArrayRegion(byteArray, 0, res, (jbyte *) buf);
    } else {
        byteArray = env->NewByteArray(0);
    }

    if (buf != nullptr) {
        free(buf);
    }
    if (msgctrl != nullptr) {
        free(msgctrl);
    }

    return pair_new(env, int_new(env, res), byteArray);
}

jlong JNICALL
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

jlong JNICALL
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
jstring JNICALL
nativeGetLastErrorStr(JNIEnv *env, jobject obj) {
    return env->NewStringUTF(srt_getlasterror_str());
}

jobject JNICALL
nativeGetLastError(JNIEnv *env, jobject obj) {
    int err = srt_getlasterror(nullptr);

    return srt_error_n2j(env, err);
}

jstring JNICALL
nativeStrError(JNIEnv *env, jobject obj) {
    int error_type = srt_error_j2n(env, obj);
    return env->NewStringUTF(srt_strerror(error_type, 0));
}

void JNICALL
nativeClearLastError(JNIEnv *env, jobject obj) {
    srt_clearlasterror();
}


// Reject reason
jint JNICALL
nativeGetRejectReason(JNIEnv *env, jobject ju) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    return srt_getrejectreason(u);
}

jstring JNICALL
nativeRejectReasonStr(JNIEnv *env, jobject obj) {
    int reject_reason = srt_reject_reason_j2n(env, obj);
    return env->NewStringUTF(srt_rejectreason_str(reject_reason));
}

jint JNICALL
nativeSetRejectReason(JNIEnv *env, jobject ju, jint rejectReason) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    return srt_setrejectreason(u, rejectReason);
}


// Performance tracking
jobject JNICALL
nativebstats(JNIEnv *env, jobject ju, jboolean clear) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    SRT_TRACEBSTATS tracebstats;

    srt_bstats(u, &tracebstats, clear);

    return srt_stats_n2j(env, tracebstats);
}

jobject JNICALL
nativebistats(JNIEnv *env, jobject ju, jboolean clear, jboolean instantaneous) {
    SRTSOCKET u = srt_socket_j2n(env, ju);
    SRT_TRACEBSTATS tracebstats;

    srt_bistats(u, &tracebstats, clear, instantaneous);

    return srt_stats_n2j(env, tracebstats);
}

// Asynchronous operations (epoll)
jboolean JNICALL
nativeEpollIsValid(JNIEnv *env, jobject epoll) {
    int eid = srt_epoll_j2n(env, epoll);

    return static_cast<jboolean>(eid != -1);
}

jint JNICALL
nativeEpollCreate(JNIEnv *env, jobject epoll) {
    return srt_epoll_create();
}

jint JNICALL
nativeEpollAddUSock(JNIEnv *env, jobject epoll, jobject ju, jobject epollEventList) {
    int eid = srt_epoll_j2n(env, epoll);
    SRTSOCKET u = srt_socket_j2n(env, ju);

    if (epollEventList) {
        int events = srt_epoll_opts_j2n(env, epollEventList);
        return srt_epoll_add_usock(eid, u, &events);
    } else {
        return srt_epoll_add_usock(eid, u, nullptr);
    }
}

jint JNICALL
nativeEpollUpdateUSock(JNIEnv *env, jobject epoll, jobject ju, jobject epollEventList) {
    int eid = srt_epoll_j2n(env, epoll);
    SRTSOCKET u = srt_socket_j2n(env, ju);

    if (epollEventList) {
        int events = srt_epoll_opts_j2n(env, epollEventList);
        return srt_epoll_update_usock(eid, u, &events);
    } else {
        return srt_epoll_update_usock(eid, u, nullptr);
    }
}

jint JNICALL
nativeEpollRemoveUSock(JNIEnv *env, jobject epoll, jobject ju) {
    int eid = srt_epoll_j2n(env, epoll);
    SRTSOCKET u = srt_socket_j2n(env, ju);

    return srt_epoll_remove_usock(eid, u);
}

jint JNICALL
nativeEpollWait(JNIEnv *env, jobject epoll, jobject readFdsList, jobject writeFdsList,
                jlong timeOut) {
    int eid = srt_epoll_j2n(env, epoll);
    int nReadFds = 0;
    int nWriteFds = 0;
    SRTSOCKET *readfds = nullptr;
    SRTSOCKET *writefds = nullptr;

    if (readFdsList) {
        readfds = srt_sockets_j2n(env, readFdsList, &nReadFds);
    }
    if (writeFdsList) {
        writefds = srt_sockets_j2n(env, writeFdsList, &nWriteFds);
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

jint JNICALL
nativeEpollUWait(JNIEnv *env, jobject epoll, jobject fdsList, jlong timeOut) {
    int eid = srt_epoll_j2n(env, epoll);
    SRT_EPOLL_EVENT *epoll_events = nullptr;
    int nEpollEvents = 0;

    if (fdsList) {
        epoll_events = srt_epoll_events_j2n(env, fdsList, &nEpollEvents);
    }

    int res = srt_epoll_uwait(eid, epoll_events, nEpollEvents, timeOut);

    if (epoll_events != nullptr) {
        free(epoll_events);
    }

    return res;
}


jobject JNICALL
nativeEpollSet(JNIEnv *env, jobject epoll, jobject epollFlagList) {
    int eid = srt_epoll_j2n(env, epoll);
    int32_t flags = srt_epoll_flags_j2n(env, epollFlagList);

    flags = srt_epoll_set(eid, flags);

    return srt_epoll_flags_n2j(env, flags);
}

jobject JNICALL
nativeEpollGet(JNIEnv *env, jobject epoll) {
    int eid = srt_epoll_j2n(env, epoll);

    int32_t flags = srt_epoll_set(eid, -1);

    return srt_epoll_flags_n2j(env, flags);
}

jint JNICALL
nativeEpollRelease(JNIEnv *env, jobject epoll) {
    int eid = srt_epoll_j2n(env, epoll);

    int res = srt_epoll_release(eid);
    srt_epoll_set_eid(env, epoll, -1);

    return res;
}


// Logging control
void JNICALL
nativeSetLogLevel(JNIEnv *env, jobject obj, jint level) {
    srt_setloglevel((int) level);
}


// Time access
jlong JNICALL
nativeNow(JNIEnv *env, jobject obj) {
    return (jlong) srt_time_now();
}

jlong JNICALL
nativeGetConnectionTime(JNIEnv *env,
                        jobject ju) {
    SRTSOCKET u = srt_socket_j2n(env, ju);

    return (jlong) srt_connection_time(u);
}


// Register natives API
static JNINativeMethod srtMethods[] = {
        {"startUp",     "()I",  (void *) &nativeStartUp},
        {"cleanUp",     "()I",  (void *) &nativeCleanUp},
        {"getVersion",  "()I",  (void *) &nativeGetVersion},
        {"setLogLevel", "(I)V", (void *) &nativeSetLogLevel}
};

static JNINativeMethod socketMethods[] = {
        {"isValid",                 "()Z",                                                           (void *) &nativeIsValid},
        {"socket",                  "(Ljava/net/StandardProtocolFamily;II)I",                        (void *) &nativeSocket},
        {"createSocket",            "()I",                                                           (void *) &nativeCreateSocket},
        {"bind",                    "(L" INETSOCKETADDRESS_CLASS ";)I",                              (void *) &nativeBind},
        {"nativeGetSockState",      "()L" SOCKSTATUS_CLASS ";",                                      (void *) &nativeGetSockState},
        {"close",                   "()V",                                                           (void *) &nativeClose},
        {"listen",                  "(I)I",                                                          (void *) &nativeListen},
        {"accept",                  "()L" PAIR_CLASS ";",                                            (void *) &nativeAccept},
        {"connect",                 "(L" INETSOCKETADDRESS_CLASS ";)I",                              (void *) &nativeConnect},
        {"rendezVous",              "(L" INETSOCKETADDRESS_CLASS ";L" INETSOCKETADDRESS_CLASS ";)I", (void *) &nativeRendezVous},
        {"nativeGetPeerName",       "()L" INETSOCKETADDRESS_CLASS ";",                               (void *) &nativeGetPeerName},
        {"nativeGetSockName",       "()L" INETSOCKETADDRESS_CLASS ";",                               (void *) &nativeGetSockName},
        {"getSockFlag",             "(L" SOCKOPT_CLASS ";)Ljava/lang/Object;",                       (void *) &nativeGetSockOpt},
        {"setSockFlag",             "(L" SOCKOPT_CLASS ";Ljava/lang/Object;)I",                      (void *) &nativeSetSockOpt},
        {"send",                    "([B)I",                                                         (void *) &nativeSend},
        {"sendMsg",                 "([BIZ)I",                                                       (void *) &nativeSendMsg},
        {"sendMsg2",                "([BL" MSGCTRL_CLASS ";)I",                                      (void *) &nativeSendMsg2},
        {"recv",                    "(I)L" PAIR_CLASS ";",                                           (void *) &nativeRecv},
        {"recvMsg2",                "(IL" MSGCTRL_CLASS ";)L" PAIR_CLASS ";",                        (void *) &nativeRecvMsg2},
        {"sendFile",                "(Ljava/lang/String;JJI)J",                                      (void *) &nativeSendFile},
        {"recvFile",                "(Ljava/lang/String;JJI)J",                                      (void *) &nativeRecvFile},
        {"nativeGetRejectReason",   "()I",                                                           (void *) &nativeGetRejectReason},
        {"nativeSetRejectReason",   "(I)I",                                                          (void *) &nativeSetRejectReason},
        {"bstats",                  "(Z)L" STATS_CLASS ";",                                          (void *) &nativebstats},
        {"bistats",                 "(ZZ)L" STATS_CLASS ";",                                         (void *) &nativebistats},
        {"nativeGetConnectionTime", "()J",                                                           (void *) &nativeGetConnectionTime}
};

static JNINativeMethod rejectReasonMethods[] = {
        {"toString", "()Ljava/lang/String;", (void *) &nativeRejectReasonStr}
};

static JNINativeMethod errorMethods[] = {
        {"nativeGetLastErrorMessage", "()Ljava/lang/String;",    (void *) &nativeGetLastErrorStr},
        {"nativeGetLastError",        "()L" ERRORTYPE_CLASS ";", (void *) &nativeGetLastError},
        {"clearLastError",            "()V",                     (void *) &nativeClearLastError}
};

static JNINativeMethod errorTypeMethods[] = {
        {"toString", "()Ljava/lang/String;", (void *) &nativeStrError}
};

static JNINativeMethod timeMethods[] = {
        {"now", "()J", (void *) &nativeNow}
};

static JNINativeMethod epollMethods[] = {
        {"isValid",     "()Z",                                      (void *) &nativeEpollIsValid},
        {"create",      "()I",                                      (void *) &nativeEpollCreate},
        {"addUSock",    "(L" SRTSOCKET_CLASS ";L" LIST_CLASS ";)I", (void *) &nativeEpollAddUSock},
        {"updateUSock", "(L" SRTSOCKET_CLASS ";L" LIST_CLASS ";)I", (void *) &nativeEpollUpdateUSock},
        {"removeUSock", "(L" SRTSOCKET_CLASS ";)I",                 (void *) &nativeEpollRemoveUSock},
        {"wait",        "(L" LIST_CLASS ";L" LIST_CLASS ";J)I",     (void *) &nativeEpollWait},
        {"uWait",       "(L" LIST_CLASS ";J)I",                     (void *) &nativeEpollUWait},
        {"set",         "(L" LIST_CLASS ";)L" LIST_CLASS ";",       (void *) &nativeEpollSet},
        {"get",         "()L" LIST_CLASS ";",                       (void *) &nativeEpollGet},
        {"release",     "()I",                                      (void *) &nativeEpollRelease}
};

static int registerNativeForClassName(JNIEnv *env, const char *className,
                                      JNINativeMethod *methods, int methodsSize) {
    jclass clazz = env->FindClass(className);
    if (clazz == nullptr) {
        LOGE(TAG, "Unable to find class '%s'", className);
        return JNI_FALSE;
    }
    int res = 0;
    if ((res = env->RegisterNatives(clazz, methods, methodsSize)) < 0) {
        LOGE(TAG, "RegisterNatives failed for '%s' (reason %d)", className, res);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

jint JNI_OnLoad(JavaVM *vm, void * /*reserved*/) {
    JNIEnv *env = nullptr;
    jint result;

    if ((result = vm->GetEnv((void **) &env, JNI_VERSION_1_6)) != JNI_OK) {
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

    if ((registerNativeForClassName(env, REJECT_REASON_CLASS, rejectReasonMethods,
                                    sizeof(rejectReasonMethods) / sizeof(rejectReasonMethods[0])) !=
         JNI_TRUE)) {
        LOGE(TAG, "RejectReason RegisterNatives failed");
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

    if ((registerNativeForClassName(env, TIME_CLASS, timeMethods,
                                    sizeof(timeMethods) / sizeof(timeMethods[0])) !=
         JNI_TRUE)) {
        LOGE(TAG, "Time RegisterNatives failed");
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
