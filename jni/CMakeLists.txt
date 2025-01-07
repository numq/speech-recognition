cmake_minimum_required(VERSION 3.24)
project(stt VERSION 1.0.0)

set(CMAKE_CXX_STANDARD 23)

add_library(stt SHARED src/Java_com_github_numq_stt_NativeSTT.cpp)

find_package(JNI)

if (JNI_FOUND)
    message(STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message(STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
    target_include_directories(stt PRIVATE ${JNI_INCLUDE_DIRS})
else ()
    message(FATAL_ERROR "JNI not found.")
endif ()

target_include_directories(stt PRIVATE include)

target_link_directories(stt PRIVATE bin)

target_link_libraries(stt PRIVATE ggml-base ggml-cpu ggml-cuda ggml whisper)