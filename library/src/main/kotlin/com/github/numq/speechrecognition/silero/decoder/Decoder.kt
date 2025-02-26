package com.github.numq.speechrecognition.silero.decoder

internal interface Decoder {
    fun process(probs: Array<FloatArray>): Result<String>

    companion object {
        fun create(labels: List<String>): Decoder = SileroDecoder(labels = labels)
    }
}