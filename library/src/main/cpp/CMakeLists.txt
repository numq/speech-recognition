cmake_minimum_required(VERSION 3.24)
project(stt VERSION 1.0.0)

set(CMAKE_CXX_STANDARD 23)

option(BUILD_WITH_CUDA "Build with CUDA support" OFF)

add_library(stt SHARED src/Java_com_github_numq_stt_whisper_NativeWhisperSpeechToText.cpp)

find_package(JNI REQUIRED)

if (JNI_FOUND)
    message(STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message(STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
    target_include_directories(stt PRIVATE ${JNI_INCLUDE_DIRS})
else ()
    message(FATAL_ERROR "JNI not found.")
endif ()

target_include_directories(stt PRIVATE include include/ggml)

if (BUILD_WITH_CUDA)
    target_link_directories(stt PRIVATE bin/cuda)

    target_link_libraries(stt PRIVATE ggml-base ggml-cpu ggml-cuda ggml whisper)
else ()
    target_link_directories(stt PRIVATE bin/cpu)

    target_link_libraries(stt PRIVATE ggml-base ggml-cpu ggml whisper)
endif ()