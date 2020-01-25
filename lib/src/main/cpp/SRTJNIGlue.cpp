#include <jni.h>
#include <string>
#include <cerrno>
#include <cstring>
#include <android/log.h>
#include <sys/socket.h>
#include <cstdlib>

#include "srt/srt.h"
#include "srt/logging_api.h"

extern "C" {

#define ENUM_PACKAGE "com.github.thibaultbee.srtwrapper.enums"

#define SRTSOCKET_CLASS "com/github/thibaultbee/srtwrapper/models/Socket"
#define INETSOCKETADDRESS_CLASS "java/net/InetSocketAddress"
#define PAIR_CLASS "android/util/Pair"


#define TAG "JNIGlue"
#define SRTLIB_TAG "SRTLIB"

// Log tools
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

// Enums
/**
 * @brief Get the name of a Java enumeration member
 *
 * @param env Java environment
 * @param jenum_value Java enumeration member
 * @return return Java String that contains Java enum name
 */
jstring java_enum_get_value_name(JNIEnv *env, jobject jenum_value) {
    jclass jenum_class = env->GetObjectClass(jenum_value);
    if (!jenum_class) {
        return nullptr;
    }

    jmethodID jenum_name_method = env->GetMethodID(jenum_class, "name", "()Ljava/lang/String;");
    if (!jenum_name_method) {
        return nullptr;
    }

    env->DeleteLocalRef(jenum_class);

    return (jstring) env->CallObjectMethod(jenum_value, jenum_name_method);
}

/**
 * @brief Convert Java address family to native value
 *
 * @param env Java environment
 * @param jenum_af_value Java address family enumeration member
 * @return return corresponding native address family value
 */
int address_family_from_java_to_native(JNIEnv *env, jobject jenum_af_value) {
    jstring jenum_name = java_enum_get_value_name(env, jenum_af_value);
    if (!jenum_name) {
        LOGE("Can't get Java address family enum name");
        return -EFAULT;
    }

    auto enum_name = (char *) env->GetStringUTFChars(jenum_name, nullptr);
    if (!enum_name) {
        LOGE("Can't get address family enum name");
        return -EFAULT;
    }

    int af = AF_UNSPEC;
    if (strcmp(enum_name, "INET") == 0) {
        af = AF_INET;
    } else if (strcmp(enum_name, "INET6") == 0) {
        af = AF_INET6;
    } else {
        LOGE("Unknown value %s", enum_name);
    }

    env->ReleaseStringUTFChars(jenum_name, enum_name);

    return af;
}

/**
 * @brief Convert Java SRT Option to native value
 *
 * @param env Java environment
 * @param jenum_srt_option_value Java SRT Option enumeration member
 * @return return corresponding native SRT option value
 */
int srt_sockopt_from_java_to_native(JNIEnv *env, jobject jenum_srt_option_value) {
    jstring jenum_name = java_enum_get_value_name(env, jenum_srt_option_value);
    if (!jenum_name) {
        LOGE("Can't get Java SRT option enum name");
        return -EFAULT;
    }

    auto enum_name = (char *) env->GetStringUTFChars(jenum_name, nullptr);
    if (!enum_name) {
        LOGE("Can't get SRT option enum name");
        return -EFAULT;
    }

    int srtopt = -1;
    if (strcmp(enum_name, "TRANSTYPE") == 0) {
        srtopt = SRTO_TRANSTYPE;
    } else if (strcmp(enum_name, "SENDER") == 0) {
        srtopt = SRTO_SENDER;
    } else {
        LOGE("Unknown value %s", enum_name);
    }

    env->ReleaseStringUTFChars(jenum_name, enum_name);

    return srtopt;
}

/**
 * @brief Convert Java SRT Option to native value
 *
 * @param env Java environment
 * @param jenum_srt_option_value Java SRT Option enumeration member
 * @return return corresponding native SRT option value
 */
SRT_TRANSTYPE srt_transtype_from_java_to_native(JNIEnv *env, jobject jenum_srt_transtype_value) {
    jstring jenum_name = java_enum_get_value_name(env, jenum_srt_transtype_value);
    if (!jenum_name) {
        LOGE("Can't get Java SRT transtype enum name");
        return SRTT_INVALID;
    }

    auto enum_name = (char *) env->GetStringUTFChars(jenum_name, nullptr);
    if (!enum_name) {
        LOGE("Can't get SRT transtype enum name");
        return SRTT_INVALID;
    }

    SRT_TRANSTYPE transtype = SRTT_INVALID;
    if (strcmp(enum_name, "LIVE") == 0) {
        transtype = SRTT_LIVE;
    } else if (strcmp(enum_name, "FILE") == 0) {
        transtype = SRTT_FILE;
    } else {
        LOGE("Unknown value %s", enum_name);
    }

    env->ReleaseStringUTFChars(jenum_name, enum_name);

    return transtype;
}

// Struct
/**
 * @brief Convert Java InetSocketAddres to sockaddr_in
 *
 * @param env Java environment
 * @param jinet_socket_address Java InetSocketAddres
 * @return return sockaddress in C domain
 */
struct sockaddr_in *
inet_socket_address_from_java_to_native(JNIEnv *env, jobject jinet_socket_address) {
    // Get InetSocketAddress class
    jclass jinet_socket_address_class = env->GetObjectClass(jinet_socket_address);
    if (!jinet_socket_address_class) {
        LOGE("Can't get InetSocketAddress class");
        return nullptr;
    }

    // Port
    jmethodID jgetPort_method = env->GetMethodID(jinet_socket_address_class, "getPort", "()I");
    if (!jgetPort_method) {
        LOGE("Can't get getPort method");
        env->DeleteLocalRef(jinet_socket_address_class);
        return nullptr;
    }
    int port = env->CallIntMethod(jinet_socket_address, jgetPort_method);

    // Hostname
    jmethodID jgetHostName_method = env->GetMethodID(jinet_socket_address_class, "getHostString",
                                                     "()Ljava/lang/String;");
    if (!jgetHostName_method) {
        LOGE("Can't get getHostString method");
        env->DeleteLocalRef(jinet_socket_address_class);
        return nullptr;
    }

    auto jhostname = (jstring) env->CallObjectMethod(jinet_socket_address,
                                                     jgetHostName_method);
    if (!jhostname) {
        LOGE("Can't get Hostname");
        env->DeleteLocalRef(jinet_socket_address_class);
        return nullptr;
    }

    const char *hostname = env->GetStringUTFChars(jhostname, nullptr);

    auto *sa = (struct sockaddr_in *) malloc(sizeof(struct sockaddr_in));

    sa->sin_port = htons(port);
    sa->sin_family = AF_INET;
    if(inet_pton(sa->sin_family, hostname, &sa->sin_addr) != 1) {
        LOGE("Can't convert sock addr");
    }

    env->ReleaseStringUTFChars(jhostname, hostname);
    env->DeleteLocalRef(jinet_socket_address_class);

    return sa;
}

/**
 * @brief Convert sockaddr_in to Java InetSocketAddres
 *
 * @param env Java environment
 * @param sockaddr socket address in C domain
 * @param addrlen socket address length
 * @return return Java InetSocketAddres
 */
jobject
inet_socket_address_from_native_to_java(JNIEnv *env, struct sockaddr_in * sa, int addrlen) {
    // Get InetSocketAddress class
    jclass jinet_socket_address_class = env->FindClass(INETSOCKETADDRESS_CLASS);
    if (!jinet_socket_address_class) {
        LOGE("Can't get InetSocketAddress class");
        return nullptr;
    }

    jmethodID jinit_method = env->GetMethodID(jinet_socket_address_class, "<init>", "(Ljava/lang/String;I)V");
    if (!jinit_method) {
        LOGE("Can't get InetSocketAddress constructor field");
        env->DeleteLocalRef(jinet_socket_address_class);
        return nullptr;
    }

    char ip[INET_ADDRSTRLEN];
    if(inet_ntop(sa->sin_family, (void *)&(sa->sin_addr), ip, sizeof(ip))) {
            LOGE("Can't convert ip");
    }

    jstring jhostname = env->NewStringUTF(ip);
    jobject jinet_socket_address = env->NewObject(jinet_socket_address_class, jinit_method, jhostname, (jint) htons(sa->sin_port));

    env->DeleteLocalRef(jinet_socket_address_class);

    return jinet_socket_address;
}

/**
 * @brief Convert Java SRT Optval to C optval for SRT library
 *
 * @param env Java environment
 * @param jopval Java SRT optval
 * @param optlen length of C optval output
 * @return return optval in C domain
 */
void *
srt_optval_from_java_to_native(JNIEnv *env, jobject jopval, int *optlen) {
    void *res = nullptr;

    if (optlen == nullptr) {
        LOGE("Can't get optlen");
        return nullptr;
    }

    jclass jopval_class = env->GetObjectClass(jopval);
    if (!jopval_class) {
        LOGE("Can't get optval class");
        return nullptr;
    }

    // As joptval could be an Int, String,... First step is to get class object.
    // First get the class object
    jmethodID jgetClass_method = env->GetMethodID(jopval_class, "getClass", "()Ljava/lang/Class;");
    if (!jgetClass_method) {
        LOGE("Can't get getClass method");
        env->DeleteLocalRef(jopval_class);
        return nullptr;
    }

    jobject jclassObject = env->CallObjectMethod(jopval, jgetClass_method);
    if (!jclassObject) {
        LOGE("Can't get object class");
        env->DeleteLocalRef(jopval_class);
        return nullptr;
    }

    jclass jclassClass = env->GetObjectClass(jclassObject);
    if (!jclassClass) {
        LOGE("Can't get class");
        env->DeleteLocalRef(jopval_class);
        return nullptr;
    }

    // Then get class name
    jmethodID jgetName_method = env->GetMethodID(jclassClass, "getName", "()Ljava/lang/String;");
    if (!jgetName_method) {
        LOGE("Can't get getName method");
        env->DeleteLocalRef(jopval_class);
        return nullptr;
    }

    auto jclassName = (jstring) env->CallObjectMethod(jclassObject, jgetName_method);
    if (!jclassName) {
        LOGE("Can't get class name");
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
        res = malloc(*optlen);
        *(int *)res = transtype;
    } else if (strcmp(class_name, "java.lang.Integer") == 0) {
        jmethodID jintValue_method = env->GetMethodID(jopval_class, "intValue", "()I");
        if (!jintValue_method) {
            LOGE("Can't get intValue method");
            return nullptr;
        }
        res = malloc(sizeof(int));
        *(int *)res = env->CallIntMethod(jopval, jintValue_method);
    } else {
        LOGE("Unknown class %s", class_name);
    }

    env->ReleaseStringUTFChars(jclassName, class_name);
    env->DeleteLocalRef(jopval_class);

    return res;
}

/**
 * @brief Convert Java MsgCtrl to C SRT_MSGCTRL for SRT library
 *
 * @param env Java environment
 * @param jopval Java MsgCtrl
 * @return return MsgCtrl in C domain
 */
SRT_MSGCTRL *
srt_msgctrl_from_java_to_native(JNIEnv *env, jobject jmsgCtrl) {
    SRT_MSGCTRL *res = nullptr;

    if (jmsgCtrl == nullptr)
        return nullptr;

    jclass jmsgctrl_class = env->GetObjectClass(jmsgCtrl);
    if (!jmsgctrl_class) {
        LOGE("Can't get MsgCtrl class");
        return nullptr;
    }

    jfieldID jflags_field = env->GetFieldID(jmsgctrl_class, "flags", "I");
    if (!jflags_field) {
        LOGE("Can't get flags field");
        env->DeleteLocalRef(jmsgctrl_class);
        return nullptr;
    }

    jfieldID jttl_field = env->GetFieldID(jmsgctrl_class, "ttl", "I");
    if (!jttl_field) {
        LOGE("Can't get ttl field");
        env->DeleteLocalRef(jmsgctrl_class);
        return nullptr;
    }

    jfieldID jinOrder_field = env->GetFieldID(jmsgctrl_class, "inOrder", "Z");
    if (!jinOrder_field) {
        LOGE("Can't get inOrder field");
        env->DeleteLocalRef(jmsgctrl_class);
        return nullptr;
    }

    jfieldID jboundary_field = env->GetFieldID(jmsgctrl_class, "boundary", "I");
    if (!jboundary_field) {
        LOGE("Can't get boundary field");
        env->DeleteLocalRef(jmsgctrl_class);
        return nullptr;
    }

    jfieldID jsrcTime_field = env->GetFieldID(jmsgctrl_class, "srcTime", "J");
    if (!jsrcTime_field) {
        LOGE("Can't get srcTime field");
        env->DeleteLocalRef(jmsgctrl_class);
        return nullptr;
    }

    jfieldID jpktSeq_field = env->GetFieldID(jmsgctrl_class, "pktSeq", "I");
    if (!jpktSeq_field) {
        LOGE("Can't get pktSeq field");
        env->DeleteLocalRef(jmsgctrl_class);
        return nullptr;
    }

    jfieldID jmsgno_field = env->GetFieldID(jmsgctrl_class, "no", "I");
    if (!jmsgno_field) {
        LOGE("Can't get message number field");
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

// Utils
/**
 * @brief Convert Java SRT Socket to SRTSOCKET for SRT library
 *
 * @param env Java environment
 * @param u Java SRT Socket
 * @return return corresponding SRTSOCKET value
 */
SRTSOCKET srt_socket_from_java_to_native(JNIEnv *env, jobject ju) {
    jclass jsocket_class = env->GetObjectClass(ju);
    if (!jsocket_class) {
        LOGE("Can't get socket class");
        return SRT_INVALID_SOCK;
    }

    jfieldID jsrtsocket_field = env->GetFieldID(jsocket_class, "srtsocket", "I");
    if (!jsrtsocket_field) {
        LOGE("Can't get srtsocket field");
        env->DeleteLocalRef(jsocket_class);
        return SRT_INVALID_SOCK;
    }

    jint srtsocket = env->GetIntField(ju, jsrtsocket_field);

    env->DeleteLocalRef(jsocket_class);
    return srtsocket;
}

/**
 * @brief Convert SRTSOCKET for SRT library to Java SRT Socket
 *
 * @param env Java environment
 * @param u Java SRT Socket
 * @return return corresponding SRTSOCKET value
 */
jobject srt_socket_from_native_to_java(JNIEnv *env, SRTSOCKET u) {
    jclass jsocket_class = env->FindClass(SRTSOCKET_CLASS);
    if (!jsocket_class) {
        LOGE("Can't find socket class");
        return nullptr;
    }

    jmethodID jinit_method = env->GetMethodID(jsocket_class, "<init>", "(I)V");
    if (!jinit_method) {
        LOGE("Can't get SrtSocket constructor field");
        env->DeleteLocalRef(jsocket_class);
        return nullptr;
    }

    jobject ju = env->NewObject(jsocket_class, jinit_method, u);

    env->DeleteLocalRef(jsocket_class);
    return ju;
}

/**
 * @brief Create a Pair Java object
 *
 * @param env Java environment
 * @param first Pair first argument
 * @param second Pair second argument
 * @return return a Pair Java object containing first and second arguments
 */
jobject create_java_pair(JNIEnv *env, jobject first, jobject second) {
    jclass jpair_class = env->FindClass(PAIR_CLASS);
    if (!jpair_class) {
        LOGE("Can't get Pair class");
        return nullptr;
    }

    jmethodID jinit_method = env->GetMethodID(jpair_class, "<init>", "(Ljava/lang/Object;Ljava/lang/Object;)V");
    if (!jinit_method) {
        LOGE("Can't get Pair constructor field");
        env->DeleteLocalRef(jpair_class);
        return nullptr;
    }

    jobject jpair = env->NewObject(jpair_class, jinit_method, first, second);

    env->DeleteLocalRef(jpair_class);
    return jpair;
}

// Logger
void srt_logger(void *opaque, int level, const char *file, int line, const char *area,
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
            LOGE("Unknown log level %d", level);
    }

    __android_log_print(android_log_level, SRTLIB_TAG, "%s@%d:%s %s", file, line, area, message);
}

// Library Initialization
JNIEXPORT jint JNICALL
Java_com_github_thibaultbee_srtwrapper_Srt_nativeStartUp(JNIEnv *env, jobject obj) {
    srt_setloghandler(nullptr, srt_logger);
    return srt_startup();
}

JNIEXPORT jint JNICALL
Java_com_github_thibaultbee_srtwrapper_Srt_nativeCleanUp(JNIEnv *env, jobject obj) {
    return srt_cleanup();
}

// Creating and configuring sockets
JNIEXPORT jint JNICALL
Java_com_github_thibaultbee_srtwrapper_models_Socket_nativeSocket(JNIEnv *env, jobject obj,
                                                                  jobject jaf,
                                                                  jint jtype,
                                                                  jint jprotocol) {
    int af = address_family_from_java_to_native(env, jaf);
    if (af <= 0) {
        LOGE("Bad value for address family");
        return af;
    }

    return srt_socket(af, jtype, jprotocol);
}

JNIEXPORT jint JNICALL
Java_com_github_thibaultbee_srtwrapper_models_Socket_nativeCreateSocket(JNIEnv *env, jobject obj,
                                                                        jint af, jint type,
                                                                        jint protocol) {
    return srt_create_socket();
}

JNIEXPORT jint JNICALL
Java_com_github_thibaultbee_srtwrapper_models_Socket_nativeBind(JNIEnv *env, jobject ju,
                                                                jobject inetSocketAddress) {
    SRTSOCKET u = srt_socket_from_java_to_native(env, ju);
    const struct sockaddr_in *sa = inet_socket_address_from_java_to_native(env,
                                                                           inetSocketAddress);

    int res = srt_bind(u, (struct sockaddr *) sa, sizeof(*sa));

    if (!sa) {
        free((void *) sa);
    }

    return res;
}

JNIEXPORT jint JNICALL
Java_com_github_thibaultbee_srtwrapper_models_Socket_nativeClose(JNIEnv *env,
                                                                 jobject ju) {
    SRTSOCKET u = srt_socket_from_java_to_native(env, ju);

    return srt_close((SRTSOCKET) u);
}

// Connecting
JNIEXPORT jint JNICALL
Java_com_github_thibaultbee_srtwrapper_models_Socket_nativeListen(JNIEnv *env,
                                                                  jobject ju,
                                                                  jint backlog) {
    SRTSOCKET u = srt_socket_from_java_to_native(env, ju);

    return srt_listen((SRTSOCKET) u, (int) backlog);
}

JNIEXPORT jobject JNICALL
Java_com_github_thibaultbee_srtwrapper_models_Socket_nativeAccept(JNIEnv *env, jobject ju) {
    SRTSOCKET u = srt_socket_from_java_to_native(env, ju);
    auto *sa =  (struct sockaddr_in *) malloc(sizeof(struct sockaddr));
    int addrlen = 0;
    jobject inetSocketAddress = nullptr;

    SRTSOCKET new_u = srt_accept((SRTSOCKET) u, (struct sockaddr *) &sa, &addrlen);
    if (addrlen != 0) {
        inet_socket_address_from_native_to_java(env, sa, addrlen);
    }
    jobject res = create_java_pair(env, srt_socket_from_native_to_java(env, new_u), inetSocketAddress);

    if (!sa) {
        free((void *) sa);
    }

    return res;
}

JNIEXPORT jint JNICALL
Java_com_github_thibaultbee_srtwrapper_models_Socket_nativeConnect(JNIEnv *env, jobject ju,
                                                                   jobject inetSocketAddress) {
    SRTSOCKET u = srt_socket_from_java_to_native(env, ju);
    const struct sockaddr_in *sa = inet_socket_address_from_java_to_native(env,
                                                                           inetSocketAddress);
    int res = srt_connect((SRTSOCKET) u, (struct sockaddr *) sa, sizeof(*sa));

    if (!sa) {
        free((void *) sa);
    }

    return res;
}

// Options and properties
JNIEXPORT jint JNICALL
Java_com_github_thibaultbee_srtwrapper_models_Socket_nativeSetSockOpt(JNIEnv *env, jobject ju,
                                                                      jint level /*ignored*/,
                                                                      jobject jopt,
                                                                      jobject
                                                                      joptval) {
    SRTSOCKET u = srt_socket_from_java_to_native(env, ju);
    int opt = srt_sockopt_from_java_to_native(env, jopt);
    if (opt <= 0) {
        LOGE("Bad value for SRT option");
        return opt;
    }
    int optlen = 0;
    const void *optval = srt_optval_from_java_to_native(env, joptval, &optlen);

    int res = srt_setsockopt((SRTSOCKET) u,
                             level /*ignored*/, (SRT_SOCKOPT) opt, optval, optlen);

    if (!optval) {
        free((void *) optval);
    }

    return
            res;
}

// Transmission
JNIEXPORT jint JNICALL
Java_com_github_thibaultbee_srtwrapper_models_Socket_nativeSend(JNIEnv *env,
                                                                   jobject ju,
                                                                   jstring jbuf
) {
    SRTSOCKET u = srt_socket_from_java_to_native(env, ju);
    const char *buf = env->GetStringUTFChars(jbuf, nullptr);

    int res = srt_send(u, buf, strlen(buf));

    env->ReleaseStringUTFChars(jbuf, buf);

    return res;
}

JNIEXPORT jint JNICALL
Java_com_github_thibaultbee_srtwrapper_models_Socket_nativeSendMsg(JNIEnv *env,
                                                                   jobject ju,
                                                                   jstring jbuf,
                                                                   jint jttl/* = -1*/,
                                                                   jboolean jinorder/* = false*/
) {
    SRTSOCKET u = srt_socket_from_java_to_native(env, ju);
    const char *buf = env->GetStringUTFChars(jbuf, nullptr);

    int res = srt_sendmsg(u, buf, strlen(buf), jttl, jinorder);

    env->ReleaseStringUTFChars(jbuf, buf);

    return res;
}

JNIEXPORT jint JNICALL
Java_com_github_thibaultbee_srtwrapper_models_Socket_nativeSendMsg2(JNIEnv *env,
                                                                    jobject ju,
                                                                    jstring jbuf,
                                                                    jobject jmsgCtrl) {
    SRTSOCKET u = srt_socket_from_java_to_native(env, ju);
    SRT_MSGCTRL * msgctrl = srt_msgctrl_from_java_to_native(env, jmsgCtrl);
    const char *buf = env->GetStringUTFChars(jbuf, nullptr);

    int res = srt_sendmsg2(u, buf, strlen(buf), msgctrl);

    env->ReleaseStringUTFChars(jbuf, buf);
    if (msgctrl != nullptr) {
        free(msgctrl);
    }

    return res;
}

JNIEXPORT jlong JNICALL
Java_com_github_thibaultbee_srtwrapper_models_Socket_nativeSendFile(JNIEnv *env,
                                                                    jobject ju,
                                                                    jstring jpath,
                                                                    jlong joffset,
                                                                    jlong jsize,
                                                                    jint jblock) {
    SRTSOCKET u = srt_socket_from_java_to_native(env, ju);
    const char *path = env->GetStringUTFChars(jpath, nullptr);
    auto offset = (int64_t)joffset;
    int64_t res = srt_sendfile(u, path, &offset, (int64_t)jsize, jblock);

    env->ReleaseStringUTFChars(jpath, path);

    return (jlong)res;
}

// Errors
JNIEXPORT jstring JNICALL
Java_com_github_thibaultbee_srtwrapper_models_Error_nativeGetLastErrorStr(JNIEnv *env,
                                                                          jobject obj) {
    return env->NewStringUTF(srt_getlasterror_str());
}

JNIEXPORT jint JNICALL
Java_com_github_thibaultbee_srtwrapper_models_Error_nativeGetLastError(JNIEnv *env,
                                                                       jobject obj) {
    return srt_getlasterror(nullptr);
}

}
