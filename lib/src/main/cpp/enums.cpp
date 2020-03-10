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
 * @param enumValue Java enumeration member
 * @return return native string that contains Java enum name (to be free after usage)
 */
const char *enums_get_field_id(JNIEnv *env, jobject enumValue) {
    jclass enumClazz = env->GetObjectClass(enumValue);
    if (!enumClazz) {
        LOGE(TAG, "Can't get enum class");
        return nullptr;
    }

    jmethodID enumNameMethod = env->GetMethodID(enumClazz, "name", "()Ljava/lang/String;");
    if (!enumNameMethod) {
        LOGE(TAG, "Can't get enum name method");
        env->DeleteLocalRef(enumClazz);
        return nullptr;
    }

    env->DeleteLocalRef(enumClazz);

    auto enumField = (jstring) env->CallObjectMethod(enumValue, enumNameMethod);
    if (!enumField) {
        LOGE(TAG, "Can't get Java enum field");
        env->DeleteLocalRef(enumClazz);
        return nullptr;
    }

    const char *enum_field = env->GetStringUTFChars(enumField, nullptr);
    if (!enum_field) {
        LOGE(TAG, "Can't get native enum field");
        env->DeleteLocalRef(enumClazz);
        return nullptr;
    }

    const char *dup_enum_field = strdup(enum_field);

    env->ReleaseStringUTFChars(enumField, enum_field);

    return dup_enum_field;
}

int address_family_j2n(JNIEnv *env, jobject addressFamily) {
    const char *address_family_field = enums_get_field_id(env, addressFamily);
    if (!address_family_field) {
        LOGE(TAG, "Can't get address family field");
        return -EFAULT;
    }

    int af = AF_UNSPEC;
    if (strcmp(address_family_field, "INET") == 0) {
        af = AF_INET;
    } else if (strcmp(address_family_field, "INET6") == 0) {
        af = AF_INET6;
    } else {
        LOGE(TAG, "AddressFamily: unknown value %s", address_family_field);
    }

    free((void *) address_family_field);

    return af;
}

