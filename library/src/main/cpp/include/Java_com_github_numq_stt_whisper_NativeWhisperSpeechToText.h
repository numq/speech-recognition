#include <jni.h>
#include <iostream>
#include <shared_mutex>
#include <mutex>
#include <unordered_map>
#include <memory>
#include <vector>
#include "whisper.h"
#include "deleter.h"

#ifndef _Included_com_github_numq_stt_whisper_NativeWhisperSpeechToText
#define _Included_com_github_numq_stt_whisper_NativeWhisperSpeechToText
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_com_github_numq_stt_whisper_NativeWhisperSpeechToText_initNative
        (JNIEnv *, jclass, jstring);

JNIEXPORT jstring JNICALL Java_com_github_numq_stt_whisper_NativeWhisperSpeechToText_recognizeNative
        (JNIEnv *, jclass, jlong, jbyteArray);

JNIEXPORT void JNICALL Java_com_github_numq_stt_whisper_NativeWhisperSpeechToText_freeNative
        (JNIEnv *, jclass, jlong);

#ifdef __cplusplus
}
#endif
#endif
