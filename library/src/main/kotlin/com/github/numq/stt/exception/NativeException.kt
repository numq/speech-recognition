package com.github.numq.stt.exception

data class NativeException(override val cause: Throwable) : Exception(cause)