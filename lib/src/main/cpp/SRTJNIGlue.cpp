#include <jni.h>
#include <string>

#include "srt/srt.h"

extern "C" {

// Library Initialization
JNIEXPORT jint JNICALL
Java_com_github_thibaultbee_srtwrapper_Srt_nativeStartUp(JNIEnv *env, jobject obj) {
    return srt_startup();
}

JNIEXPORT jint JNICALL
Java_com_github_thibaultbee_srtwrapper_Srt_nativeCleanUp(JNIEnv *env, jobject obj) {
    return srt_cleanup();
}

}
