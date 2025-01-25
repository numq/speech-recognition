package com.github.numq.stt.whisper

import com.github.numq.stt.SpeechToText
import com.github.numq.stt.audio.AudioProcessing

internal class WhisperSpeechToText(private val nativeWhisperSpeechToText: NativeWhisperSpeechToText) : SpeechToText {
    private val paddingSize = SpeechToText.SAMPLE_RATE * 2

    override fun recognize(pcmBytes: ByteArray, sampleRate: Int, channels: Int) = runCatching {
        val monoBytes = AudioProcessing.downmixToMono(pcmBytes, channels)

        val resampledBytes = AudioProcessing.resample(monoBytes, sampleRate, SpeechToText.SAMPLE_RATE)

        val paddedBytes = if (resampledBytes.size < paddingSize) resampledBytes.copyOf(paddingSize) else resampledBytes

        nativeWhisperSpeechToText.recognize(pcmBytes = paddedBytes)
    }

    override fun close() = runCatching { nativeWhisperSpeechToText.close() }.getOrDefault(Unit)
}