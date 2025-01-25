import com.github.numq.stt.SpeechToText
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertEquals

class SpeechToTextTest {
    companion object {
        private val modelPath = this::class.java.getResource("model/ggml-tiny.bin")?.path?.trimStart('/')!!

        private val stt by lazy { SpeechToText.Whisper.create(modelPath).getOrThrow() }

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            val pathToBinaries = this::class.java.getResource("bin")?.path

            SpeechToText.Whisper.load(
                ggmlbase = "$pathToBinaries\\ggml-base.dll",
                ggmlcpu = "$pathToBinaries\\ggml-cpu.dll",
                ggmlcuda = "$pathToBinaries\\ggml-cuda.dll",
                ggml = "$pathToBinaries\\ggml.dll",
                whisper = "$pathToBinaries\\whisper.dll",
                libstt = "$pathToBinaries\\libstt.dll"
            ).getOrThrow()
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            stt.close()
        }
    }

    @Test
    fun `should recognize short speech and return a string`() = runTest {
        val pcmBytes = javaClass.classLoader.getResource("audio/short.wav")!!.readBytes()
        val sampleRate = 48_000
        val channels = 1

        val expectedString = "Test audio".lowercase().replace(" ", "")

        assertEquals(
            expectedString,
            stt.recognize(pcmBytes = pcmBytes, sampleRate = sampleRate, channels = channels).getOrThrow().lowercase()
                .filter(Char::isLetter)
        )
    }

    @Test
    fun `should recognize long speech and return a string`() = runTest {
        val pcmBytes = javaClass.classLoader.getResource("audio/long.wav")!!.readBytes()
        val sampleRate = 48_000
        val channels = 1

        val expectedString = "This is a very long audio file for testing".lowercase().replace(" ", "")

        assertEquals(
            expectedString,
            stt.recognize(pcmBytes = pcmBytes, sampleRate = sampleRate, channels = channels).getOrThrow().lowercase()
                .filter(Char::isLetter)
        )
    }
}