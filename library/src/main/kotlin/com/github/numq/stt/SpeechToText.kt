package com.github.numq.stt

import com.github.numq.stt.whisper.NativeWhisperSpeechToText
import com.github.numq.stt.whisper.WhisperSpeechToText

interface SpeechToText : AutoCloseable {
    fun recognize(pcmBytes: ByteArray): Result<String>

    interface Whisper : SpeechToText {
        companion object {
            private var isLoaded = false

            fun load(
                ggmlbase: String,
                ggmlcpu: String,
                ggmlcuda: String,
                ggml: String,
                whisper: String,
                libstt: String,
            ) = runCatching {
                System.load(ggmlbase)
                System.load(ggmlcpu)
                System.load(ggmlcuda)
                System.load(ggml)
                System.load(whisper)
                System.load(libstt)
            }.onSuccess {
                isLoaded = true
            }

            fun create(modelPath: String): Result<SpeechToText> = runCatching {
                check(isLoaded) { "Native binaries were not loaded" }

                WhisperSpeechToText(nativeWhisperSpeechToText = NativeWhisperSpeechToText(modelPath = modelPath))
            }
        }
    }
}