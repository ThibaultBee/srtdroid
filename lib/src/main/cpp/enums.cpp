#include <jni.h>
#include <string>
#include <cerrno>
#include <cstring>
#include <sys/socket.h>
#include <cstdlib>

#include "srt/srt.h"

#include "log.h"
#include "enums.h"

#ifdef __cplusplus
extern "C" {
#endif

#define TAG "SRTJniEnums"

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

int address_family_from_java_to_native(JNIEnv *env, jobject jenum_af_value) {
    jstring jenum_name = java_enum_get_value_name(env, jenum_af_value);
    if (!jenum_name) {
        LOGE(TAG, "Can't get Java address family enum name");
        return -EFAULT;
    }

    auto enum_name = (char *) env->GetStringUTFChars(jenum_name, nullptr);
    if (!enum_name) {
        LOGE(TAG, "Can't get address family enum name");
        return -EFAULT;
    }

    int af = AF_UNSPEC;
    if (strcmp(enum_name, "INET") == 0) {
        af = AF_INET;
    } else if (strcmp(enum_name, "INET6") == 0) {
        af = AF_INET6;
    } else {
        LOGE(TAG, "Unknown value %s", enum_name);
    }

    env->ReleaseStringUTFChars(jenum_name, enum_name);

    return af;
}

int srt_sockopt_from_java_to_native(JNIEnv *env, jobject jenum_srt_option_value) {
    jstring jenum_name = java_enum_get_value_name(env, jenum_srt_option_value);
    if (!jenum_name) {
        LOGE(TAG, "Can't get Java SRT option enum name");
        return -EFAULT;
    }

    auto enum_name = (char *) env->GetStringUTFChars(jenum_name, nullptr);
    if (!enum_name) {
        LOGE(TAG, "Can't get SRT option enum name");
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
        LOGE(TAG, "Unknown value %s", enum_name);
    }

    env->ReleaseStringUTFChars(jenum_name, enum_name);

    return srtopt;
}

SRT_TRANSTYPE srt_transtype_from_java_to_native(JNIEnv *env, jobject jenum_srt_transtype_value) {
    jstring jenum_name = java_enum_get_value_name(env, jenum_srt_transtype_value);
    if (!jenum_name) {
        LOGE(TAG, "Can't get Java SRT transtype enum name");
        return SRTT_INVALID;
    }

    auto enum_name = (char *) env->GetStringUTFChars(jenum_name, nullptr);
    if (!enum_name) {
        LOGE(TAG, "Can't get SRT transtype enum name");
        return SRTT_INVALID;
    }

    SRT_TRANSTYPE transtype = SRTT_INVALID;
    if (strcmp(enum_name, "LIVE") == 0) {
        transtype = SRTT_LIVE;
    } else if (strcmp(enum_name, "FILE") == 0) {
        transtype = SRTT_FILE;
    } else {
        LOGE(TAG, "Unknown value %s", enum_name);
    }

    env->ReleaseStringUTFChars(jenum_name, enum_name);

    return transtype;
}

SRT_KM_STATE srt_kmstate_from_java_to_native(JNIEnv *env, jobject jenum_srt_kmstate_value) {
    jstring jenum_name = java_enum_get_value_name(env, jenum_srt_kmstate_value);
    if (!jenum_name) {
        LOGE(TAG, "Can't get Java SRT KMState enum name");
        return SRT_KM_S_UNSECURED;
    }

    auto enum_name = (char *) env->GetStringUTFChars(jenum_name, nullptr);
    if (!enum_name) {
        LOGE(TAG, "Can't get SRT KMState enum name");
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
        LOGE(TAG, "Unknown value %s", enum_name);
    }

    env->ReleaseStringUTFChars(jenum_name, enum_name);

    return kmstate;
}

jobject srt_sock_status_from_native_to_java(JNIEnv *env, SRT_SOCKSTATUS sock_status) {
        jclass sockStatusClass = env->FindClass(SOCKSTATUS_CLASS);
        if (!sockStatusClass) {
            LOGE(TAG, "Can't get " SOCKSTATUS_CLASS " class");
            return nullptr;
        }

        char *sockStatus_field = nullptr;
        switch (sock_status) {
            case SRTS_INIT:
                sockStatus_field = strdup("INIT");
                break;
            case SRTS_OPENED:
                sockStatus_field = strdup("OPENED");
                break;
            case SRTS_LISTENING:
                sockStatus_field = strdup("LISTENING");
                break;
            case SRTS_CONNECTING:
                sockStatus_field = strdup("CONNECTING");
                break;
            case SRTS_CONNECTED:
                sockStatus_field = strdup("CONNECTED");
                break;
            case SRTS_BROKEN:
                sockStatus_field = strdup("BROKEN");
                break;
            case SRTS_CLOSING:
                sockStatus_field = strdup("CLOSING");
                break;
            case SRTS_CLOSED:
                sockStatus_field = strdup("CLOSED");
                break;
            case SRTS_NONEXIST:
                sockStatus_field = strdup("NONEXIST");
                break;
            default:
                LOGE(TAG, "Unknown value %d", sock_status);
        }

        jfieldID jSockStatusField = env->GetStaticFieldID(sockStatusClass, sockStatus_field,
                                                         "L" SOCKSTATUS_CLASS ";");
        if (!jSockStatusField) {
            LOGE(TAG, "Can't get SockStatus field");
            env->DeleteLocalRef(sockStatusClass);
            return nullptr;
        }

        jobject sockStatus = env->GetStaticObjectField(sockStatusClass, jSockStatusField);

        if (sockStatus_field != nullptr) {
            free(sockStatus_field);
        }

        env->DeleteLocalRef(sockStatusClass);

        return sockStatus;
    }

int error_from_java_to_native(JNIEnv *env, jobject jerrorType) {
    jstring jenum_name = java_enum_get_value_name(env, jerrorType);
    if (!jenum_name) {
        LOGE(TAG, "Can't get Java SRT ErrorType enum name");
        return SRT_EUNKNOWN;
    }

    auto enum_name = (char *) env->GetStringUTFChars(jenum_name, nullptr);
    if (!enum_name) {
        LOGE(TAG, "Can't get SRT ErrorType enum name");
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
        LOGE(TAG, "Unknown value %s", enum_name);
    }

    env->ReleaseStringUTFChars(jenum_name, enum_name);

    return error_type;
}

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
        LOGE(TAG, "Can't get ErrorType field");
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

#ifdef __cplusplus
}
#endif
