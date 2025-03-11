package com.github.numq.speechrecognition.silero.model

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession

internal class OnnxSileroModel(modelPath: String) : SileroModel {
    private val env by lazy { OrtEnvironment.getEnvironment() }

    private val session by lazy {
        env.createSession(modelPath, OrtSession.SessionOptions().apply {
            setOptimizationLevel(OrtSession.SessionOptions.OptLevel.NO_OPT)
        })
    }

    private fun extractTensorData(result: OrtSession.Result): Array<FloatArray> {
        val outputTensor = result.lastOrNull()?.value as? OnnxTensor

        val tensorData = outputTensor?.value as? Array<Array<FloatArray>>

        return tensorData?.firstOrNull() ?: arrayOf()
    }

    override fun process(input: Array<FloatArray>) = runCatching {
        OnnxTensor.createTensor(env, input).use { inputTensor ->
            session.run(mapOf("input" to inputTensor)).use(::extractTensorData)
        }
    }

    override fun reset() = runCatching {
        process(arrayOf(FloatArray(16_000)))

        Unit
    }

    override fun close() {
        session.close()
        env.close()
    }
}