int srt_sockopt_j2n(JNIEnv *env, jobject sockOpt) {
    const char *srt_sockopt_field = enums_get_field_id(env, sockOpt);
    if (!srt_sockopt_field) {
        LOGE(TAG, "Can't get sockopt field");
        return -EFAULT;
    }

    int srt_sockopt = -1;
    if (strcmp(srt_sockopt_field, "MSS") == 0) {
        srt_sockopt = SRTO_MSS;
    } else if (strcmp(srt_sockopt_field, "SNDSYN") == 0) {
        srt_sockopt = SRTO_SNDSYN;
    } else if (strcmp(srt_sockopt_field, "RCVSYN") == 0) {
        srt_sockopt = SRTO_RCVSYN;
    } else if (strcmp(srt_sockopt_field, "ISN") == 0) {
        srt_sockopt = SRTO_ISN;
    } else if (strcmp(srt_sockopt_field, "FC") == 0) {
        srt_sockopt = SRTO_FC;
    } else if (strcmp(srt_sockopt_field, "SNDBUF") == 0) {
        srt_sockopt = SRTO_SNDBUF;
    } else if (strcmp(srt_sockopt_field, "RCVBUF") == 0) {
        srt_sockopt = SRTO_RCVBUF;
    } else if (strcmp(srt_sockopt_field, "LINGER") == 0) {
        srt_sockopt = SRTO_LINGER;
    } else if (strcmp(srt_sockopt_field, "UDP_SNDBUF") == 0) {
        srt_sockopt = SRTO_UDP_SNDBUF;
    } else if (strcmp(srt_sockopt_field, "UDP_RCVBUF") == 0) {
        srt_sockopt = SRTO_UDP_RCVBUF;
    } else if (strcmp(srt_sockopt_field, "RENDEZVOUS") == 0) {
        srt_sockopt = SRTO_RENDEZVOUS;
    } else if (strcmp(srt_sockopt_field, "SNDTIMEO") == 0) {
        srt_sockopt = SRTO_SNDTIMEO;
    } else if (strcmp(srt_sockopt_field, "RCVTIMEO") == 0) {
        srt_sockopt = SRTO_RCVTIMEO;
    } else if (strcmp(srt_sockopt_field, "REUSEADDR") == 0) {
        srt_sockopt = SRTO_REUSEADDR;
    } else if (strcmp(srt_sockopt_field, "MAXBW") == 0) {
        srt_sockopt = SRTO_MAXBW;
    } else if (strcmp(srt_sockopt_field, "STATE") == 0) {
        srt_sockopt = SRTO_STATE;
    } else if (strcmp(srt_sockopt_field, "EVENT") == 0) {
        srt_sockopt = SRTO_EVENT;
    } else if (strcmp(srt_sockopt_field, "SNDDATA") == 0) {
        srt_sockopt = SRTO_SNDDATA;
    } else if (strcmp(srt_sockopt_field, "RCVDATA") == 0) {
        srt_sockopt = SRTO_RCVDATA;
    } else if (strcmp(srt_sockopt_field, "SENDER") == 0) {
        srt_sockopt = SRTO_SENDER;
    } else if (strcmp(srt_sockopt_field, "TSBPDMODE") == 0) {
        srt_sockopt = SRTO_TSBPDMODE;
    } else if (strcmp(srt_sockopt_field, "LATENCY") == 0) {
        srt_sockopt = SRTO_LATENCY;
    } else if (strcmp(srt_sockopt_field, "TSBPDDELAY") == 0) {
        srt_sockopt = SRTO_TSBPDDELAY;
    } else if (strcmp(srt_sockopt_field, "INPUTBW") == 0) {
        srt_sockopt = SRTO_INPUTBW;
    } else if (strcmp(srt_sockopt_field, "OHEADBW") == 0) {
        srt_sockopt = SRTO_OHEADBW;
    } else if (strcmp(srt_sockopt_field, "PASSPHRASE") == 0) {
        srt_sockopt = SRTO_PASSPHRASE;
    } else if (strcmp(srt_sockopt_field, "PBKEYLEN") == 0) {
        srt_sockopt = SRTO_PBKEYLEN;
    } else if (strcmp(srt_sockopt_field, "KMSTATE") == 0) {
        srt_sockopt = SRTO_KMSTATE;
    } else if (strcmp(srt_sockopt_field, "IPTTL") == 0) {
        srt_sockopt = SRTO_IPTTL;
    } else if (strcmp(srt_sockopt_field, "IPTOS") == 0) {
        srt_sockopt = SRTO_IPTOS;
    } else if (strcmp(srt_sockopt_field, "TLPKTDROP") == 0) {
        srt_sockopt = SRTO_TLPKTDROP;
    } else if (strcmp(srt_sockopt_field, "SNDDROPDELAY") == 0) {
        srt_sockopt = SRTO_SNDDROPDELAY;
    } else if (strcmp(srt_sockopt_field, "NAKREPORT") == 0) {
        srt_sockopt = SRTO_NAKREPORT;
    } else if (strcmp(srt_sockopt_field, "VERSION") == 0) {
        srt_sockopt = SRTO_VERSION;
    } else if (strcmp(srt_sockopt_field, "PEERVERSION") == 0) {
        srt_sockopt = SRTO_PEERVERSION;
    } else if (strcmp(srt_sockopt_field, "CONNTIMEO") == 0) {
        srt_sockopt = SRTO_CONNTIMEO;
    } else if (strcmp(srt_sockopt_field, "SNDKMSTATE") == 0) {
        srt_sockopt = SRTO_SNDKMSTATE;
    } else if (strcmp(srt_sockopt_field, "RCVKMSTATE") == 0) {
        srt_sockopt = SRTO_RCVKMSTATE;
    } else if (strcmp(srt_sockopt_field, "LOSSMAXTTL") == 0) {
        srt_sockopt = SRTO_LOSSMAXTTL;
    } else if (strcmp(srt_sockopt_field, "RCVLATENCY") == 0) {
        srt_sockopt = SRTO_RCVLATENCY;
    } else if (strcmp(srt_sockopt_field, "PEERLATENCY") == 0) {
        srt_sockopt = SRTO_PEERLATENCY;
    } else if (strcmp(srt_sockopt_field, "MINVERSION") == 0) {
        srt_sockopt = SRTO_MINVERSION;
    } else if (strcmp(srt_sockopt_field, "STREAMID") == 0) {
        srt_sockopt = SRTO_STREAMID;
    } else if (strcmp(srt_sockopt_field, "CONGESTION") == 0) {
        srt_sockopt = SRTO_CONGESTION;
    } else if (strcmp(srt_sockopt_field, "MESSAGEAPI") == 0) {
        srt_sockopt = SRTO_MESSAGEAPI;
    } else if (strcmp(srt_sockopt_field, "PAYLOADSIZE") == 0) {
        srt_sockopt = SRTO_PAYLOADSIZE;
    } else if (strcmp(srt_sockopt_field, "TRANSTYPE") == 0) {
        srt_sockopt = SRTO_TRANSTYPE;
    } else if (strcmp(srt_sockopt_field, "KMREFRESHRATE") == 0) {
        srt_sockopt = SRTO_KMREFRESHRATE;
    } else if (strcmp(srt_sockopt_field, "KMPREANNOUNCE") == 0) {
        srt_sockopt = SRTO_KMPREANNOUNCE;
    } else if (strcmp(srt_sockopt_field, "STRICTENC") == 0) {
        srt_sockopt = SRTO_STRICTENC;
    } else if (strcmp(srt_sockopt_field, "IPV6ONLY") == 0) {
        srt_sockopt = SRTO_IPV6ONLY;
    } else if (strcmp(srt_sockopt_field, "PEERIDLETIMEO") == 0) {
        srt_sockopt = SRTO_PEERIDLETIMEO;
    } else {
        LOGE(TAG, "SockOpt: unknown value %s", srt_sockopt_field);
    }

    free((void *) srt_sockopt_field);

    return srt_sockopt;
}

