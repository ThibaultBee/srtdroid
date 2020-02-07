#include <jni.h>

#include "srt/srt.h"

#ifdef __cplusplus
extern "C" {
#endif

#define ENUM_PACKAGE "com.github.thibaultbee.srtwrapper.enums"

#define ERRORTYPE_CLASS "com/github/thibaultbee/srtwrapper/enums/ErrorType"
#define SOCKOPT_CLASS "com/github/thibaultbee/srtwrapper/enums/SockOpt"
#define SOCKSTATUS_CLASS "com/github/thibaultbee/srtwrapper/enums/SockStatus"

/**
 * @brief Convert Java address family to native value
 *
 * @param env Java environment
 * @param jenum_af_value Java address family enumeration member
 * @return return corresponding native address family value
 */
int address_family_from_java_to_native(JNIEnv *env, jobject jenum_af_value);

/**
 * @brief Convert Java SRT Option to native value
 *
 * @param env Java environment
 * @param jenum_srt_option_value Java SRT Option enumeration member
 * @return return corresponding native SRT option value
 */
int srt_sockopt_from_java_to_native(JNIEnv *env, jobject jenum_srt_option_value);

/**
 * @brief Convert Java SRT Transtype to native value
 *
 * @param env Java environment
 * @param jenum_srt_transtype_value Java SRT TransType enumeration member
 * @return return corresponding native SRT option value
 */
SRT_TRANSTYPE srt_transtype_from_java_to_native(JNIEnv *env, jobject jenum_srt_transtype_value);

/**
 * @brief Convert Java SRT KMState to native value
 *
 * @param env Java environment
 * @param jenum_srt_kmstate_value Java SRT KMState enumeration member
 * @return return corresponding native SRT option value
 */
SRT_KM_STATE srt_kmstate_from_java_to_native(JNIEnv *env, jobject jenum_srt_kmstate_value);

/**
 * @brief Convert native SRT_SOCKSTATUS to Java SRT SockStatus
 *
 * @param env Java environment
 * @param sock_status Native SRT_SOCKSTATUS
 * @return return corresponding Java SRT SockStatus
 */
jobject srt_sock_status_from_native_to_java(JNIEnv *env, SRT_SOCKSTATUS sock_status);

/**
 * @brief Convert Java SRT error to native SRT error
 *
 * @param env Java environment
 * @param jerrorType Java SRT error
 * @return return corresponding Java SRT error
 */
int error_from_java_to_native(JNIEnv *env, jobject jerrorType);

/**
 * @brief Convert native SRT error to Java SRT error
 *
 * @param env Java environment
 * @param error_type Native SRT error
 * @return return corresponding Java SRT error
 */
jobject error_from_native_to_java(JNIEnv *env, int error_type);

#ifdef __cplusplus
}
#endif
