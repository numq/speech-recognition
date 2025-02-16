# Speech recognition

JVM library for speech recognition, written in Kotlin and based on the C++
library [whisper.cpp](https://github.com/ggerganov/whisper.cpp)

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

- Unzip binaries

## Usage

### TL;DR

> See the [example](example) module for implementation details

- Call `recognize` to process the input data and get recognized string

### Step-by-step

- Load binaries
    - CPU
       ```kotlin
       SpeechToText.Whisper.loadCPU(
        ggmlBase = "/path/to/ggml-base", 
        ggmlCpu = "/path/to/ggml-cpu",
        ggml = "/path/to/ggml",
        speechRecognitionWhisper = "/path/to/speech-recognition-whisper",
      )
       ```
    - CUDA
       ```kotlin
       SpeechToText.Whisper.loadCUDA(
        ggmlBase = "/path/to/ggml-base", 
        ggmlCpu = "/path/to/ggml-cpu",
        ggmlCuda = "/path/to/ggml-cuda",
        ggml = "/path/to/ggml",
        speechRecognitionWhisper = "/path/to/speech-recognition-whisper",
      )
       ```

- Create an instance

  ```kotlin
  SpeechToText.Whisper.create()
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
