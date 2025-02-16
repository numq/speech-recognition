cmake_minimum_required(VERSION 3.24)
project(speech-recognition VERSION 1.0.0)

set(CMAKE_CXX_STANDARD 23)

set(CMAKE_SHARED_LIBRARY_PREFIX "")

option(BUILD_WITH_CUDA "Build with CUDA support" OFF)

# WHISPER

add_library(speech-recognition-whisper SHARED
        src/whisper/whisper.cpp
        src/Java_com_github_numq_speechrecognition_whisper_NativeWhisperSpeechRecognition.cpp
)

find_package(JNI REQUIRED)

if (JNI_FOUND)
    message(STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message(STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
    target_include_directories(speech-recognition-whisper PRIVATE ${JNI_INCLUDE_DIRS})
else ()
    message(FATAL_ERROR "JNI not found.")
endif ()

target_include_directories(speech-recognition-whisper PRIVATE include include/ggml include/whisper)

if (BUILD_WITH_CUDA)
    target_link_directories(speech-recognition-whisper PRIVATE bin/cuda)

    target_link_libraries(speech-recognition-whisper PRIVATE ggml-base ggml-cpu ggml-cuda ggml)
else ()
    target_link_directories(speech-recognition-whisper PRIVATE bin/cpu)

    target_link_libraries(speech-recognition-whisper PRIVATE ggml-base ggml-cpu ggml)
endif ()