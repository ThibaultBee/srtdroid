#include <jni.h>
#include <string>
#include <android/log.h>
#include "srt/srt.h"

extern "C" {

#define TAG "JNIGlue"

// Log tools
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

// Library Initialization
JNIEXPORT jint JNICALL
Java_com_github_thibaultbee_srtwrapper_Srt_nativeStartUp(JNIEnv *env, jobject obj) {
    return srt_startup();
}

JNIEXPORT jint JNICALL
Java_com_github_thibaultbee_srtwrapper_Srt_nativeCleanUp(JNIEnv *env, jobject obj) {
    return srt_cleanup();
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
