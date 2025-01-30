package com.github.numq.stt

import com.github.numq.stt.whisper.NativeWhisperSpeechToText
import com.github.numq.stt.whisper.WhisperSpeechToText

interface SpeechToText : AutoCloseable {
    companion object {
        const val SAMPLE_RATE = 24_000
    }

    /**
     * Recognizes text from the given PCM audio data.
     *
     * The input data is processed to generate a textual transcription.
     *
     * @param pcmBytes the audio data in PCM format.
     * @param sampleRate the sampling rate of the audio data in Hz.
     * @param channels the number of audio channels (e.g., 1 for mono, 2 for stereo).
     * @return a [Result] containing the recognized text if successful.
     */
    fun recognize(pcmBytes: ByteArray, sampleRate: Int, channels: Int): Result<String>

    interface Whisper : SpeechToText {
        companion object {
            private sealed interface LoadState {
                data object Unloaded : LoadState

                data object CPU : LoadState

                data object CUDA : LoadState
            }

            @Volatile
            private var loadState: LoadState = LoadState.Unloaded

            /**
             * Loads the CPU-based native libraries required for Whisper speech-to-text processing.
             *
             * This method must be called before creating a Whisper instance.
             *
             * @param ggmlbase the path to the ggml-base library.
             * @param ggmlcpu the path to the ggml-cpu library.
             * @param ggml the path to the ggml library.
             * @param whisper the path to the whisper library.
             * @param libstt the path to the libstt library.
             * @return a [Result] indicating the success or failure of the operation.
             */
            fun loadCPU(
                ggmlbase: String,
                ggmlcpu: String,
                ggml: String,
                whisper: String,
                libstt: String,
            ) = runCatching {
                check(loadState is LoadState.Unloaded) { "Native binaries have already been loaded as ${loadState::class.simpleName}" }

                System.load(ggmlbase)
                System.load(ggmlcpu)
                System.load(ggml)
                System.load(whisper)
                System.load(libstt)

                loadState = LoadState.CPU
            }

            /**
             * Loads the CUDA-based native libraries required for Whisper speech-to-text processing.
             *
             * This method must be called before creating a Whisper instance.
             *
             * @param ggmlbase the path to the ggml-base library.
             * @param ggmlcpu the path to the ggml-cpu library.
             * @param ggmlcuda the path to the ggml-cuda library.
             * @param ggml the path to the ggml library.
             * @param whisper the path to the whisper library.
             * @param libstt the path to the libstt library.
             * @return a [Result] indicating the success or failure of the operation.
             */
            fun loadCUDA(
                ggmlbase: String,
                ggmlcpu: String,
                ggmlcuda: String,
                ggml: String,
                whisper: String,
                libstt: String,
            ) = runCatching {
                check(loadState is LoadState.Unloaded) { "Native binaries have already been loaded as ${loadState::class.simpleName}" }

                System.load(ggmlbase)
                System.load(ggmlcpu)
                System.load(ggmlcuda)
                System.load(ggml)
                System.load(whisper)
                System.load(libstt)

                loadState = LoadState.CUDA
            }

            /**
             * Creates a new instance of [SpeechToText] using the Whisper implementation.
             *
             * This method initializes the Whisper speech-to-text system with the specified model.
             *
             * @param modelPath the path to the Whisper model file.
             * @return a [Result] containing the created instance if successful.
             * @throws IllegalStateException if the native libraries are not loaded or if there is an issue with the underlying native libraries.
             */
            fun create(modelPath: String): Result<Whisper> = runCatching {
                check(loadState !is LoadState.Unloaded) { "Native binaries were not loaded" }

                WhisperSpeechToText(nativeWhisperSpeechToText = NativeWhisperSpeechToText(modelPath = modelPath))
            }
        }

        override fun close() {
            loadState = LoadState.Unloaded
        }
    }
}