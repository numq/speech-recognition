package com.github.numq.speechrecognition.whisper

import com.github.numq.speechrecognition.exception.NativeException
import java.lang.ref.Cleaner

internal class NativeWhisperSpeechRecognition(modelPath: String) : AutoCloseable {
    private val nativeHandle = initNative(modelPath = modelPath).also { handle ->
        require(handle != -1L) { "Unable to initialize native library" }
    }

    private val cleanable = cleaner.register(this) { freeNative(nativeHandle) }

    private companion object {
        val cleaner: Cleaner = Cleaner.create()

        @JvmStatic
        external fun initNative(modelPath: String): Long

        @JvmStatic
        external fun recognizeNative(handle: Long, pcmBytes: ByteArray, temperature: Float): String

        @JvmStatic
        external fun freeNative(handle: Long)
    }

    fun recognize(pcmBytes: ByteArray, temperature: Float): String {
        try {
            return recognizeNative(handle = nativeHandle, pcmBytes = pcmBytes, temperature = temperature)
        } catch (e: Exception) {
            throw NativeException(e)
        }
    }

    override fun close() = cleanable.clean()
}