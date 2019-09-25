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
Java_com_github_thibaultbee_srtwrapper_models_Socket_nativeBind(JNIEnv *env, jobject obj, jint u,
                                                                jobject inetSocketAddress) {
    const struct sockaddr_in *sa = inet_socket_address_from_java_to_native(env,
                                                                           inetSocketAddress);
    int res = srt_bind((SRTSOCKET) u, (struct sockaddr *) sa, sizeof(*sa));

    if (!sa) {
        free((void *) sa);
    }

    return res;
}

JNIEXPORT jint JNICALL
Java_com_github_thibaultbee_srtwrapper_models_Socket_nativeClose(JNIEnv *env,
                                                                 jobject obj, jint u) {
    return srt_close((SRTSOCKET) u);
}

// Connecting
JNIEXPORT jint JNICALL
Java_com_github_thibaultbee_srtwrapper_models_Socket_nativeListen(JNIEnv *env,
                                                                  jobject obj, jint u,
                                                                  jint backlog) {
    return srt_listen((SRTSOCKET) u, (int) backlog);
}

JNIEXPORT jint JNICALL
Java_com_github_thibaultbee_srtwrapper_models_Socket_nativeConnect(JNIEnv *env, jobject obj, jint u,
                                                                jobject inetSocketAddress) {
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
Java_com_github_thibaultbee_srtwrapper_models_Socket_nativeSetSockOpt(JNIEnv *env, jobject obj,
                                                                      jint u,
                                                                      jint level /*ignored*/,
                                                                      jobject jopt,
                                                                      jobject joptval) {
    int opt = srt_sockopt_from_java_to_native(env, jopt);
    if (opt <= 0) {
        LOGE("Bad value for SRT option");
        return opt;
    }

    int optlen = 0;
    const void *optval = srt_optval_from_java_to_native(env, joptval, &optlen);
    int res = srt_setsockopt((SRTSOCKET) u, level /*ignored*/, (SRT_SOCKOPT) opt, optval, optlen);

    if (!optval) {
        free((void *) optval);
    }

    return res;
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