jobject srt_transtype_n2j(JNIEnv *env, SRT_TRANSTYPE transtype) {
    jclass transTypeClazz = env->FindClass(TRANSTYPE_CLASS);
    if (!transTypeClazz) {
        LOGE(TAG, "Can't get "
                TRANSTYPE_CLASS
                " class");
        return nullptr;
    }

    char *transtype_field = nullptr;
    switch (transtype) {
        case SRTT_LIVE:
            transtype_field = strdup("LIVE");
            break;
        case SRTT_FILE:
            transtype_field = strdup("FILE");
            break;
        case SRTT_INVALID:
            transtype_field = strdup("INVALID");
            break;
        default:
            LOGE(TAG, "SRT_TRANSTYPE: unknown value %d", transtype);
    }

    jfieldID transTypeField = env->GetStaticFieldID(transTypeClazz, transtype_field,
                                                    "L" TRANSTYPE_CLASS ";");
    if (!transTypeField) {
        LOGE(TAG, "Can't get Transtype field");
        env->DeleteLocalRef(transTypeClazz);
        return nullptr;
    }

    jobject transType = env->GetStaticObjectField(transTypeClazz, transTypeField);

    if (transtype_field != nullptr) {
        free(transtype_field);
    }

    env->DeleteLocalRef(transTypeClazz);

    return transType;
}

SRT_TRANSTYPE srt_transtype_j2n(JNIEnv *env, jobject transType) {
    const char *transtype_field = enums_get_field_id(env, transType);
    if (!transtype_field) {
        LOGE(TAG, "Can't get SRT_TRANSTYPE");
        return SRTT_INVALID;
    }

    SRT_TRANSTYPE transtype = SRTT_INVALID;
    if (strcmp(transtype_field, "LIVE") == 0) {
        transtype = SRTT_LIVE;
    } else if (strcmp(transtype_field, "FILE") == 0) {
        transtype = SRTT_FILE;
    } else {
        LOGE(TAG, "TransType: unknown value %s", transtype_field);
    }

    free((void *) transtype_field);

    return transtype;
}

