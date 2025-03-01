import com.github.numq.speechrecognition.SpeechRecognition
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SpeechRecognitionSileroTest {
    companion object {
        private val modelPath = this::class.java.getResource("model/silero_stt_en.onnx")?.path?.trimStart('/')!!
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
            speechRecognition.recognize(pcmBytes, sampleRate, channels).getOrThrow().lowercase().filter(Char::isLetter)
        )
    }

    @Test
    fun `should recognize speech`() = runTest {
        SpeechRecognition.Silero.create(modelPath).getOrThrow().use { speechRecognition ->
            testSpeechRecognition(speechRecognition, "audio/short.wav", "Test audio")

            testSpeechRecognition(
                speechRecognition, "audio/long.wav", "This is a very long audio file for testing"
            )
        }
    }
}
