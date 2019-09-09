#include <jni.h>
#include <string>

#include "srt/srt.h"

extern "C" {

JNIEXPORT void JNICALL Java_com_example_srtwrapper_SrtWrapper_startUp(JNIEnv * env, jobject obj)
{
  srt_startup();
}

}
