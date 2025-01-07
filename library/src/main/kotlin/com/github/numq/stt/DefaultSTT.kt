package com.github.numq.stt

internal class DefaultSTT(private val nativeSTT: NativeSTT) : STT {
    override fun recognize(pcmBytes: ByteArray) = runCatching { nativeSTT.recognize(pcmBytes = pcmBytes) }

    override fun close() = runCatching { nativeSTT.close() }.getOrDefault(Unit)
}