jobject srt_kmstate_n2j(JNIEnv *env, SRT_KM_STATE kmstate) {
    jclass kmStateClazz = env->FindClass(KMSTATE_CLASS);
    if (!kmStateClazz) {
        LOGE(TAG, "Can't get "
                KMSTATE_CLASS
                " class");
        return nullptr;
    }

    char *kmstate_field = nullptr;
    switch (kmstate) {
        case SRT_KM_S_UNSECURED:
            kmstate_field = strdup("KM_S_UNSECURED");
            break;
        case SRT_KM_S_SECURING:
            kmstate_field = strdup("KM_S_SECURING");
            break;
        case SRT_KM_S_SECURED:
            kmstate_field = strdup("KM_S_SECURED");
            break;
        case SRT_KM_S_NOSECRET:
            kmstate_field = strdup("KM_S_NOSECRET");
            break;
        case SRT_KM_S_BADSECRET:
            kmstate_field = strdup("KM_S_BADSECRET");
            break;
        default:
            LOGE(TAG, "SRT_KM_STATE: unknown value %d", kmstate);
    }

    jfieldID kmStateField = env->GetStaticFieldID(kmStateClazz, kmstate_field,
                                                  "L" KMSTATE_CLASS ";");
    if (!kmStateField) {
        LOGE(TAG, "Can't get KMState field");
        env->DeleteLocalRef(kmStateClazz);
        return nullptr;
    }

    jobject kmState = env->GetStaticObjectField(kmStateClazz, kmStateField);

    if (kmstate_field != nullptr) {
        free(kmstate_field);
    }

    env->DeleteLocalRef(kmStateClazz);

    return kmState;
}

SRT_KM_STATE srt_kmstate_j2n(JNIEnv *env, jobject kmState) {
    const char *kmstate_field = enums_get_field_id(env, kmState);
    if (!kmstate_field) {
        LOGE(TAG, "Can't get SRT_KM_STATE");
        return SRT_KM_S_UNSECURED;
    }

    SRT_KM_STATE kmstate = SRT_KM_S_UNSECURED;
    if (strcmp(kmstate_field, "KM_S_UNSECURED") == 0) {
        kmstate = SRT_KM_S_UNSECURED;
    } else if (strcmp(kmstate_field, "KM_S_SECURING") == 0) {
        kmstate = SRT_KM_S_SECURING;
    } else if (strcmp(kmstate_field, "KM_S_SECURED") == 0) {
        kmstate = SRT_KM_S_SECURED;
    } else if (strcmp(kmstate_field, "KM_S_NOSECRET") == 0) {
        kmstate = SRT_KM_S_NOSECRET;
    } else if (strcmp(kmstate_field, "KM_S_BADSECRET") == 0) {
        kmstate = SRT_KM_S_BADSECRET;
    } else {
        LOGE(TAG, "KMState: unknown value %s", kmstate_field);
    }

    free((void *) kmstate_field);

    return kmstate;
}

