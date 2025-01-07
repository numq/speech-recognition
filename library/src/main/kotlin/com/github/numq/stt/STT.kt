package com.github.numq.stt

interface STT : AutoCloseable {
    fun recognize(pcmBytes: ByteArray): Result<String>

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

        fun create(modelPath: String): Result<STT> = runCatching {
            check(isLoaded) { "Native binaries were not loaded" }

            DefaultSTT(nativeSTT = NativeSTT(modelPath = modelPath))
        }
    }
}