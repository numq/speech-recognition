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
        const val SAMPLE_RATE = 24_000
        const val CHANNELS_MONO = 1
        const val DEFAULT_LANGUAGE = "auto"
        const val DEFAULT_TRANSLATION_FLAG = false
    }

    private val mutex = Mutex()

    private var temperature = 0f

    override val sampleRate = SAMPLE_RATE

    override val channels = CHANNELS_MONO

    override var language = DEFAULT_LANGUAGE

    override var translationFlag = DEFAULT_TRANSLATION_FLAG

    override suspend fun setLanguage(language: String) = mutex.withLock {
        runCatching {
            this.language = language.takeIf(String::isNotBlank) ?: DEFAULT_LANGUAGE
        }
    }

    override suspend fun setTranslationFlag(translationFlag: Boolean) = mutex.withLock {
        runCatching {
            this.translationFlag = translationFlag
        }
    }

    override suspend fun adjustTemperature(temperature: Float) = mutex.withLock {
        runCatching {
            this.temperature = temperature
        }
    }

    override fun minimumInputSize(sampleRate: Int, channels: Int) = runCatching {
        require(sampleRate > 0) { "Sample rate must be greater than 0" }

        require(channels > 0) { "Number of channels must be greater than 0" }

        sampleRate * channels * 2
    }

    override suspend fun recognize(pcmBytes: ByteArray, sampleRate: Int, channels: Int) = mutex.withLock {
        runCatching {
            require(sampleRate > 0) { "Sample rate must be greater than 0" }

            require(channels > 0) { "Number of channels must be greater than 0" }

            val monoBytes = downmixToMono(inputData = pcmBytes, channels = channels)

            val resampledBytes = resample(
                inputData = monoBytes,
                inputSampleRate = sampleRate,
                outputSampleRate = SAMPLE_RATE,
                channels = CHANNELS_MONO,
            )

            nativeWhisperSpeechRecognition.recognize(
                pcmBytes = resampledBytes,
                temperature = temperature,
                language = language,
                translationFlag = translationFlag,
            )
        }
    }

    override fun close() = runCatching {
        super.close()

        nativeWhisperSpeechRecognition.close()
    }.getOrDefault(Unit)
}