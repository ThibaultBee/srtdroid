#include <jni.h>

#include "srt/srt.h"

#ifdef __cplusplus
extern "C" {
#endif

#define ENUM_PACKAGE "com.github.thibaultbee.srtwrapper.enums"

#define EPOLLFLAG_CLASS "com/github/thibaultbee/srtwrapper/enums/EpollFlag"
#define EPOLLOPT_CLASS "com/github/thibaultbee/srtwrapper/enums/EpollOpt"
#define ERRORTYPE_CLASS "com/github/thibaultbee/srtwrapper/enums/ErrorType"
#define KMSTATE_CLASS "com/github/thibaultbee/srtwrapper/enums/KMState"
#define SOCKOPT_CLASS "com/github/thibaultbee/srtwrapper/enums/SockOpt"
#define SOCKSTATUS_CLASS "com/github/thibaultbee/srtwrapper/enums/SockStatus"
#define TRANSTYPE_CLASS "com/github/thibaultbee/srtwrapper/enums/Transtype"

/**
 * @brief Convert Java address family to native value
 *
 * @param env Java environment
 * @param addressFamily Java address family enumeration member
 * @return return corresponding native address family value
 */
int address_family_j2n(JNIEnv *env, jobject addressFamily);

/**
 * @brief Convert Java SRT Option to native value
 *
 * @param env Java environment
 * @param sockOpt Java SRT Option enumeration member
 * @return return corresponding native SRT option value
 */
int srt_sockopt_j2n(JNIEnv *env, jobject sockOpt);

/**
 * @brief Convert native SRT Transtype to Java value
 *
 * @param env Java environment
 * @param transtype Native SRT TransType
 * @return return corresponding Java SRT option value
 */
jobject srt_transtype_n2j(JNIEnv *env, SRT_TRANSTYPE transtype);

/**
 * @brief Convert Java SRT Transtype to native value
 *
 * @param env Java environment
 * @param transType Java SRT TransType enumeration member
 * @return return corresponding native SRT option value
 */
SRT_TRANSTYPE srt_transtype_j2n(JNIEnv *env, jobject transType);

/**
 * @brief Convert native SRT KMState to Java value
 *
 * @param env Java environment
 * @param kmState Native KMState
 * @return return corresponding Java SRT option value
 */
jobject srt_kmstate_n2j(JNIEnv *env, SRT_KM_STATE kmstate);

/**
 * @brief Convert Java SRT KMState to native value
 *
 * @param env Java environment
 * @param kmState Java SRT KMState enumeration member
 * @return return corresponding native SRT option value
 */
SRT_KM_STATE srt_kmstate_j2n(JNIEnv *env, jobject kmState);

/**
 * @brief Convert native SRT_SOCKSTATUS to Java SRT SockStatus
 *
 * @param env Java environment
 * @param sockstatus Native SRT_SOCKSTATUS
 * @return return corresponding Java SRT SockStatus
 */
jobject srt_sockstatus_n2j(JNIEnv *env, SRT_SOCKSTATUS sockstatus);

/**
 * @brief Convert Java SRT error to native SRT error
 *
 * @param env Java environment
 * @param errorType Java SRT error
 * @return return corresponding Java SRT error
 */
int srt_error_j2n(JNIEnv *env, jobject errorType);

/**
 * @brief Convert native SRT error to Java SRT error
 *
 * @param env Java environment
 * @param error_type Native SRT error
 * @return return corresponding Java SRT error
 */
jobject srt_error_n2j(JNIEnv *env, int error_type);

/**
 * @brief Convert Java EpollEvent to native epoll event
 *
 * @param env Java environment
 * @param epollEvent Java EpollEvent
 * @return return corresponding native epoll event
 */
int32_t srt_epoll_opt_j2n(JNIEnv *env, jobject epollEvent);

/**
 * @brief Convert native epoll event to Java EpollEvent
 *
 * @param env Java environment
 * @param event native epoll event
 * @return return corresponding Java EpollEvent
 */
jobject srt_epoll_opt_n2j(JNIEnv *env, uint32_t event);

/**
 * @brief Convert Java EpollFlag to native epoll flag
 *
 * @param env Java environment
 * @param epollFlag Java EpollFlag
 * @return return corresponding native epoll flag
 */
int32_t srt_epoll_flag_j2n(JNIEnv *env, jobject epollFlag);

/**
 * @brief Convert native epoll flag to Java EpollFlag
 *
 * @param env Java environment
 * @param flag native epoll flag
 * @return return corresponding Java EpollFlag
 */
jobject srt_epoll_flag_n2j(JNIEnv *env, int flag);

#ifdef __cplusplus
}
#endif
