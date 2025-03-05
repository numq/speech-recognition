import com.github.numq.speechrecognition.SpeechRecognition
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SpeechRecognitionWhisperTest {
    companion object {
        private val modelPath = this::class.java.getResource("model/ggml-tiny.bin")?.path?.trimStart('/')!!

        private val pathToCpuBinaries = this::class.java.getResource("bin/cpu")?.path!!

        private val pathToCudaBinaries = this::class.java.getResource("bin/cuda")?.path!!
    }

    private suspend fun testSpeechRecognition(
        speechRecognition: SpeechRecognition,
        audioFile: String,
        expectedText: String,
    ) {
        val pcmBytes = javaClass.classLoader.getResource(audioFile)!!.readBytes()
        val sampleRate = 22_050
        val channels = 1

        assertEquals(
            expectedText.lowercase().replace(" ", ""),
            speechRecognition.recognize(pcmBytes, channels, sampleRate).getOrThrow().lowercase().filter(Char::isLetter)
        )
    }

    @Test
    fun `should recognize speech with CPU`() = runTest {
        SpeechRecognition.Whisper.loadCPU(
            ggmlBase = "$pathToCpuBinaries/ggml-base.dll",
            ggmlCpu = "$pathToCpuBinaries/ggml-cpu.dll",
            ggml = "$pathToCpuBinaries/ggml.dll",
            speechRecognitionWhisper = "$pathToCpuBinaries/speech-recognition-whisper.dll"
        ).getOrThrow()

        SpeechRecognition.Whisper.create(modelPath).getOrThrow().use { speechRecognition ->
            testSpeechRecognition(speechRecognition, "audio/short.wav", "Test audio")

            testSpeechRecognition(
                speechRecognition, "audio/long.wav", "This is a very long audio file for testing"
            )
        }
    }

    @Test
    fun `should recognize speech with CUDA`() = runTest {
        SpeechRecognition.Whisper.loadCUDA(
            ggmlBase = "$pathToCudaBinaries/ggml-base.dll",
            ggmlCpu = "$pathToCudaBinaries/ggml-cpu.dll",
            ggmlCuda = "$pathToCudaBinaries/ggml-cuda.dll",
            ggml = "$pathToCudaBinaries/ggml.dll",
            speechRecognitionWhisper = "$pathToCudaBinaries/speech-recognition-whisper.dll"
        ).getOrThrow()

        SpeechRecognition.Whisper.create(modelPath).getOrThrow().use { speechRecognition ->
            testSpeechRecognition(speechRecognition, "audio/short.wav", "Test audio")

            testSpeechRecognition(
                speechRecognition, "audio/long.wav", "This is a very long audio file for testing"
            )
        }
    }
}
