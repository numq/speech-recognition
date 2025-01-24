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
    fun `should recognize speech and return a string`() = runTest {
        val pcmBytes = javaClass.classLoader.getResource("audio/test.wav")!!.readBytes()

        assertEquals(
            "testaudio",
            stt.recognize(pcmBytes).getOrThrow().lowercase().filter(Char::isLetter)
        )
    }
}