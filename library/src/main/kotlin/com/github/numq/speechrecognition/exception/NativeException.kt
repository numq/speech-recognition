package com.github.numq.speechrecognition.exception

data class NativeException(override val cause: Throwable) : Exception(cause)