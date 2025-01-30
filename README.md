# Speech-to-Text

JVM library for speech-to-text recognition, written in Kotlin and based on the C++
library [whisper.cpp](https://github.com/ggerganov/whisper.cpp)

## Features

- Recognizes speech in PCM audio data and returns a string with the result
- Supports any sampling rate and number of channels due to resampling and downmixing

## Installation

- Download latest [release](https://github.com/numq/speech-to-text/releases)

- Add library dependency
   ```kotlin
   dependencies {
        implementation(file("/path/to/jar"))
   }
   ```

- Unzip binaries

## Usage

> See the [example](example) module for implementation details

- Load binaries
  - CPU
     ```kotlin
     SpeechToText.Whisper.loadCPU(
      ggmlbase = "/path/to/ggmlbase", 
      ggmlcpu = "/path/to/ggmlcpu",
      ggml = "/path/to/ggml",
      whisper = "/path/to/whisper",
      libstt = "/path/to/libstt",
    )
     ```
  - CUDA
     ```kotlin
     SpeechToText.Whisper.loadCUDA(
      ggmlbase = "/path/to/ggmlbase", 
      ggmlcpu = "/path/to/ggmlcpu",
      ggmlcuda = "/path/to/ggmlcuda",
      ggml = "/path/to/ggml",
      whisper = "/path/to/whisper",
      libstt = "/path/to/libstt",
    )
     ```

- Create an instance

  ```kotlin
  SpeechToText.Whisper.create()
  ```


- Call `recognize` passing the input data, sample rate, and number of channels as arguments


- Call `reset` to reset the internal state - for example when the audio source changes


- Call `close` to release resources

## Requirements

- JVM version 9 or higher

## License

This project is licensed under the [Apache License 2.0](LICENSE)

## Acknowledgments

- [whisper.cpp](https://github.com/ggerganov/whisper.cpp)
