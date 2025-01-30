import com.github.numq.stt.SpeechToText
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WhisperCpuTest {
    companion object {
        lateinit var speechToText: SpeechToText

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            val pathToBinaries = this::class.java.getResource("bin/cpu")?.path!!

            SpeechToText.Whisper.loadCPU(
                ggmlbase = "$pathToBinaries/ggml-base.dll",
                ggmlcpu = "$pathToBinaries/ggml-cpu.dll",
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
    fun `should recognize short speech with CPU`() = runTest {
        SpeechToTextTest.testSpeechRecognition(speechToText, "audio/short.wav", "Test audio")
    }

    @Test
    fun `should recognize long speech with CPU`() = runTest {
        SpeechToTextTest.testSpeechRecognition(
            speechToText,
            "audio/long.wav",
            "This is a very long audio file for testing"
        )
    }
}