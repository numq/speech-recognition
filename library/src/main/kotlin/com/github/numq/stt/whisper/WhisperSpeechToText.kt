package com.github.numq.stt.whisper

import com.github.numq.stt.SpeechToText

internal class WhisperSpeechToText(private val nativeWhisperSpeechToText: NativeWhisperSpeechToText) : SpeechToText {
    override fun recognize(pcmBytes: ByteArray) = runCatching {
        nativeWhisperSpeechToText.recognize(pcmBytes = pcmBytes)
    }

    override fun close() = runCatching { nativeWhisperSpeechToText.close() }.getOrDefault(Unit)
}