jobject srt_sockstatus_n2j(JNIEnv *env, SRT_SOCKSTATUS sockstatus) {
    jclass sockStatusClazz = env->FindClass(SOCKSTATUS_CLASS);
    if (!sockStatusClazz) {
        LOGE(TAG, "Can't get "
                SOCKSTATUS_CLASS
                " class");
        return nullptr;
    }

    char *sockstatus_field = nullptr;
    switch (sockstatus) {
        case SRTS_INIT:
            sockstatus_field = strdup("INIT");
            break;
        case SRTS_OPENED:
            sockstatus_field = strdup("OPENED");
            break;
        case SRTS_LISTENING:
            sockstatus_field = strdup("LISTENING");
            break;
        case SRTS_CONNECTING:
            sockstatus_field = strdup("CONNECTING");
            break;
        case SRTS_CONNECTED:
            sockstatus_field = strdup("CONNECTED");
            break;
        case SRTS_BROKEN:
            sockstatus_field = strdup("BROKEN");
            break;
        case SRTS_CLOSING:
            sockstatus_field = strdup("CLOSING");
            break;
        case SRTS_CLOSED:
            sockstatus_field = strdup("CLOSED");
            break;
        case SRTS_NONEXIST:
            sockstatus_field = strdup("NONEXIST");
            break;
        default:
            LOGE(TAG, "SRT_SOCKSTATUS: unknown value %d", sockstatus);
    }

    jfieldID sockStatusField = env->GetStaticFieldID(sockStatusClazz, sockstatus_field,
                                                     "L" SOCKSTATUS_CLASS ";");
    if (!sockStatusField) {
        LOGE(TAG, "Can't get SockStatus field");
        env->DeleteLocalRef(sockStatusClazz);
        return nullptr;
    }

    jobject sockStatus = env->GetStaticObjectField(sockStatusClazz, sockStatusField);

    if (sockstatus_field != nullptr) {
        free(sockstatus_field);
    }

    env->DeleteLocalRef(sockStatusClazz);

    return sockStatus;
}

int srt_error_j2n(JNIEnv *env, jobject errorType) {
    const char *error_type_field = enums_get_field_id(env, errorType);
    if (!error_type_field) {
        LOGE(TAG, "Can't get SRT Error field");
        return SRTT_INVALID;
    }

    int error_type = SRT_EUNKNOWN;
    if (strcmp(error_type_field, "EUNKNOWN") == 0) {
        error_type = SRT_EUNKNOWN;
    } else if (strcmp(error_type_field, "SUCCESS") == 0) {
        error_type = SRT_SUCCESS;
    } else if (strcmp(error_type_field, "ECONNSETUP") == 0) {
        error_type = SRT_ECONNSETUP;
    } else if (strcmp(error_type_field, "ENOSERVER") == 0) {
        error_type = SRT_ENOSERVER;
    } else if (strcmp(error_type_field, "ECONNREJ") == 0) {
        error_type = SRT_ECONNREJ;
    } else if (strcmp(error_type_field, "ESOCKFAIL") == 0) {
        error_type = SRT_ESOCKFAIL;
    } else if (strcmp(error_type_field, "ESECFAIL") == 0) {
        error_type = SRT_ESECFAIL;
    } else if (strcmp(error_type_field, "ECONNFAIL") == 0) {
        error_type = SRT_ECONNFAIL;
    } else if (strcmp(error_type_field, "ECONNLOST") == 0) {
        error_type = SRT_ECONNLOST;
    } else if (strcmp(error_type_field, "ENOCONN") == 0) {
        error_type = SRT_ENOCONN;
    } else if (strcmp(error_type_field, "ERESOURCE") == 0) {
        error_type = SRT_ERESOURCE;
    } else if (strcmp(error_type_field, "ETHREAD") == 0) {
        error_type = SRT_ETHREAD;
    } else if (strcmp(error_type_field, "ENOBUF") == 0) {
        error_type = SRT_ENOBUF;
    } else if (strcmp(error_type_field, "EFILE") == 0) {
        error_type = SRT_EFILE;
    } else if (strcmp(error_type_field, "EINVRDOFF") == 0) {
        error_type = SRT_EINVRDOFF;
    } else if (strcmp(error_type_field, "ERDPERM") == 0) {
        error_type = SRT_ERDPERM;
    } else if (strcmp(error_type_field, "EINVWROFF") == 0) {
        error_type = SRT_EINVWROFF;
    } else if (strcmp(error_type_field, "EWRPERM") == 0) {
        error_type = SRT_EWRPERM;
    } else if (strcmp(error_type_field, "EINVOP") == 0) {
        error_type = SRT_EINVOP;
    } else if (strcmp(error_type_field, "EBOUNDSOCK") == 0) {
        error_type = SRT_EBOUNDSOCK;
    } else if (strcmp(error_type_field, "ECONNSOCK") == 0) {
        error_type = SRT_ECONNSOCK;
    } else if (strcmp(error_type_field, "EINVPARAM") == 0) {
        error_type = SRT_EINVPARAM;
    } else if (strcmp(error_type_field, "EINVSOCK") == 0) {
        error_type = SRT_EINVSOCK;
    } else if (strcmp(error_type_field, "EUNBOUNDSOCK") == 0) {
        error_type = SRT_EUNBOUNDSOCK;
    } else if (strcmp(error_type_field, "ENOLISTEN") == 0) {
        error_type = SRT_ENOLISTEN;
    } else if (strcmp(error_type_field, "ERDVNOSERV") == 0) {
        error_type = SRT_ERDVNOSERV;
    } else if (strcmp(error_type_field, "ERDVUNBOUND") == 0) {
        error_type = SRT_ERDVUNBOUND;
    } else if (strcmp(error_type_field, "EINVALMSGAPI") == 0) {
        error_type = SRT_EINVALMSGAPI;
    } else if (strcmp(error_type_field, "EINVALBUFFERAPI") == 0) {
        error_type = SRT_EINVALBUFFERAPI;
    } else if (strcmp(error_type_field, "EDUPLISTEN") == 0) {
        error_type = SRT_EDUPLISTEN;
    } else if (strcmp(error_type_field, "ELARGEMSG") == 0) {
        error_type = SRT_ELARGEMSG;
    } else if (strcmp(error_type_field, "EINVPOLLID") == 0) {
        error_type = SRT_EINVPOLLID;
    } else if (strcmp(error_type_field, "EASYNCFAIL") == 0) {
        error_type = SRT_EASYNCFAIL;
    } else if (strcmp(error_type_field, "EASYNCSND") == 0) {
        error_type = SRT_EASYNCSND;
    } else if (strcmp(error_type_field, "EASYNCRCV") == 0) {
        error_type = SRT_EASYNCRCV;
    } else if (strcmp(error_type_field, "ETIMEOUT") == 0) {
        error_type = SRT_ETIMEOUT;
    } else if (strcmp(error_type_field, "ECONGEST") == 0) {
        error_type = SRT_ECONGEST;
    } else if (strcmp(error_type_field, "EPEERERR") == 0) {
        error_type = SRT_EPEERERR;
    } else {
        LOGE(TAG, "ErrorType: unknown value %s", error_type_field);
    }

    free((void *) error_type_field);

    return error_type;
}

