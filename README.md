# Speech recognition

JVM library for speech recognition, written in Kotlin and based on the C++
library [whisper.cpp](https://github.com/ggerganov/whisper.cpp) and ML
model [Silero](https://github.com/snakers4/silero-models)

### See also

- [Stretch](https://github.com/numq/stretch) *to change the speed of audio without changing the pitch*


- [Voice Activity Detection](https://github.com/numq/voice-activity-detection) *to extract speech from audio*


- [Speech generation](https://github.com/numq/speech-generation) *to generate voice audio from text*


- [Text generation](https://github.com/numq/text-generation) *to generate text from prompt*


- [Noise reduction](https://github.com/numq/noise-reduction) *to remove noise from audio*

## Features

- Recognizes speech in PCM audio data and returns a string with the result
- Supports any sampling rate and number of channels due to resampling and downmixing

## Installation

- Download latest [release](https://github.com/numq/speech-recognition/releases)

- Add library dependency
   ```kotlin
   dependencies {
        implementation(file("/path/to/jar"))
   }
   ```

### whisper.cpp

- Unzip binaries

### Silero

- Add ONNX dependency
   ```kotlin
   dependencies {
        implementation("com.microsoft.onnxruntime:onnxruntime:1.20.0")
   }
   ```

## Usage

### TL;DR

> See the [example](example) module for implementation details

- Call `recognize` to process the input data and get recognized string

### Step-by-step

- Load binaries if you are going to use whisper.cpp
    - CPU
       ```kotlin
       SpeechRecognition.Whisper.loadCPU(
        ggmlBase = "/path/to/ggml-base", 
        ggmlCpu = "/path/to/ggml-cpu",
        ggml = "/path/to/ggml",
        speechRecognitionWhisper = "/path/to/speech-recognition-whisper",
      )
       ```
    - CUDA
       ```kotlin
       SpeechRecognition.Whisper.loadCUDA(
        ggmlBase = "/path/to/ggml-base", 
        ggmlCpu = "/path/to/ggml-cpu",
        ggmlCuda = "/path/to/ggml-cuda",
        ggml = "/path/to/ggml",
        speechRecognitionWhisper = "/path/to/speech-recognition-whisper",
      )
       ```

- Create an instance

    ### whisper.cpp

     ```kotlin
     SpeechRecognition.Whisper.create(modelPath = "/path/to/model")
     ```
  
    ### Silero

    ```kotlin
    SpeechRecognition.Silero.create(modelPath = "/path/to/model")
    ```

- Call `minimumInputSize` to get the audio producer buffer size for real-time detection


- Call `adjustTemperature` to adjust the temperature parameter


- Call `recognize` passing the input data, sample rate, and number of channels as arguments


- Call `reset` to reset the internal state - for example when the audio source changes


- Call `close` to release resources

## Requirements

- JVM version 9 or higher

## License

This project is licensed under the [Apache License 2.0](LICENSE)

## Acknowledgments

- [whisper.cpp](https://github.com/ggerganov/whisper.cpp)
- [Silero](https://github.com/snakers4/silero-models)
