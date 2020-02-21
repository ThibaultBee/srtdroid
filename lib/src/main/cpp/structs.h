#include <jni.h>

#include "srt/srt.h"

#ifdef __cplusplus
extern "C" {
#endif

#define ERROR_CLASS "com/github/thibaultbee/srtwrapper/models/Error"
#define MSGCTRL_CLASS "com/github/thibaultbee/srtwrapper/models/MsgCtrl"
#define SRTSOCKET_CLASS "com/github/thibaultbee/srtwrapper/models/Socket"
#define SRT_CLASS "com/github/thibaultbee/srtwrapper/Srt"

#define INETSOCKETADDRESS_CLASS "java/net/InetSocketAddress"
#define PAIR_CLASS "android/util/Pair"
#define LONG_CLASS "java/lang/Long"
#define BOOLEAN_CLASS "java/lang/Boolean"
#define INT_CLASS "java/lang/Integer"

/**
 * @brief Convert Java InetSocketAddres to sockaddr_in
 *
 * @param env Java environment
 * @param inetSocketAddress Java InetSocketAddres
 * @return return sockaddress in C domain
 */
struct sockaddr_in *
sockaddr_inet_j2n(JNIEnv *env, jobject inetSocketAddress);

/**
 * @brief Convert sockaddr_in to Java InetSocketAddres
 *
 * @param env Java environment
 * @param sockaddr socket address in C domain
 * @param sockaddr_len socket address length
 * @return return Java InetSocketAddres
 */
jobject
sockaddr_inet_n2j(JNIEnv *env, struct sockaddr_in *sa, int sockaddr_len);

/**
 * @brief Convert Java SRT Optval to C optval for SRT library
 *
 * @param env Java environment
 * @param optVal Java SRT optval
 * @param optval_len length of C optval output
 * @return return optval in C domain
 */
void *
srt_optval_j2n(JNIEnv *env, jobject optVal, int *optval_len);

/**
 * @brief Convert Native SRT Optval to Java optval object
 *
 * @param env Java environment
 * @param u socket to get optval
 * @param level ignored
 * @param sockOpt Java option to set
 * @return return a Java OptVal object
 */
jobject srt_optval_n2j(JNIEnv *env, int u, int level, jobject sockOpt);

/**
 * @brief Convert Java MsgCtrl to C SRT_MSGCTRL for SRT library
 *
 * @param env Java environment
 * @param msgCtrl Java MsgCtrl
 * @return return MsgCtrl in C domain
 */
SRT_MSGCTRL *
srt_msgctrl_j2n(JNIEnv *env, jobject msgCtrl);

// Utils
/**
 * @brief Convert Java SRT Socket to SRTSOCKET for SRT library
 *
 * @param env Java environment
 * @param srtSocket Java SRT Socket
 * @return return corresponding SRTSOCKET value
 */
SRTSOCKET srt_socket_j2n(JNIEnv *env, jobject srtSocket);

/**
 * @brief Convert SRTSOCKET for SRT library to Java SRT Socket
 *
 * @param env Java environment
 * @param srtsocket Native SRTSOCKET value
 * @return return corresponding Java SRT Socket
 */
jobject srt_socket_n2j(JNIEnv *env, SRTSOCKET srtsocket);

/**
 * @brief Create a Pair Java object
 *
 * @param env Java environment
 * @param first Pair first argument
 * @param second Pair second argument
 * @return return a Pair Java object containing first and second arguments
 */
jobject new_pair(JNIEnv *env, jobject first, jobject second);

#ifdef __cplusplus
}
#endif
