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

#define ERRORTYPE_CLASS "com/github/thibaultbee/srtwrapper/enums/ErrorType"
#define SOCKOPT_CLASS "com/github/thibaultbee/srtwrapper/enums/SockOpt"
#define ERROR_CLASS "com/github/thibaultbee/srtwrapper/models/Error"
#define MSGCTRL_CLASS "com/github/thibaultbee/srtwrapper/models/MsgCtrl"
#define SRTSOCKET_CLASS "com/github/thibaultbee/srtwrapper/models/Socket"
#define SRT_CLASS "com/github/thibaultbee/srtwrapper/Srt"

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
    if (strcmp(enum_name, "MSS") == 0) {
        srtopt = SRTO_MSS;
    } else if (strcmp(enum_name, "SNDSYN") == 0) {
        srtopt = SRTO_SNDSYN;
    } else if (strcmp(enum_name, "RCVSYN") == 0) {
        srtopt = SRTO_RCVSYN;
    } else if (strcmp(enum_name, "ISN") == 0) {
        srtopt = SRTO_ISN;
    } else if (strcmp(enum_name, "FC") == 0) {
        srtopt = SRTO_FC;
    } else if (strcmp(enum_name, "SNDBUF") == 0) {
        srtopt = SRTO_SNDBUF;
    } else if (strcmp(enum_name, "RCVBUF") == 0) {
        srtopt = SRTO_RCVBUF;
    } else if (strcmp(enum_name, "LINGER") == 0) {
        srtopt = SRTO_LINGER;
    } else if (strcmp(enum_name, "UDP_SNDBUF") == 0) {
        srtopt = SRTO_UDP_SNDBUF;
    } else if (strcmp(enum_name, "UDP_RCVBUF") == 0) {
        srtopt = SRTO_UDP_RCVBUF;
    } else if (strcmp(enum_name, "RENDEZVOUS") == 0) {
        srtopt = SRTO_RENDEZVOUS;
    } else if (strcmp(enum_name, "SNDTIMEO") == 0) {
        srtopt = SRTO_SNDTIMEO;
    } else if (strcmp(enum_name, "RCVTIMEO") == 0) {
        srtopt = SRTO_RCVTIMEO;
    } else if (strcmp(enum_name, "REUSEADDR") == 0) {
        srtopt = SRTO_REUSEADDR;
    } else if (strcmp(enum_name, "MAXBW") == 0) {
        srtopt = SRTO_MAXBW;
    } else if (strcmp(enum_name, "STATE") == 0) {
        srtopt = SRTO_STATE;
    } else if (strcmp(enum_name, "EVENT") == 0) {
        srtopt = SRTO_EVENT;
    } else if (strcmp(enum_name, "SNDDATA") == 0) {
        srtopt = SRTO_SNDDATA;
    } else if (strcmp(enum_name, "RCVDATA") == 0) {
        srtopt = SRTO_RCVDATA;
    } else if (strcmp(enum_name, "SENDER") == 0) {
        srtopt = SRTO_SENDER;
    } else if (strcmp(enum_name, "TSBPDMODE") == 0) {
        srtopt = SRTO_TSBPDMODE;
    } else if (strcmp(enum_name, "LATENCY") == 0) {
        srtopt = SRTO_LATENCY;
    } else if (strcmp(enum_name, "TSBPDDELAY") == 0) {
        srtopt = SRTO_TSBPDDELAY;
    } else if (strcmp(enum_name, "INPUTBW") == 0) {
        srtopt = SRTO_INPUTBW;
    } else if (strcmp(enum_name, "OHEADBW") == 0) {
        srtopt = SRTO_OHEADBW;
    } else if (strcmp(enum_name, "PASSPHRASE") == 0) {
        srtopt = SRTO_PASSPHRASE;
    } else if (strcmp(enum_name, "PBKEYLEN") == 0) {
        srtopt = SRTO_PBKEYLEN;
    } else if (strcmp(enum_name, "KMSTATE") == 0) {
        srtopt = SRTO_KMSTATE;
    } else if (strcmp(enum_name, "IPTTL") == 0) {
        srtopt = SRTO_IPTTL;
    } else if (strcmp(enum_name, "IPTOS") == 0) {
        srtopt = SRTO_IPTOS;
    } else if (strcmp(enum_name, "TLPKTDROP") == 0) {
        srtopt = SRTO_TLPKTDROP;
    } else if (strcmp(enum_name, "SNDDROPDELAY") == 0) {
        srtopt = SRTO_SNDDROPDELAY;
    } else if (strcmp(enum_name, "NAKREPORT") == 0) {
        srtopt = SRTO_NAKREPORT;
    } else if (strcmp(enum_name, "VERSION") == 0) {
        srtopt = SRTO_VERSION;
    } else if (strcmp(enum_name, "PEERVERSION") == 0) {
        srtopt = SRTO_PEERVERSION;
    } else if (strcmp(enum_name, "CONNTIMEO") == 0) {
        srtopt = SRTO_CONNTIMEO;
    } else if (strcmp(enum_name, "SNDKMSTATE") == 0) {
        srtopt = SRTO_SNDKMSTATE;
    } else if (strcmp(enum_name, "RCVKMSTATE") == 0) {
        srtopt = SRTO_RCVKMSTATE;
    } else if (strcmp(enum_name, "LOSSMAXTTL") == 0) {
        srtopt = SRTO_LOSSMAXTTL;
    } else if (strcmp(enum_name, "RCVLATENCY") == 0) {
        srtopt = SRTO_RCVLATENCY;
    } else if (strcmp(enum_name, "PEERLATENCY") == 0) {
        srtopt = SRTO_PEERLATENCY;
    } else if (strcmp(enum_name, "MINVERSION") == 0) {
        srtopt = SRTO_MINVERSION;
    } else if (strcmp(enum_name, "STREAMID") == 0) {
        srtopt = SRTO_STREAMID;
    } else if (strcmp(enum_name, "CONGESTION") == 0) {
        srtopt = SRTO_CONGESTION;
    } else if (strcmp(enum_name, "MESSAGEAPI") == 0) {
        srtopt = SRTO_MESSAGEAPI;
    } else if (strcmp(enum_name, "PAYLOADSIZE") == 0) {
        srtopt = SRTO_PAYLOADSIZE;
    } else if (strcmp(enum_name, "TRANSTYPE") == 0) {
        srtopt = SRTO_TRANSTYPE;
    } else if (strcmp(enum_name, "KMREFRESHRATE") == 0) {
        srtopt = SRTO_KMREFRESHRATE;
    } else if (strcmp(enum_name, "KMPREANNOUNCE") == 0) {
        srtopt = SRTO_KMPREANNOUNCE;
    } else if (strcmp(enum_name, "STRICTENC") == 0) {
        srtopt = SRTO_STRICTENC;
    } else if (strcmp(enum_name, "IPV6ONLY") == 0) {
        srtopt = SRTO_IPV6ONLY;
    } else if (strcmp(enum_name, "PEERIDLETIMEO") == 0) {
        srtopt = SRTO_PEERIDLETIMEO;
    } else {
        LOGE("Unknown value %s", enum_name);
    }

    env->ReleaseStringUTFChars(jenum_name, enum_name);

    return srtopt;
}

