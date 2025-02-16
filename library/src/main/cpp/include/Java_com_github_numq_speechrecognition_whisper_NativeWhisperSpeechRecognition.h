#include <jni.h>
#include <iostream>
#include <shared_mutex>
#include <mutex>
#include <unordered_map>
#include <memory>
#include <vector>
#include "whisper/whisper.h"
#include "deleter.h"

#ifndef _Included_com_github_numq_speechrecognition_whisper_NativeWhisperSpeechRecognition
#define _Included_com_github_numq_speechrecognition_whisper_NativeWhisperSpeechRecognition
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_com_github_numq_speechrecognition_whisper_NativeWhisperSpeechRecognition_initNative
        (JNIEnv *, jclass, jstring);

JNIEXPORT jstring JNICALL Java_com_github_numq_speechrecognition_whisper_NativeWhisperSpeechRecognition_recognizeNative
        (JNIEnv *, jclass, jlong, jbyteArray, jfloat);

JNIEXPORT void JNICALL Java_com_github_numq_speechrecognition_whisper_NativeWhisperSpeechRecognition_freeNative
        (JNIEnv *, jclass, jlong);

#ifdef __cplusplus
}
#endif
#endif
