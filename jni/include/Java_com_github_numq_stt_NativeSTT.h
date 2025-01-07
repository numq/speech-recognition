#include <jni.h>
#include <iostream>
#include <shared_mutex>
#include <mutex>
#include <unordered_map>
#include <memory>
#include <vector>
#include "whisper.h"
#include "deleter.h"

#ifndef _Included_com_github_numq_stt_NativeSTT
#define _Included_com_github_numq_stt_NativeSTT
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_com_github_numq_stt_NativeSTT_initNative
        (JNIEnv *, jclass, jstring);

JNIEXPORT jstring JNICALL Java_com_github_numq_stt_NativeSTT_recognizeNative
        (JNIEnv *, jclass, jlong, jbyteArray);

JNIEXPORT void JNICALL Java_com_github_numq_stt_NativeSTT_freeNative
        (JNIEnv *, jclass, jlong);

#ifdef __cplusplus
}
#endif
#endif
