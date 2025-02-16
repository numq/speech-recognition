package com.github.numq.speechrecognition.whisper

import com.github.numq.speechrecognition.SpeechRecognition
import com.github.numq.speechrecognition.audio.AudioProcessing.downmixToMono
import com.github.numq.speechrecognition.audio.AudioProcessing.resample
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class WhisperSpeechRecognition(
    private val nativeWhisperSpeechRecognition: NativeWhisperSpeechRecognition,
) : SpeechRecognition.Whisper {
    private companion object {
        const val CHANNELS_MONO = 1
    }

    private val mutex = Mutex()

    private var temperature = 0f

    override suspend fun adjustTemperature(temperature: Float) = mutex.withLock {
        runCatching {
            this.temperature = temperature
        }
    }

    override suspend fun minimumInputSize(sampleRate: Int, channels: Int) = mutex.withLock {
        runCatching {
            require(sampleRate > 0) { "Sample rate must be greater than 0" }

            require(channels > 0) { "Number of channels must be greater than 0" }

            sampleRate * channels * 2
        }
    }

    override suspend fun recognize(pcmBytes: ByteArray, sampleRate: Int, channels: Int) = mutex.withLock {
        runCatching {
            require(sampleRate > 0) { "Sample rate must be greater than 0" }

            require(channels > 0) { "Number of channels must be greater than 0" }

            val monoBytes = downmixToMono(inputData = pcmBytes, channels = channels)

            val resampledBytes = resample(
                inputData = monoBytes,
                channels = CHANNELS_MONO,
                inputSampleRate = sampleRate,
                outputSampleRate = SpeechRecognition.Whisper.SAMPLE_RATE
            )

            nativeWhisperSpeechRecognition.recognize(pcmBytes = resampledBytes, temperature = temperature)
        }
    }

    override fun close() = runCatching {
        super.close()

        nativeWhisperSpeechRecognition.close()
    }.getOrDefault(Unit)
}