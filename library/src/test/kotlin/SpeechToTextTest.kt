import com.github.numq.stt.SpeechToText
import org.junit.jupiter.api.Assertions.assertEquals

object SpeechToTextTest {
    fun testSpeechRecognition(speechToText: SpeechToText, audioFile: String, expectedText: String) {
        val pcmBytes = javaClass.classLoader.getResource(audioFile)!!.readBytes()
        val sampleRate = 48_000
        val channels = 1

        assertEquals(
            expectedText.lowercase().replace(" ", ""),
            speechToText.recognize(pcmBytes, sampleRate, channels).getOrThrow().lowercase().filter(Char::isLetter)
        )
    }
}