/**
 * @brief Convert Java SRT Transtype to native value
 *
 * @param env Java environment
 * @param jenum_srt_transtype_value Java SRT TransType enumeration member
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

/**
 * @brief Convert Java SRT KMState to native value
 *
 * @param env Java environment
 * @param jenum_srt_kmstate_value Java SRT KMState enumeration member
 * @return return corresponding native SRT option value
 */
SRT_KM_STATE srt_kmstate_from_java_to_native(JNIEnv *env, jobject jenum_srt_kmstate_value) {
    jstring jenum_name = java_enum_get_value_name(env, jenum_srt_kmstate_value);
    if (!jenum_name) {
        LOGE("Can't get Java SRT KMState enum name");
        return SRT_KM_S_UNSECURED;
    }

    auto enum_name = (char *) env->GetStringUTFChars(jenum_name, nullptr);
    if (!enum_name) {
        LOGE("Can't get SRT KMState enum name");
        return SRT_KM_S_UNSECURED;
    }

    SRT_KM_STATE kmstate = SRT_KM_S_UNSECURED;
    if (strcmp(enum_name, "KM_S_UNSECURED") == 0) {
        kmstate = SRT_KM_S_UNSECURED;
    } else if (strcmp(enum_name, "KM_S_SECURING") == 0) {
        kmstate = SRT_KM_S_SECURING;
    } else if (strcmp(enum_name, "KM_S_SECURED") == 0) {
        kmstate = SRT_KM_S_SECURED;
    } else if (strcmp(enum_name, "KM_S_NOSECRET") == 0) {
        kmstate = SRT_KM_S_NOSECRET;
    } else if (strcmp(enum_name, "KM_S_BADSECRET") == 0) {
        kmstate = SRT_KM_S_BADSECRET;
    } else {
        LOGE("Unknown value %s", enum_name);
    }

    env->ReleaseStringUTFChars(jenum_name, enum_name);

    return kmstate;
}