jobject srt_error_n2j(JNIEnv *env, int error_type) {
    jclass errorTypeClazz = env->FindClass(ERRORTYPE_CLASS);
    if (!errorTypeClazz) {
        LOGE(TAG, "Can't get "
                ERRORTYPE_CLASS
                " class");
        return nullptr;
    }

    char *error_type_field = nullptr;
    switch (error_type) {
        case SRT_EUNKNOWN:
            error_type_field = strdup("EUNKNOWN");
            break;
        case SRT_SUCCESS:
            error_type_field = strdup("SUCCESS");
            break;
        case SRT_ECONNSETUP:
            error_type_field = strdup("ECONNSETUP");
            break;
        case SRT_ENOSERVER:
            error_type_field = strdup("ENOSERVER");
            break;
        case SRT_ECONNREJ:
            error_type_field = strdup("ECONNREJ");
            break;
        case SRT_ESOCKFAIL:
            error_type_field = strdup("ESOCKFAIL");
            break;
        case SRT_ESECFAIL:
            error_type_field = strdup("ESECFAIL");
            break;
        case SRT_ECONNFAIL:
            error_type_field = strdup("ECONNFAIL");
            break;
        case SRT_ECONNLOST:
            error_type_field = strdup("ECONNLOST");
            break;
        case SRT_ENOCONN:
            error_type_field = strdup("ENOCONN");
            break;
        case SRT_ERESOURCE:
            error_type_field = strdup("ERESOURCE");
            break;
        case SRT_ETHREAD:
            error_type_field = strdup("ETHREAD");
            break;
        case SRT_ENOBUF:
            error_type_field = strdup("ENOBUF");
            break;
        case SRT_EFILE:
            error_type_field = strdup("EFILE");
            break;
        case SRT_EINVRDOFF:
            error_type_field = strdup("EINVRDOFF");
            break;
        case SRT_ERDPERM:
            error_type_field = strdup("ERDPERM");
            break;
        case SRT_EINVWROFF:
            error_type_field = strdup("EINVWROFF");
            break;
        case SRT_EWRPERM:
            error_type_field = strdup("EWRPERM");
            break;
        case SRT_EINVOP:
            error_type_field = strdup("EINVOP");
            break;
        case SRT_EBOUNDSOCK:
            error_type_field = strdup("EBOUNDSOCK");
            break;
        case SRT_ECONNSOCK:
            error_type_field = strdup("ECONNSOCK");
            break;
        case SRT_EINVPARAM:
            error_type_field = strdup("EINVPARAM");
            break;
        case SRT_EINVSOCK:
            error_type_field = strdup("EINVSOCK");
            break;
        case SRT_EUNBOUNDSOCK:
            error_type_field = strdup("EUNBOUNDSOCK");
            break;
        case SRT_ENOLISTEN:
            error_type_field = strdup("ENOLISTEN");
            break;
        case SRT_ERDVNOSERV:
            error_type_field = strdup("ERDVNOSERV");
            break;
        case SRT_ERDVUNBOUND:
            error_type_field = strdup("ERDVUNBOUND");
            break;
        case SRT_EINVALMSGAPI:
            error_type_field = strdup("EINVALMSGAPI");
            break;
        case SRT_EINVALBUFFERAPI:
            error_type_field = strdup("EINVALBUFFERAPI");
            break;
        case SRT_EDUPLISTEN:
            error_type_field = strdup("EDUPLISTEN");
            break;
        case SRT_ELARGEMSG:
            error_type_field = strdup("ELARGEMSG");
            break;
        case SRT_EINVPOLLID:
            error_type_field = strdup("EINVPOLLID");
            break;
        case SRT_EASYNCFAIL:
            error_type_field = strdup("EASYNCFAIL");
            break;
        case SRT_EASYNCSND:
            error_type_field = strdup("EASYNCSND");
            break;
        case SRT_EASYNCRCV:
            error_type_field = strdup("EASYNCRCV");
            break;
        case SRT_ETIMEOUT:
            error_type_field = strdup("ETIMEOUT");
            break;
        case SRT_ECONGEST:
            error_type_field = strdup("ECONGEST");
            break;
        case SRT_EPEERERR:
            error_type_field = strdup("EPEERERR");
            break;
        default:
            error_type_field = strdup("EUNKNOWN");
    }

    jfieldID errorTypeField = env->GetStaticFieldID(errorTypeClazz, error_type_field,
                                                    "L" ERRORTYPE_CLASS ";");
    if (!errorTypeField) {
        LOGE(TAG, "Can't get ErrorType field");
        env->DeleteLocalRef(errorTypeClazz);
        return nullptr;
    }

    jobject errorType = env->GetStaticObjectField(errorTypeClazz, errorTypeField);

    if (error_type_field != nullptr) {
        free(error_type_field);
    }

    env->DeleteLocalRef(errorTypeClazz);

    return errorType;
}

