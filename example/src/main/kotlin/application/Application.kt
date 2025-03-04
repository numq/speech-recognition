package application

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import capturing.CapturingService
import com.github.numq.speechrecognition.SpeechRecognition
import com.github.numq.voiceactivitydetection.VoiceActivityDetection
import device.DeviceService
import interaction.InteractionScreen

const val APP_NAME = "Speech recognition"

fun main(args: Array<String>) {
    val (sileroModelPath, whisperModelPath) = args

    val pathToBinariesSpeechRecognition = Thread.currentThread().contextClassLoader.getResource("bin/stt")?.file

    checkNotNull(pathToBinariesSpeechRecognition) { "Speech recognition binaries not found" }

    SpeechRecognition.Whisper.loadCUDA(
        ggmlBase = "$pathToBinariesSpeechRecognition\\ggml-base.dll",
        ggmlCpu = "$pathToBinariesSpeechRecognition\\ggml-cpu.dll",
        ggmlCuda = "$pathToBinariesSpeechRecognition\\ggml-cuda.dll",
        ggml = "$pathToBinariesSpeechRecognition\\ggml.dll",
        speechRecognitionWhisper = "$pathToBinariesSpeechRecognition\\speech-recognition-whisper.dll"
    ).getOrThrow()

    singleWindowApplication(state = WindowState(width = 512.dp, height = 512.dp), title = APP_NAME) {
        val deviceService = remember { DeviceService.create().getOrThrow() }

        val capturingService = remember { CapturingService.create().getOrThrow() }

        val vad = remember { VoiceActivityDetection.Silero.create().getOrThrow() }

        val silero = remember { SpeechRecognition.Silero.create(modelPath = sileroModelPath).getOrThrow() }

        val whisper = remember { SpeechRecognition.Whisper.create(modelPath = whisperModelPath).getOrThrow() }

        val (throwable, setThrowable) = remember { mutableStateOf<Throwable?>(null) }

        DisposableEffect(Unit) {
            onDispose {
                vad.close()
                silero.close()
                whisper.close()
            }
        }

        MaterialTheme {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                InteractionScreen(
                    deviceService = deviceService,
                    capturingService = capturingService,
                    vad = vad,
                    silero = silero,
                    whisper = whisper,
                    handleThrowable = setThrowable
                )
                throwable?.let { t ->
                    Snackbar(
                        modifier = Modifier.padding(8.dp),
                        action = {
                            Button(onClick = { setThrowable(null) }) { Text("Dismiss") }
                        }
                    ) { Text(t.localizedMessage) }
                }
            }
        }
    }
}