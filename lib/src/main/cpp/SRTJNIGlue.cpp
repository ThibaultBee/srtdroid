#include <jni.h>
#include <string>

#include "srt/srt.h"

extern "C" {

JNIEXPORT void JNICALL Java_com_github_thibaultbee_srtwrapper_Srt_nativeStartup(JNIEnv * env, jobject obj)
{
  srt_startup();
}

}