/**
 * @brief Convert Java SRT error to native SRT error
 *
 * @param env Java environment
 * @param jerrorType Java SRT error
 * @return return corresponding Java SRT error
 */
int error_from_java_to_native(JNIEnv *env, jobject jerrorType) {
    jstring jenum_name = java_enum_get_value_name(env, jerrorType);
    if (!jenum_name) {
        LOGE("Can't get Java SRT ErrorType enum name");
        return SRT_EUNKNOWN;
    }

    auto enum_name = (char *) env->GetStringUTFChars(jenum_name, nullptr);
    if (!enum_name) {
        LOGE("Can't get SRT ErrorType enum name");
        return SRTT_INVALID;
    }

    int error_type = SRT_EUNKNOWN;
    if (strcmp(enum_name, "EUNKNOWN") == 0) {
        error_type = SRT_EUNKNOWN;
    } else if (strcmp(enum_name, "SUCCESS") == 0) {
        error_type = SRT_SUCCESS;
    } else if (strcmp(enum_name, "ECONNSETUP") == 0) {
        error_type = SRT_ECONNSETUP;
    } else if (strcmp(enum_name, "ENOSERVER") == 0) {
        error_type = SRT_ENOSERVER;
    } else if (strcmp(enum_name, "ECONNREJ") == 0) {
        error_type = SRT_ECONNREJ;
    } else if (strcmp(enum_name, "ESOCKFAIL") == 0) {
        error_type = SRT_ESOCKFAIL;
    } else if (strcmp(enum_name, "ESECFAIL") == 0) {
        error_type = SRT_ESECFAIL;
    } else if (strcmp(enum_name, "ECONNFAIL") == 0) {
        error_type = SRT_ECONNFAIL;
    } else if (strcmp(enum_name, "ECONNLOST") == 0) {
        error_type = SRT_ECONNLOST;
    } else if (strcmp(enum_name, "ENOCONN") == 0) {
        error_type = SRT_ENOCONN;
    } else if (strcmp(enum_name, "ERESOURCE") == 0) {
        error_type = SRT_ERESOURCE;
    } else if (strcmp(enum_name, "ETHREAD") == 0) {
        error_type = SRT_ETHREAD;
    } else if (strcmp(enum_name, "ENOBUF") == 0) {
        error_type = SRT_ENOBUF;
    } else if (strcmp(enum_name, "EFILE") == 0) {
        error_type = SRT_EFILE;
    } else if (strcmp(enum_name, "EINVRDOFF") == 0) {
        error_type = SRT_EINVRDOFF;
    } else if (strcmp(enum_name, "ERDPERM") == 0) {
        error_type = SRT_ERDPERM;
    } else if (strcmp(enum_name, "EINVWROFF") == 0) {
        error_type = SRT_EINVWROFF;
    } else if (strcmp(enum_name, "EWRPERM") == 0) {
        error_type = SRT_EWRPERM;
    } else if (strcmp(enum_name, "EINVOP") == 0) {
        error_type = SRT_EINVOP;
    } else if (strcmp(enum_name, "EBOUNDSOCK") == 0) {
        error_type = SRT_EBOUNDSOCK;
    } else if (strcmp(enum_name, "ECONNSOCK") == 0) {
        error_type = SRT_ECONNSOCK;
    } else if (strcmp(enum_name, "EINVPARAM") == 0) {
        error_type = SRT_EINVPARAM;
    } else if (strcmp(enum_name, "EINVSOCK") == 0) {
        error_type = SRT_EINVSOCK;
    } else if (strcmp(enum_name, "EUNBOUNDSOCK") == 0) {
        error_type = SRT_EUNBOUNDSOCK;
    } else if (strcmp(enum_name, "ENOLISTEN") == 0) {
        error_type = SRT_ENOLISTEN;
    } else if (strcmp(enum_name, "ERDVNOSERV") == 0) {
        error_type = SRT_ERDVNOSERV;
    } else if (strcmp(enum_name, "ERDVUNBOUND") == 0) {
        error_type = SRT_ERDVUNBOUND;
    } else if (strcmp(enum_name, "EINVALMSGAPI") == 0) {
        error_type = SRT_EINVALMSGAPI;
    } else if (strcmp(enum_name, "EINVALBUFFERAPI") == 0) {
        error_type = SRT_EINVALBUFFERAPI;
    } else if (strcmp(enum_name, "EDUPLISTEN") == 0) {
        error_type = SRT_EDUPLISTEN;
    } else if (strcmp(enum_name, "ELARGEMSG") == 0) {
        error_type = SRT_ELARGEMSG;
    } else if (strcmp(enum_name, "EINVPOLLID") == 0) {
        error_type = SRT_EINVPOLLID;
    } else if (strcmp(enum_name, "EASYNCFAIL") == 0) {
        error_type = SRT_EASYNCFAIL;
    } else if (strcmp(enum_name, "EASYNCSND") == 0) {
        error_type = SRT_EASYNCSND;
    } else if (strcmp(enum_name, "EASYNCRCV") == 0) {
        error_type = SRT_EASYNCRCV;
    } else if (strcmp(enum_name, "ETIMEOUT") == 0) {
        error_type = SRT_ETIMEOUT;
    } else if (strcmp(enum_name, "ECONGEST") == 0) {
        error_type = SRT_ECONGEST;
    } else if (strcmp(enum_name, "EPEERERR") == 0) {
        error_type = SRT_EPEERERR;
    } else {
        LOGE("Unknown value %s", enum_name);
    }

    env->ReleaseStringUTFChars(jenum_name, enum_name);

    return error_type;
}

