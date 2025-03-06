package com.github.numq.speechrecognition

import com.github.numq.speechrecognition.silero.SileroSpeechRecognition
import com.github.numq.speechrecognition.silero.decoder.Decoder
import com.github.numq.speechrecognition.silero.model.SileroModel
import com.github.numq.speechrecognition.whisper.NativeWhisperSpeechRecognition
import com.github.numq.speechrecognition.whisper.WhisperSpeechRecognition
import java.io.File

interface SpeechRecognition : AutoCloseable {
    /**
     * The sample rate of the audio data in Hertz (Hz).
     */
    val sampleRate: Int

    /**
     * The number of audio channels.
     */
    val channels: Int

    /**
     * Returns the minimum input size.
     *
     * @param sampleRate the sampling rate of the audio data in Hz.
     * @param channels the number of audio channels.
     * @return a [Result] containing the minimum input size in bytes.
     */
    fun minimumInputSize(sampleRate: Int, channels: Int): Result<Int>

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
    suspend fun recognize(pcmBytes: ByteArray, sampleRate: Int, channels: Int): Result<String>

    interface Silero : SpeechRecognition {
        companion object {
            /**
             * Creates a new instance of [SpeechRecognition] using the Whisper implementation.
             *
             * This method initializes the Whisper speech recognition with the specified model.
             *
             * @param modelPath the path to the Whisper model file.
             * @return a [Result] containing the created instance if successful.
             * @throws IllegalStateException if the native libraries are not loaded or if there is an issue with the underlying native libraries.
             */
            fun create(modelPath: String): Result<Silero> = runCatching {
                val language = modelPath.split(".").first().split("_").last()

                check(language.isNotBlank()) { "Not found language" }

                val resourceStream = Companion::class.java.classLoader.getResourceAsStream("labels/$language.json")
                    ?: throw IllegalStateException("Labels '$language.json' not found in resources")

                val tempFile = File.createTempFile("$language-$language", ".json").apply {
                    deleteOnExit()
                    outputStream().use { resourceStream.copyTo(it) }
                }

                SileroSpeechRecognition(
                    model = SileroModel.create(modelPath = modelPath),
                    decoder = Decoder.create(
                        labels = tempFile.readText().trim().removePrefix("[").removeSuffix("]").split(",").map {
                            it.trim().removeSurrounding("\"")
                        }
                    )
                )
            }
        }

        /**
         * Resets the speech recognition internal state.
         *
         * @return a [Result] indicating the success or failure of the operation.
         */
        fun reset(): Result<Unit>
    }

    interface Whisper : SpeechRecognition {
        /**
         * The currently configured language for speech recognition. Default is "auto".
         *
         * @return The language code (e.g., "auto" for automatic detection, "en" for English, etc.) used for transcription.
         */
        val language: String

        /**
         * A flag indicating whether translation is enabled during speech recognition.
         *
         * @return `true` if translation is enabled, `false` otherwise.
         */
        val translationFlag: Boolean

        /**
         * Sets the language for speech recognition.
         *
         * @param language the language code to use for transcription.
         * @return a [Result] indicating success or failure of the operation..
         */
        suspend fun setLanguage(language: String): Result<Unit>

        /**
         * Enables or disables the translation feature during speech recognition.
         *
         * @param translationFlag `true` to enable translation, `false` to disable it.
         * @return a [Result] indicating success or failure of the operation.
         */
        suspend fun setTranslationFlag(translationFlag: Boolean): Result<Unit>

        companion object {
            private sealed interface LoadState {
                data object Unloaded : LoadState

                data object CPU : LoadState

                data object CUDA : LoadState
            }

            @Volatile
            private var loadState: LoadState = LoadState.Unloaded

            /**
             * Loads the CPU-based native libraries required for Whisper speech recognition.
             *
             * This method must be called before creating a Whisper instance.
             *
             * @param ggmlBase The path to the `ggml-base` binary.
             * @param ggmlCpu The path to the `ggml-cpu` binary.
             * @param ggml The path to the `ggml` binary.
             * @param speechRecognitionWhisper The path to the `speech-recognition-whisper` binary.
             * @return A [Result] indicating the success or failure of the operation.
             */
            fun loadCPU(
                ggmlBase: String,
                ggmlCpu: String,
                ggml: String,
                speechRecognitionWhisper: String,
            ) = runCatching {
                check(loadState is LoadState.Unloaded) { "Native binaries have already been loaded as ${loadState::class.simpleName}" }

                System.load(ggmlBase)
                System.load(ggmlCpu)
                System.load(ggml)
                System.load(speechRecognitionWhisper)

                loadState = LoadState.CPU
            }

            /**
             * Loads the CUDA-based native libraries required for Whisper speech recognition.
             *
             * This method must be called before creating a Whisper instance.
             *
             * @param ggmlBase The path to the `ggml-base` binary.
             * @param ggmlCpu The path to the `ggml-cpu` binary.
             * @param ggmlCuda The path to the `ggml-cuda` binary.
             * @param ggml The path to the `ggml` binary.
             * @param speechRecognitionWhisper The path to the `speech-recognition-whisper` binary.
             * @return A [Result] indicating the success or failure of the operation.
             */
            fun loadCUDA(
                ggmlBase: String,
                ggmlCpu: String,
                ggmlCuda: String,
                ggml: String,
                speechRecognitionWhisper: String,
            ) = runCatching {
                check(loadState is LoadState.Unloaded) { "Native binaries have already been loaded as ${loadState::class.simpleName}" }

                System.load(ggmlBase)
                System.load(ggmlCpu)
                System.load(ggmlCuda)
                System.load(ggml)
                System.load(speechRecognitionWhisper)

                loadState = LoadState.CUDA
            }

            /**
             * Creates a new instance of [SpeechRecognition] using the Whisper implementation.
             *
             * This method initializes the Whisper speech recognition with the specified model.
             *
             * @param modelPath the path to the Whisper model file.
             * @return a [Result] containing the created instance if successful.
             * @throws IllegalStateException if the native libraries are not loaded or if there is an issue with the underlying native libraries.
             */
            fun create(modelPath: String): Result<Whisper> = runCatching {
                check(loadState !is LoadState.Unloaded) { "Native binaries were not loaded" }

                WhisperSpeechRecognition(nativeWhisperSpeechRecognition = NativeWhisperSpeechRecognition(modelPath = modelPath))
            }
        }

        /**
         * Adjusts the temperature parameter for speech recognition.
         *
         * The temperature parameter controls the randomness of the model's predictions.
         * A lower temperature results in more deterministic outputs, while a higher
         * temperature introduces more variability in the recognition results.
         *
         * @param temperature the new temperature value to set. Typically ranges from 0.0 (most deterministic)
         * to 1.0 (most random).
         * @return a [Result] indicating success or failure of the operation.
         */
        suspend fun adjustTemperature(temperature: Float): Result<Unit>

        override fun close() {
            loadState = LoadState.Unloaded
        }
    }
}