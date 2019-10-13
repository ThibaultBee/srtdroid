#include <jni.h>
#include <string>
#include <android/log.h>
#include "srt/srt.h"
#include "srt/logging_api.h"

extern "C" {

#define TAG "JNIGlue"
#define SRTLIB_TAG "SRTLIB"

// Log tools
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

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
