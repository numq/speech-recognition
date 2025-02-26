package com.github.numq.speechrecognition.silero.model

internal interface SileroModel : AutoCloseable {
    fun process(input: Array<FloatArray>): Result<Array<FloatArray>>
    fun reset(): Result<Unit>

    companion object {
        fun create(modelPath: String): SileroModel = OnnxSileroModel(modelPath = modelPath)
    }
}