jobject srt_epoll_flag_n2j(JNIEnv *env, int flag) {
    jclass epollFlagClazz = env->FindClass(EPOLLFLAG_CLASS);
    if (!epollFlagClazz) {
        LOGE(TAG, "Can't get "
                EPOLLFLAG_CLASS
                " class");
        return nullptr;
    }

    char *epoll_flag_field = nullptr;
    switch (flag) {
        case 0:
            epoll_flag_field = strdup("CLEAR_ALL");
            break;
        case SRT_EPOLL_ENABLE_EMPTY:
            epoll_flag_field = strdup("ENABLE_EMPTY");
            break;
        case SRT_EPOLL_ENABLE_OUTPUTCHECK:
            epoll_flag_field = strdup("ENABLE_OUTPUTCHECK");
            break;
        default:
            LOGE(TAG, "Epoll Flag: unknown value %d", flag);
    }

    jfieldID epollFlagField = env->GetStaticFieldID(epollFlagClazz, epoll_flag_field,
                                                    "L" EPOLLFLAG_CLASS ";");
    if (!epollFlagField) {
        LOGE(TAG, "Can't get EpollFlag field");
        env->DeleteLocalRef(epollFlagClazz);
        return nullptr;
    }

    jobject epollFlag = env->GetStaticObjectField(epollFlagClazz, epollFlagField);

    if (epoll_flag_field != nullptr) {
        free(epoll_flag_field);
    }

    env->DeleteLocalRef(epollFlagClazz);

    return epollFlag;
}

