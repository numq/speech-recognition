#include "Java_com_github_numq_speechrecognition_whisper_NativeWhisperSpeechRecognition.h"

static jclass exceptionClass;
static std::shared_mutex mutex;
static std::unordered_map<jlong, whisper_context_ptr> pointers;

void handleException(JNIEnv *env, const std::string &errorMessage) {
    env->ThrowNew(exceptionClass, errorMessage.c_str());
}

whisper_context *getPointer(jlong handle) {
    std::shared_lock<std::shared_mutex> lock(mutex);

    auto it = pointers.find(handle);
    if (it == pointers.end()) {
        throw std::runtime_error("Invalid handle");
    }
    return it->second.get();
}

std::vector<float> convertSamples(JNIEnv *env, jbyteArray pcmBytes) {
    jsize length = env->GetArrayLength(pcmBytes);
    if (length % sizeof(int16_t) != 0) {
        throw std::runtime_error("Invalid PCM data size");
    }
    jbyte *byteArray = env->GetByteArrayElements(pcmBytes, nullptr);
    if (!byteArray) {
        throw std::runtime_error("Failed to get byte array elements");
    }

    std::vector<float> samples(length / sizeof(int16_t));
    try {
        for (jsize i = 0; i < length; i += 2) {
            auto sample = (static_cast<int16_t>(byteArray[i + 1]) << 8) | (byteArray[i] & 0xFF);
            samples[i / 2] = static_cast<float>(sample) / 32768.0f;
        }
    } catch (...) {
        env->ReleaseByteArrayElements(pcmBytes, byteArray, JNI_ABORT);
        throw;
    }

    env->ReleaseByteArrayElements(pcmBytes, byteArray, JNI_ABORT);

    return samples;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;

    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_8) != JNI_OK) {
        return JNI_ERR;
    }

    exceptionClass = reinterpret_cast<jclass>(env->NewGlobalRef(env->FindClass("java/lang/RuntimeException")));
    if (exceptionClass == nullptr) {
        return JNI_ERR;
    }

    whisper_log_set(nullptr, nullptr);

    return JNI_VERSION_1_8;
}

JNIEXPORT void JNICALL
JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env;

    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_8) != JNI_OK) return;

    if (exceptionClass) env->DeleteGlobalRef(exceptionClass);

    pointers.clear();
}

JNIEXPORT jlong JNICALL
Java_com_github_numq_speechrecognition_whisper_NativeWhisperSpeechRecognition_initNative(JNIEnv *env, jclass thisClass,
                                                                                         jstring modelPath) {
    std::unique_lock<std::shared_mutex> lock(mutex);

    try {
        const char *modelPathChars = env->GetStringUTFChars(modelPath, nullptr);
        if (!modelPathChars) {
            throw std::runtime_error("Failed to get model path string");
        }

        std::string modelPathStr(modelPathChars);
        env->ReleaseStringUTFChars(modelPath, modelPathChars);

        if (modelPathStr.empty()) {
            throw std::runtime_error("Model path should not be empty");
        }

        auto params = whisper_context_default_params();

        auto context = whisper_init_from_file_with_params(modelPathStr.c_str(), params);
        if (!context) {
            throw std::runtime_error("Failed to load model");
        }

        whisper_context_ptr ptr(context);

        auto handle = reinterpret_cast<jlong>(ptr.get());

        pointers[handle] = std::move(ptr);

        return handle;
    } catch (const std::exception &e) {
        handleException(env, e.what());
        return -1;
    }
}

JNIEXPORT jstring JNICALL
Java_com_github_numq_speechrecognition_whisper_NativeWhisperSpeechRecognition_recognizeNative(JNIEnv *env,
                                                                                              jclass thisClass,
                                                                                              jlong handle,
                                                                                              jbyteArray pcmBytes,
                                                                                              jfloat temperature) {
    std::shared_lock<std::shared_mutex> lock(mutex);

    try {
        auto context = getPointer(handle);

        auto samples = convertSamples(env, pcmBytes);

        auto params = whisper_full_default_params(whisper_sampling_strategy::WHISPER_SAMPLING_GREEDY);
        params.no_context = true;
        params.no_timestamps = true;
        params.single_segment = true;
        params.suppress_blank = true;
        params.suppress_nst = true;
        params.temperature = static_cast<float>(temperature);

        if (whisper_full(context, params, samples.data(), static_cast<int>(samples.size())) != 0) {
            return env->NewStringUTF("");
        }

        std::string result;

        const int n_segments = whisper_full_n_segments(context);
        for (int i = 0; i < n_segments; ++i) {
            result += whisper_full_get_segment_text(context, i);
        }

        return env->NewStringUTF(result.c_str());
    } catch (const std::exception &e) {
        handleException(env, e.what());
        return nullptr;
    }
}

JNIEXPORT void JNICALL
Java_com_github_numq_speechrecognition_whisper_NativeWhisperSpeechRecognition_freeNative(JNIEnv *env, jclass thisClass,
                                                                                         jlong handle) {
    std::unique_lock<std::shared_mutex> lock(mutex);

    try {
        if (pointers.erase(handle) == 0) {
            handleException(env, "Unable to free native pointer");
        }
    } catch (const std::exception &e) {
        handleException(env, e.what());
    }
}