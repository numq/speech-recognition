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

    private fun extractTensorData(output: OrtSession.Result): Array<FloatArray> {
        val outputTensor = output.lastOrNull()?.value as? OnnxTensor

        val tensorData = outputTensor?.value as? Array<Array<FloatArray>>

        return tensorData?.firstOrNull() ?: arrayOf()
    }

    override fun process(input: Array<FloatArray>) = runCatching {
        OnnxTensor.createTensor(env, input).use { inputTensor ->
            val inputs = mapOf("input" to inputTensor)

            session.run(inputs).use { output ->
                extractTensorData(output)
            }
        }
    }

    override fun reset() = runCatching {
        process(arrayOf(FloatArray(16_000)))

        Unit
    }

    override fun close() {
        session.close()
    }
}