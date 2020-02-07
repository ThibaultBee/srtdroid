#include <android/log.h>

#ifdef __cplusplus
extern "C" {
#endif

// Log tools
#define  LOGE(TAG, ...)  __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define  LOGW(TAG, ...)  __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define  LOGD(TAG, ...)  __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define  LOGI(TAG, ...)  __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

#ifdef __cplusplus
}
#endif
