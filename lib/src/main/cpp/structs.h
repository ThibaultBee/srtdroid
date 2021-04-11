/*
 * Copyright (C) 2021 Thibault B.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <jni.h>

#include "srt/srt.h"

#ifdef __cplusplus
extern "C" {
#endif

#define EPOLL_CLASS "com/github/thibaultbee/srtdroid/models/Epoll"
#define EPOLLEVENT_CLASS "com/github/thibaultbee/srtdroid/models/EpollEvent"
#define ERROR_CLASS "com/github/thibaultbee/srtdroid/models/Error"
#define MSGCTRL_CLASS "com/github/thibaultbee/srtdroid/models/MsgCtrl"
#define SRTSOCKET_CLASS "com/github/thibaultbee/srtdroid/models/Socket"
#define STATS_CLASS "com/github/thibaultbee/srtdroid/models/Stats"
#define SRT_CLASS "com/github/thibaultbee/srtdroid/Srt"
#define TIME_CLASS "com/github/thibaultbee/srtdroid/models/Time"

#define INETSOCKETADDRESS_CLASS "java/net/InetSocketAddress"
#define PAIR_CLASS "android/util/Pair"
#define LIST_CLASS "java/util/List"
#define LONG_CLASS "java/lang/Long"
#define BOOLEAN_CLASS "java/lang/Boolean"
#define INT_CLASS "java/lang/Integer"

/**
 * @brief Convert Java InetSocketAddres to sockaddr_storage
 *
 * @param env Java environment
 * @param inetSocketAddress Java InetSocketAddres
 * @return return sockaddress in C domain
 */
struct sockaddr_storage *
sockaddr_inet_j2n(JNIEnv *env, jobject inetSocketAddress, int *size);

/**
 * @brief Convert sockaddr_storage to Java InetSocketAddres
 *
 * @param env Java environment
 * @param clazz SockAddrInet class if already known
 * @param sockaddr socket address in C domain
 * @return return Java InetSocketAddres
 */
jobject
sockaddr_inet_n2j(JNIEnv *env, jclass clazz, struct sockaddr_storage *sa);

/**
 * @brief From a C int create a Java Integer object
 *
 * @param env Java environment
 * @param val C integer
 * @return return Java Integer object
 */
jobject int_new(JNIEnv *env, int val);

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

/**
 * @brief Set Socket object srtsocket field
 *
 * @param env Java environment
 * @param srtSocket Java Socket object
 * @param srtsocket Native srt socket value
 */
void srt_socket_set(JNIEnv *env, jobject srtSocket, SRTSOCKET srtsocket);

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
 * @param clazz SrtSocket class if already known
 * @param srtsocket Native SRTSOCKET value
 * @return return corresponding Java SRT Socket
 */
jobject srt_socket_n2j(JNIEnv *env, jclass clazz, SRTSOCKET srtsocket);

/**
 * @brief Convert Stats from SRT library SRT_TRACEBSTATS to Java Stats
 *
 * @param env Java environment
 * @param tracebstats Native SRT_TRACEBSTATS structure
 * @return return corresponding Java Stats
 */
jobject srt_stats_n2j(JNIEnv *env, SRT_TRACEBSTATS tracebstats);

/**
 * @brief Convert Java Socket List to a native set of SRTSOCKET
 *
 * @param env Java environment
 * @param srtSocketList List of Socket
 * @param nSockets Size of Java Socket List
 * @return return native set of SRTSOCKET
 */
SRTSOCKET *srt_sockets_j2n(JNIEnv *env, jobject srtSocketList, int *nSockets);

/**
 * @brief Set Epoll object eid field
 *
 * @param env Java environment
 * @param epoll Java Epoll object
 * @param eid Native srt epoll value
 */
void srt_epoll_set_eid(JNIEnv *env, jobject epoll, int eid);

/**
 * @brief Convert Java Epoll to int for SRT library
 *
 * @param env Java environment
 * @param epoll Java Epoll
 * @return return corresponding int value
 */
int srt_epoll_j2n(JNIEnv *env, jobject epoll);

/**
 * @brief Convert eid for SRT library to Java Epoll
 *
 * @param env Java environment
 * @param eid Native eid
 * @return return corresponding Java Epoll
 */
jobject srt_epoll_n2j(JNIEnv *env, int eid);

/**
 * @brief Convert EpollEvent object List to events set
 *
 * @param env Java environment
 * @param epollEventList Java List of epoll events
 * @return return epoll events set
 */
int srt_epoll_opts_j2n(JNIEnv *env, jobject epollEventList);

/**
 * @brief Convert EpollFlag object List to flags set
 *
 * @param env Java environment
 * @param epollFlags Java List of epoll flags
 * @return return epoll flags set
 */
int srt_epoll_flags_j2n(JNIEnv *env, jobject epollFlagList);

/**
 * @brief Convert native epoll flags set to an EpollFlag object List
 *
 * @param env Java environment
 * @param epoll_flags native epoll flags set
 * @return return EpollFlag object List
 */
jobject srt_epoll_flags_n2j(JNIEnv *env, int epoll_flags);

/**
 * @brief Convert EpollEvent object List to a SRT_EPOLL_EVENT set
 *
 * @param env Java environment
 * @param epollEvents Java EpollEvent List
 * @param nEvents Size of Java EpollEvent List
 * @return return a pointer to an set of SRT_EPOLL_EVENT
 */
SRT_EPOLL_EVENT *srt_epoll_events_j2n(JNIEnv *env, jobject epollEventList, int *nEvents);

/**
 * @brief Create a Pair Java object
 *
 * @param env Java environment
 * @param first Pair first argument
 * @param second Pair second argument
 * @return return a Pair Java object containing first and second arguments
 */
jobject pair_new(JNIEnv *env, jobject first, jobject second);

#ifdef __cplusplus
}
#endif