/**
 * @brief Convert native SRT error to Java SRT error
 *
 * @param env Java environment
 * @param error_type Native SRT error
 * @return return corresponding Java SRT error
 */
jobject error_from_native_to_java(JNIEnv *env, int error_type) {
    jclass errorTypeClass = env->FindClass(ERRORTYPE_CLASS);
    if (!errorTypeClass) {
        return nullptr;
    }

    char *error_field = nullptr;
    switch (error_type) {
        case SRT_EUNKNOWN:
            error_field = strdup("EUNKNOWN");
            break;
        case SRT_SUCCESS:
            error_field = strdup("SUCCESS");
            break;
        case SRT_ECONNSETUP:
            error_field = strdup("ECONNSETUP");
            break;
        case SRT_ENOSERVER:
            error_field = strdup("ENOSERVER");
            break;
        case SRT_ECONNREJ:
            error_field = strdup("ECONNREJ");
            break;
        case SRT_ESOCKFAIL:
            error_field = strdup("ESOCKFAIL");
            break;
        case SRT_ESECFAIL:
            error_field = strdup("ESECFAIL");
            break;
        case SRT_ECONNFAIL:
            error_field = strdup("ECONNFAIL");
            break;
        case SRT_ECONNLOST:
            error_field = strdup("ECONNLOST");
            break;
        case SRT_ENOCONN:
            error_field = strdup("ENOCONN");
            break;
        case SRT_ERESOURCE:
            error_field = strdup("ERESOURCE");
            break;
        case SRT_ETHREAD:
            error_field = strdup("ETHREAD");
            break;
        case SRT_ENOBUF:
            error_field = strdup("ENOBUF");
            break;
        case SRT_EFILE:
            error_field = strdup("EFILE");
            break;
        case SRT_EINVRDOFF:
            error_field = strdup("EINVRDOFF");
            break;
        case SRT_ERDPERM:
            error_field = strdup("ERDPERM");
            break;
        case SRT_EINVWROFF:
            error_field = strdup("EINVWROFF");
            break;
        case SRT_EWRPERM:
            error_field = strdup("EWRPERM");
            break;
        case SRT_EINVOP:
            error_field = strdup("EINVOP");
            break;
        case SRT_EBOUNDSOCK:
            error_field = strdup("EBOUNDSOCK");
            break;
        case SRT_ECONNSOCK:
            error_field = strdup("ECONNSOCK");
            break;
        case SRT_EINVPARAM:
            error_field = strdup("EINVPARAM");
            break;
        case SRT_EINVSOCK:
            error_field = strdup("EINVSOCK");
            break;
        case SRT_EUNBOUNDSOCK:
            error_field = strdup("EUNBOUNDSOCK");
            break;
        case SRT_ENOLISTEN:
            error_field = strdup("ENOLISTEN");
            break;
        case SRT_ERDVNOSERV:
            error_field = strdup("ERDVNOSERV");
            break;
        case SRT_ERDVUNBOUND:
            error_field = strdup("ERDVUNBOUND");
            break;
        case SRT_EINVALMSGAPI:
            error_field = strdup("EINVALMSGAPI");
            break;
        case SRT_EINVALBUFFERAPI:
            error_field = strdup("EINVALBUFFERAPI");
            break;
        case SRT_EDUPLISTEN:
            error_field = strdup("EDUPLISTEN");
            break;
        case SRT_ELARGEMSG:
            error_field = strdup("ELARGEMSG");
            break;
        case SRT_EINVPOLLID:
            error_field = strdup("EINVPOLLID");
            break;
        case SRT_EASYNCFAIL:
            error_field = strdup("EASYNCFAIL");
            break;
        case SRT_EASYNCSND:
            error_field = strdup("EASYNCSND");
            break;
        case SRT_EASYNCRCV:
            error_field = strdup("EASYNCRCV");
            break;
        case SRT_ETIMEOUT:
            error_field = strdup("ETIMEOUT");
            break;
        case SRT_ECONGEST:
            error_field = strdup("ECONGEST");
            break;
        case SRT_EPEERERR:
            error_field = strdup("EPEERERR");
            break;
        default:
            error_field = strdup("EUNKNOWN");
    }

    jfieldID jErrorTypeField = env->GetStaticFieldID(errorTypeClass, error_field,
                                                     "L" ERRORTYPE_CLASS ";");
    if (!jErrorTypeField) {
        LOGE("Can't get ErrorType field");
        env->DeleteLocalRef(errorTypeClass);
        return nullptr;
    }

    jobject jErrorType = env->GetStaticObjectField(errorTypeClass, jErrorTypeField);

    if (error_field != nullptr) {
        free(error_field);
    }

    env->DeleteLocalRef(errorTypeClass);

    return jErrorType;
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
        res = malloc(static_cast<size_t>(*optlen));
        *(SRT_TRANSTYPE *)res = transtype;
    } else if (strcmp(class_name, ENUM_PACKAGE".KMState") == 0) {
        SRT_KM_STATE kmstate = srt_kmstate_from_java_to_native(env, jopval);
        *optlen = sizeof(kmstate);
        res = malloc(static_cast<size_t>(*optlen));
        *(SRT_KM_STATE *)res = kmstate;
        LOGE(">> res %d size %d", kmstate,  *optlen);
    } else if (strcmp(class_name, "java.lang.Long") == 0) {
        jmethodID jlongValue_method = env->GetMethodID(jopval_class, "longValue", "()J");
        if (!jlongValue_method) {
            LOGE("Can't get longValue method");
            return nullptr;
        }
        *optlen = sizeof(int64_t);
        res = malloc(static_cast<size_t>(*optlen));
        *(int64_t *)res = env->CallLongMethod(jopval, jlongValue_method);
    } else if (strcmp(class_name, "java.lang.Integer") == 0) {
        jmethodID jintValue_method = env->GetMethodID(jopval_class, "intValue", "()I");
        if (!jintValue_method) {
            LOGE("Can't get intValue method");
            return nullptr;
        }
        *optlen = sizeof(int);
        res = malloc(static_cast<size_t>(*optlen));
        *(int *)res = env->CallIntMethod(jopval, jintValue_method);
    } else if (strcmp(class_name, "java.lang.Boolean") == 0) {
        jmethodID jbooleanValue_method = env->GetMethodID(jopval_class, "booleanValue", "()Z");
        if (!jbooleanValue_method) {
            LOGE("Can't get booleanValue method");
            return nullptr;
        }
        *optlen = sizeof(bool);
        res = malloc(static_cast<size_t>(*optlen));
        *(bool *)res = (env->CallBooleanMethod(jopval, jbooleanValue_method) == JNI_TRUE);
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
nativeStartUp(JNIEnv *env, jobject obj) {
    srt_setloghandler(nullptr, srt_logger);
    return srt_startup();
}

JNIEXPORT jint JNICALL
nativeCleanUp(JNIEnv *env, jobject obj) {
    return srt_cleanup();
}

// Creating and configuring sockets
JNIEXPORT jint JNICALL
nativeSocket(JNIEnv *env, jobject obj,
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
nativeCreateSocket(JNIEnv *env, jobject obj) {
    return srt_create_socket();
}

JNIEXPORT jint JNICALL
nativeBind(JNIEnv *env, jobject ju, jobject inetSocketAddress) {
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
nativeClose(JNIEnv *env, jobject ju) {
    SRTSOCKET u = srt_socket_from_java_to_native(env, ju);

    return srt_close((SRTSOCKET) u);
}

// Connecting
JNIEXPORT jint JNICALL
nativeListen(JNIEnv *env, jobject ju, jint backlog) {
    SRTSOCKET u = srt_socket_from_java_to_native(env, ju);

    return srt_listen((SRTSOCKET) u, (int) backlog);
}

JNIEXPORT jobject JNICALL
nativeAccept(JNIEnv *env, jobject ju) {
    SRTSOCKET u = srt_socket_from_java_to_native(env, ju);
    auto *sa = (struct sockaddr_in *) malloc(sizeof(struct sockaddr));
    int addrlen = 0;
    jobject inetSocketAddress = nullptr;

    SRTSOCKET new_u = srt_accept((SRTSOCKET) u, (struct sockaddr *) &sa, &addrlen);
    if (addrlen != 0) {
        inet_socket_address_from_native_to_java(env, sa, addrlen);
    }
    jobject res = create_java_pair(env, srt_socket_from_native_to_java(env, new_u),
                                   inetSocketAddress);

    if (!sa) {
        free((void *) sa);
    }

    return res;
}

JNIEXPORT jint JNICALL
nativeConnect(JNIEnv *env, jobject ju, jobject inetSocketAddress) {
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
nativeSetSockOpt(JNIEnv *env,
                 jobject ju,
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
nativeSend(JNIEnv *env, jobject ju, jbyteArray jbuf) {
    SRTSOCKET u = srt_socket_from_java_to_native(env, ju);
    int len = env->GetArrayLength(jbuf);
    char *buf = (char *) env->GetByteArrayElements(jbuf, nullptr);

    int res = srt_send(u, buf, len);

    env->ReleaseByteArrayElements(jbuf, (jbyte *) buf, 0);

    return res;
}

JNIEXPORT jint JNICALL
nativeSendMsg(JNIEnv *env,
              jobject ju,
              jbyteArray jbuf,
              jint jttl/* = -1*/,
              jboolean jinorder/* = false*/) {
    SRTSOCKET u = srt_socket_from_java_to_native(env, ju);
    int len = env->GetArrayLength(jbuf);
    char *buf = (char *) env->GetByteArrayElements(jbuf, nullptr);

    int res = srt_sendmsg(u, buf, len, jttl, jinorder);

    env->ReleaseByteArrayElements(jbuf, (jbyte *) buf, 0);

    return res;
}

JNIEXPORT jint JNICALL
nativeSendMsg2(JNIEnv *env,
               jobject ju,
               jbyteArray jbuf,
               jobject jmsgCtrl) {
    SRTSOCKET u = srt_socket_from_java_to_native(env, ju);
    SRT_MSGCTRL *msgctrl = srt_msgctrl_from_java_to_native(env, jmsgCtrl);
    int len = env->GetArrayLength(jbuf);
    char *buf = (char *) env->GetByteArrayElements(jbuf, nullptr);

    int res = srt_sendmsg2(u, buf, len, msgctrl);

    env->ReleaseByteArrayElements(jbuf, (jbyte *) buf, 0);
    if (msgctrl != nullptr) {
        free(msgctrl);
    }

    return res;
}

JNIEXPORT jbyteArray JNICALL
nativeRecv(JNIEnv *env, jobject ju, jint len) {
    SRTSOCKET u = srt_socket_from_java_to_native(env, ju);
    jbyteArray jbuf = nullptr;
    auto *buf = (char *) malloc(sizeof(char));
    int res = srt_recv(u, buf, len);

    if (res > 0) {
        jbuf = env->NewByteArray(res);
        env->SetByteArrayRegion(jbuf, 0, res, (jbyte *) buf);
    }

    if (buf != nullptr) {
        free(buf);
    }

    return jbuf;

}

JNIEXPORT jbyteArray JNICALL
nativeRecvMsg2(JNIEnv *env,
               jobject ju,
               jint len,
               jobject jmsgCtrl) {
    SRTSOCKET u = srt_socket_from_java_to_native(env, ju);
    SRT_MSGCTRL *msgctrl = srt_msgctrl_from_java_to_native(env, jmsgCtrl);
    jbyteArray jbuf = nullptr;
    auto *buf = (char *) malloc(sizeof(char));
    int res = srt_recvmsg2(u, buf, len, msgctrl);

    if (res > 0) {
        jbuf = env->NewByteArray(res);
        env->SetByteArrayRegion(jbuf, 0, res, (jbyte *) buf);
    }

    if (buf != nullptr) {
        free(buf);
    }
    if (msgctrl != nullptr) {
        free(msgctrl);
    }

    return jbuf;

}

JNIEXPORT jlong JNICALL
nativeSendFile(JNIEnv *env,
               jobject ju,
               jstring jpath,
               jlong joffset,
               jlong jsize,
               jint jblock) {
    SRTSOCKET u = srt_socket_from_java_to_native(env, ju);
    const char *path = env->GetStringUTFChars(jpath, nullptr);
    auto offset = (int64_t) joffset;
    int64_t res = srt_sendfile(u, path, &offset, (int64_t) jsize, jblock);

    env->ReleaseStringUTFChars(jpath, path);

    return (jlong) res;
}

JNIEXPORT jlong JNICALL
nativeRecvFile(JNIEnv *env,
               jobject ju,
               jstring jpath,
               jlong joffset,
               jlong jsize,
               jint jblock) {
    SRTSOCKET u = srt_socket_from_java_to_native(env, ju);
    const char *path = env->GetStringUTFChars(jpath, nullptr);
    auto offset = (int64_t) joffset;
    int64_t res = srt_recvfile(u, path, &offset, (int64_t) jsize, jblock);

    env->ReleaseStringUTFChars(jpath, path);

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

    return error_from_native_to_java(env, err);
}

JNIEXPORT jstring JNICALL
nativeStrError(JNIEnv *env, jobject obj) {
    int error_type = error_from_java_to_native(env, obj);
    return env->NewStringUTF(srt_strerror(error_type, 0));
}

JNIEXPORT void JNICALL
nativeClearLastError(JNIEnv *env, jobject obj) {
    srt_clearlasterror();
}

// Register natives API
static JNINativeMethod srtMethods[] = {
        {"nativeStartUp", "()I", (void *) &nativeStartUp},
        {"nativeCleanUp", "()I", (void *) &nativeCleanUp}
};

static JNINativeMethod socketMethods[] = {
        {"nativeSocket",       "(Ljava/net/StandardProtocolFamily;II)I",    (void *) &nativeSocket},
        {"nativeCreateSocket", "()I",                                       (void *) &nativeCreateSocket},
        {"nativeBind",         "(L" INETSOCKETADDRESS_CLASS ";)I",          (void *) &nativeBind},
        {"nativeClose",        "()I",                                       (void *) &nativeClose},
        {"nativeListen",       "(I)I",                                      (void *) &nativeListen},
        {"nativeAccept",       "()L" PAIR_CLASS ";",                        (void *) &nativeAccept},
        {"nativeConnect",      "(L" INETSOCKETADDRESS_CLASS ";)I",          (void *) &nativeConnect},
        {"nativeSetSockOpt",   "(IL" SOCKOPT_CLASS ";Ljava/lang/Object;)I", (void *) &nativeSetSockOpt},
        {"nativeSend",         "([B)I",                                     (void *) &nativeSend},
        {"nativeSendMsg",      "([BIZ)I",                                   (void *) &nativeSendMsg},
        {"nativeSendMsg2",     "([BL" MSGCTRL_CLASS ";)I",                  (void *) &nativeSendMsg2},
        {"nativeRecv",         "(I)[B",                                     (void *) &nativeRecv},
        {"nativeRecvMsg2",     "(IL" MSGCTRL_CLASS ";)[B",                  (void *) &nativeRecvMsg2},
        {"nativeSendFile",     "(Ljava/lang/String;JJI)J",                  (void *) &nativeSendFile},
        {"nativeRecvFile",     "(Ljava/lang/String;JJI)J",                  (void *) &nativeRecvFile}
};

static JNINativeMethod errorMethods[] = {
        {"nativeGetLastErrorStr", "()Ljava/lang/String;", (void *) &nativeGetLastErrorStr},
        {"nativeGetLastError", "()L" ERRORTYPE_CLASS ";", (void *) &nativeGetLastError},
        {"nativeClearLastError", "()V", (void *) &nativeClearLastError}
};

static JNINativeMethod errorTypeMethods[] = {
        {"nativeStrError", "()Ljava/lang/String;", (void *) &nativeStrError}
};

static int registerNativeForClassName(JNIEnv *env, const char *className,
                                      JNINativeMethod *methods, int methodsSize) {
    jclass clazz = env->FindClass(className);
    if (clazz == nullptr) {
        LOGE("Unable to find class '%s'", className);
        return JNI_FALSE;
    }

    if (env->RegisterNatives(clazz, methods, methodsSize) < 0) {
        LOGE("RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

jint JNI_OnLoad(JavaVM *vm, void * /*reserved*/) {
    JNIEnv *env = nullptr;
    jint result;

    if ((result = vm->GetEnv((void **) &env, JNI_VERSION_1_4)) != JNI_OK) {
        LOGE("GetEnv failed");
        return result;
    }

    if ((registerNativeForClassName(env, SRT_CLASS, srtMethods,
                                    sizeof(srtMethods) / sizeof(srtMethods[0])) != JNI_TRUE)) {
        LOGE("SRT RegisterNatives failed");
        return -1;
    }

    if ((registerNativeForClassName(env, SRTSOCKET_CLASS, socketMethods,
                                    sizeof(socketMethods) / sizeof(socketMethods[0])) !=
         JNI_TRUE)) {
        LOGE("Socket RegisterNatives failed");
        return -1;
    }

    if ((registerNativeForClassName(env, ERROR_CLASS, errorMethods,
                                    sizeof(errorMethods) / sizeof(errorMethods[0])) != JNI_TRUE)) {
        LOGE("Error RegisterNatives failed");
        return -1;
    }

    if ((registerNativeForClassName(env, ERRORTYPE_CLASS, errorTypeMethods,
                                    sizeof(errorTypeMethods) / sizeof(errorTypeMethods[0])) != JNI_TRUE)) {
        LOGE("ErrorType RegisterNatives failed");
        return -1;
    }

    return JNI_VERSION_1_6;
}

}
