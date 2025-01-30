import com.github.numq.stt.SpeechToText
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SpeechToTextWhisperTest {
    companion object {
        private val modelPath = this::class.java.getResource("model/ggml-tiny.bin")?.path?.trimStart('/')!!

        private val pathToCpuBinaries = this::class.java.getResource("bin/cpu")?.path!!

        private val pathToCudaBinaries = this::class.java.getResource("bin/cuda")?.path!!
    }

    private fun testSpeechRecognition(speechToText: SpeechToText, audioFile: String, expectedText: String) {
        val pcmBytes = javaClass.classLoader.getResource(audioFile)!!.readBytes()
        val sampleRate = 48_000
        val channels = 1

        assertEquals(
            expectedText.lowercase().replace(" ", ""),
            speechToText.recognize(pcmBytes, sampleRate, channels).getOrThrow().lowercase().filter(Char::isLetter)
        )
    }

    @Test
    fun `should recognize speech with CPU`() = runTest {
        SpeechToText.Whisper.loadCPU(
            ggmlbase = "$pathToCpuBinaries/ggml-base.dll",
            ggmlcpu = "$pathToCpuBinaries/ggml-cpu.dll",
            ggml = "$pathToCpuBinaries/ggml.dll",
            whisper = "$pathToCpuBinaries/whisper.dll",
            libstt = "$pathToCpuBinaries/libstt.dll"
        ).getOrThrow()

        SpeechToText.Whisper.create(modelPath).getOrThrow().use { speechToText ->
            testSpeechRecognition(speechToText, "audio/short.wav", "Test audio")

            testSpeechRecognition(
                speechToText, "audio/long.wav", "This is a very long audio file for testing"
            )
        }
    }

    @Test
    fun `should recognize speech with CUDA`() = runTest {
        SpeechToText.Whisper.loadCUDA(
            ggmlbase = "$pathToCudaBinaries/ggml-base.dll",
            ggmlcpu = "$pathToCudaBinaries/ggml-cpu.dll",
            ggmlcuda = "$pathToCudaBinaries/ggml-cuda.dll",
            ggml = "$pathToCudaBinaries/ggml.dll",
            whisper = "$pathToCudaBinaries/whisper.dll",
            libstt = "$pathToCudaBinaries/libstt.dll"
        ).getOrThrow()

        SpeechToText.Whisper.create(modelPath).getOrThrow().use { speechToText ->
            testSpeechRecognition(speechToText, "audio/short.wav", "Test audio")

            testSpeechRecognition(
                speechToText, "audio/long.wav", "This is a very long audio file for testing"
            )
        }
    }
}
