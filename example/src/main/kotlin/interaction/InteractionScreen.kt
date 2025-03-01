package interaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import capturing.CapturingService
import com.github.numq.speechrecognition.SpeechRecognition
import com.github.numq.voiceactivitydetection.VoiceActivityDetection
import device.Device
import device.DeviceService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import selector.SpeechRecognitionItemSelector
import java.io.ByteArrayOutputStream
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun InteractionScreen(
    deviceService: DeviceService,
    capturingService: CapturingService,
    vad: VoiceActivityDetection.Silero,
    silero: SpeechRecognition.Silero,
    whisper: SpeechRecognition.Whisper,
    handleThrowable: (Throwable) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope { Dispatchers.Default }

    var deviceJob by remember { mutableStateOf<Job?>(null) }

    var capturingJob by remember { mutableStateOf<Job?>(null) }

    var selectedSpeechRecognitionItem by remember { mutableStateOf(SpeechRecognitionItem.SILERO) }

    val capturingDevices = remember { mutableStateListOf<Device>() }

    var selectedCapturingDevice by remember { mutableStateOf<Device?>(null) }

    var refreshRequested by remember { mutableStateOf(true) }

    val recognizedChunks = remember { mutableStateListOf<String>() }

    val listState = rememberLazyListState()

    LaunchedEffect(recognizedChunks.size) {
        listState.animateScrollToItem(listState.layoutInfo.totalItemsCount)
    }

    LaunchedEffect(refreshRequested) {
        deviceJob?.cancel()
        deviceJob = null

        if (refreshRequested) {
            deviceJob = coroutineScope.launch {
                deviceService.listCapturingDevices().onSuccess { devices ->
                    if (devices != capturingDevices) {
                        capturingDevices.clear()
                        capturingDevices.addAll(devices)

                        if (selectedCapturingDevice !in capturingDevices) {
                            selectedCapturingDevice = null
                        }
                    }
                }.onFailure(handleThrowable)

                refreshRequested = false
            }
        }
    }

    LaunchedEffect(selectedCapturingDevice, selectedSpeechRecognitionItem) {
        capturingJob?.cancelAndJoin()
        capturingJob = null

        capturingJob = when (val device = selectedCapturingDevice) {
            null -> return@LaunchedEffect

            else -> coroutineScope.launch {
                val sampleRate = device.sampleRate

                val channels = device.channels

                val chunkSize = vad.inputSizeForMillis(
                    sampleRate = sampleRate, channels = channels, millis = 1_000L
                ).getOrThrow()

                val speechRecognition = when (selectedSpeechRecognitionItem) {
                    SpeechRecognitionItem.WHISPER -> whisper

                    SpeechRecognitionItem.SILERO -> silero
                }

                val minInputSize = speechRecognition.minimumInputSize(
                    sampleRate = sampleRate,
                    channels = channels
                ).getOrThrow()

                var i = 0

                ByteArrayOutputStream().use { baos ->
                    capturingService.capture(device, chunkSize).catch {
                        if (it != CancellationException()) handleThrowable(it)
                    }.map { pcmBytes ->
                        vad.detect(pcmBytes, sampleRate, channels).map { (fragments, isLastFragmentComplete) ->
                            var incompleteFragment = byteArrayOf()

                            if (isLastFragmentComplete) {
                                fragments.forEach(baos::write)
                            } else fragments.forEachIndexed { index, pcmBytes ->
                                if (index == fragments.lastIndex) incompleteFragment =
                                    pcmBytes else baos.write(pcmBytes)
                            }

                            incompleteFragment
                        }.getOrThrow()
                    }.onEach { incompleteFragment ->
                        if (baos.size() > minInputSize) {
                            speechRecognition.recognize(
                                pcmBytes = baos.toByteArray(),
                                sampleRate = sampleRate,
                                channels = channels
                            ).onSuccess { text ->
                                if (text.isNotBlank()) {
                                    recognizedChunks.add(text.lowercase().replace("[^\\w\\s]".toRegex(), ""))
                                }
                            }.getOrThrow()

                            baos.reset()
                        }

                        baos.write(incompleteFragment)
                    }.flowOn(Dispatchers.IO).collect()
                }
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Card(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpeechRecognitionItemSelector(
                        modifier = Modifier.fillMaxWidth(),
                        selectedSpeechRecognitionItem = selectedSpeechRecognitionItem
                    ) { speechRecognitionItem ->
                        selectedSpeechRecognitionItem = speechRecognitionItem

                        if (selectedSpeechRecognitionItem == SpeechRecognitionItem.SILERO) {
                            silero.reset().onFailure(handleThrowable)
                        }
                    }

                    Divider(modifier = Modifier.fillMaxWidth())

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Capturing devices", modifier = Modifier.padding(8.dp))
                        when (refreshRequested) {
                            true -> IconButton(onClick = {
                                refreshRequested = false
                            }) {
                                Icon(Icons.Default.Cancel, null)
                            }

                            false -> IconButton(onClick = {
                                refreshRequested = true
                            }) {
                                Icon(Icons.Default.Refresh, null)
                            }
                        }
                    }
                    when {
                        refreshRequested -> Box(
                            modifier = Modifier.weight(1f), contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }

                        else -> LazyColumn(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.Top),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(capturingDevices, key = { it.name }) { device ->
                                Card(modifier = Modifier.fillMaxWidth()
                                    .alpha(alpha = if (device == selectedCapturingDevice) .5f else 1f).clickable {
                                        selectedCapturingDevice = device.takeIf { it != selectedCapturingDevice }
                                    }) {
                                    Text(device.name, modifier = Modifier.padding(8.dp))
                                }
                            }
                        }
                    }
                }
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                state = listState
            ) {
                items(recognizedChunks) { recognizedChunk ->
                    Text(text = recognizedChunk)
                }
            }
            Button(onClick = {
                recognizedChunks.clear()
            }, enabled = recognizedChunks.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                        Text("Clear", modifier = Modifier.padding(8.dp))
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        Icon(Icons.Default.Clear, null)
                    }
                }
            }
        }
    }
}