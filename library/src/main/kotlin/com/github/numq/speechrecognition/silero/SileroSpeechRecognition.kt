package com.github.numq.speechrecognition.silero

import com.github.numq.speechrecognition.SpeechRecognition
import com.github.numq.speechrecognition.audio.AudioProcessing.downmixToMono
import com.github.numq.speechrecognition.audio.AudioProcessing.resample
import com.github.numq.speechrecognition.silero.decoder.Decoder
import com.github.numq.speechrecognition.silero.model.SileroModel

internal class SileroSpeechRecognition(
    private val model: SileroModel,
    private val decoder: Decoder,
) : SpeechRecognition.Silero {
    private companion object {
        const val CHANNELS_MONO = 1
    }

    override suspend fun minimumInputSize(sampleRate: Int, channels: Int) = runCatching {
        require(sampleRate > 0) { "Sample rate must be greater than 0" }

        require(channels > 0) { "Number of channels must be greater than 0" }

        sampleRate * channels * 2
    }

    override suspend fun recognize(pcmBytes: ByteArray, sampleRate: Int, channels: Int) = runCatching {
        require(sampleRate > 0) { "Sample rate must be greater than 0" }

        require(channels > 0) { "Channel count must be at least 1" }

        if (pcmBytes.isEmpty()) return@runCatching ""

        val monoData = downmixToMono(inputData = pcmBytes, channels = channels)

        val resampledData = resample(
            inputData = monoData,
            channels = CHANNELS_MONO,
            inputSampleRate = sampleRate,
            outputSampleRate = SpeechRecognition.Silero.SAMPLE_RATE
        )

        val floatSamples = FloatArray(resampledData.size / 2) { i ->
            ((resampledData[i * 2].toInt() and 0xFF) or (resampledData[i * 2 + 1].toInt() shl 8)) / 32767f
        }

        decoder.process(probs = model.process(arrayOf(floatSamples)).getOrThrow()).getOrThrow()
    }

    override fun reset() = model.reset()

    override fun close() = runCatching {
        reset().getOrDefault(Unit)
        model.close()
    }.getOrDefault(Unit)
}