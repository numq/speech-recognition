import com.github.numq.stt.SpeechToText
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WhisperCudaTest {
    companion object {
        lateinit var speechToText: SpeechToText

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            val pathToBinaries = this::class.java.getResource("bin/cuda")?.path!!

            SpeechToText.Whisper.loadCUDA(
                ggmlbase = "$pathToBinaries/ggml-base.dll",
                ggmlcpu = "$pathToBinaries/ggml-cpu.dll",
                ggmlcuda = "$pathToBinaries/ggml-cuda.dll",
                ggml = "$pathToBinaries/ggml.dll",
                whisper = "$pathToBinaries/whisper.dll",
                libstt = "$pathToBinaries/libstt.dll"
            ).getOrThrow()

            val modelPath = this::class.java.getResource("model/ggml-tiny.bin")?.path?.trimStart('/')!!
            speechToText = SpeechToText.Whisper.create(modelPath).getOrThrow()
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            speechToText.close()
        }
    }

    @Test
    fun `should recognize short speech with CUDA`() = runTest {
        SpeechToTextTest.testSpeechRecognition(speechToText, "audio/short.wav", "Test audio")
    }

    @Test
    fun `should recognize long speech with CUDA`() = runTest {
        SpeechToTextTest.testSpeechRecognition(
            speechToText,
            "audio/long.wav",
            "This is a very long audio file for testing"
        )
    }
}