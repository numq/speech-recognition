package com.github.numq.stt.whisper

import com.github.numq.stt.exception.NativeException
import java.lang.ref.Cleaner

internal class NativeWhisperSpeechToText(modelPath: String) : AutoCloseable {
    private val nativeHandle = initNative(modelPath = modelPath).also { handle ->
        require(handle != -1L) { "Unable to initialize native library" }
    }

    private val cleanable = cleaner.register(this) { freeNative(nativeHandle) }

    private companion object {
        val cleaner: Cleaner = Cleaner.create()

        @JvmStatic
        external fun initNative(modelPath: String): Long

        @JvmStatic
        external fun recognizeNative(handle: Long, pcmBytes: ByteArray): String

        @JvmStatic
        external fun freeNative(handle: Long)
    }

    fun recognize(pcmBytes: ByteArray): String {
        try {
            return recognizeNative(handle = nativeHandle, pcmBytes = pcmBytes)
        } catch (e: Exception) {
            throw NativeException(e)
        }
    }

    override fun close() = cleanable.clean()
}