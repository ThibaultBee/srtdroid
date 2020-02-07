#include <sys/socket.h>

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

/**
 * @brief Convert Java InetSocketAddres to sockaddr_in
 *
 * @param env Java environment
 * @param jinet_socket_address Java InetSocketAddres
 * @return return sockaddress in C domain
 */
struct sockaddr_in *
inet_socket_address_from_java_to_native(JNIEnv *env, jobject jinet_socket_address);

/**
 * @brief Convert sockaddr_in to Java InetSocketAddres
 *
 * @param env Java environment
 * @param sockaddr socket address in C domain
 * @param addrlen socket address length
 * @return return Java InetSocketAddres
 */
jobject
inet_socket_address_from_native_to_java(JNIEnv *env, struct sockaddr_in * sa, int addrlen);

/**
 * @brief Convert Java SRT Optval to C optval for SRT library
 *
 * @param env Java environment
 * @param jopval Java SRT optval
 * @param optlen length of C optval output
 * @return return optval in C domain
 */
void *
srt_optval_from_java_to_native(JNIEnv *env, jobject jopval, int *optlen);

/**
 * @brief Convert Java MsgCtrl to C SRT_MSGCTRL for SRT library
 *
 * @param env Java environment
 * @param jopval Java MsgCtrl
 * @return return MsgCtrl in C domain
 */
SRT_MSGCTRL *
srt_msgctrl_from_java_to_native(JNIEnv *env, jobject jmsgCtrl);

// Utils
/**
 * @brief Convert Java SRT Socket to SRTSOCKET for SRT library
 *
 * @param env Java environment
 * @param u Java SRT Socket
 * @return return corresponding SRTSOCKET value
 */
SRTSOCKET srt_socket_from_java_to_native(JNIEnv *env, jobject ju);

/**
 * @brief Convert SRTSOCKET for SRT library to Java SRT Socket
 *
 * @param env Java environment
 * @param u Java SRT Socket
 * @return return corresponding SRTSOCKET value
 */
jobject srt_socket_from_native_to_java(JNIEnv *env, SRTSOCKET u);

/**
 * @brief Create a Pair Java object
 *
 * @param env Java environment
 * @param first Pair first argument
 * @param second Pair second argument
 * @return return a Pair Java object containing first and second arguments
 */
jobject create_java_pair(JNIEnv *env, jobject first, jobject second);

#ifdef __cplusplus
}
#endif