int32_t srt_epoll_flag_j2n(JNIEnv *env, jobject epollFlag) {
    const char *epoll_flag_field = enums_get_field_id(env, epollFlag);
    if (!epoll_flag_field) {
        LOGE(TAG, "Can't get EpollFlag");
        return -1;
    }

    int32_t flag = -1;
    if (strcmp(epoll_flag_field, "CLEAR_ALL") == 0) {
        flag = 0;
    } else if (strcmp(epoll_flag_field, "ENABLE_EMPTY") == 0) {
        flag = SRT_EPOLL_ENABLE_EMPTY;
    } else if (strcmp(epoll_flag_field, "ENABLE_OUTPUTCHECK") == 0) {
        flag = SRT_EPOLL_ENABLE_OUTPUTCHECK;
    } else {
        LOGE(TAG, "EpollFlag: unknown value %s", epoll_flag_field);
    }

    free((void *) epoll_flag_field);

    return flag;
}

jobject srt_epoll_opt_n2j(JNIEnv *env, uint32_t event) {
    jclass epollEventClazz = env->FindClass(EPOLLOPT_CLASS);
    if (!epollEventClazz) {
        LOGE(TAG, "Can't get "
                EPOLLOPT_CLASS
                " class");
        return nullptr;
    }

    char *epoll_event_field = nullptr;
    switch (event) {
        case SRT_EPOLL_IN:
            epoll_event_field = strdup("IN");
            break;
        case SRT_EPOLL_OUT:
            epoll_event_field = strdup("OUT");
            break;
        case SRT_EPOLL_ERR:
            epoll_event_field = strdup("ERR");
            break;
        case SRT_EPOLL_ET:
            epoll_event_field = strdup("ET");
            break;
        default:
            LOGE(TAG, "Epoll Event: unknown value %d", event);
    }

    jfieldID epollEventField = env->GetStaticFieldID(epollEventClazz, epoll_event_field,
                                                     "L" EPOLLOPT_CLASS ";");
    if (!epollEventField) {
        LOGE(TAG, "Can't get EpollEvent field");
        env->DeleteLocalRef(epollEventClazz);
        return nullptr;
    }

    jobject epollEvent = env->GetStaticObjectField(epollEventClazz, epollEventField);

    if (epoll_event_field != nullptr) {
        free(epoll_event_field);
    }

    env->DeleteLocalRef(epollEventClazz);

    return epollEvent;
}

int32_t srt_epoll_opt_j2n(JNIEnv *env, jobject epollEvent) {
    const char *epoll_event_field = enums_get_field_id(env, epollEvent);
    if (!epoll_event_field) {
        LOGE(TAG, "Can't get EpollEvent");
        return -1;
    }

    int32_t event = -1;
    if (strcmp(epoll_event_field, "IN") == 0) {
        event = SRT_EPOLL_IN;
    } else if (strcmp(epoll_event_field, "OUT") == 0) {
        event = SRT_EPOLL_OUT;
    } else if (strcmp(epoll_event_field, "ERR") == 0) {
        event = SRT_EPOLL_ERR;
    } else if (strcmp(epoll_event_field, "ET") == 0) {
        event = SRT_EPOLL_ET;
    } else {
        LOGE(TAG, "EpollEvent: unknown value %s", epoll_event_field);
    }

    free((void *) epoll_event_field);

    return event;
}

#ifdef __cplusplus
}
#endif
