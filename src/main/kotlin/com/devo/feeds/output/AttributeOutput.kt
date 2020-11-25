package com.devo.feeds.output

interface AttributeOutput {
    suspend fun write(eventUpdate: EventUpdate)
    fun close()

    class WriteException(message: String, cause: Throwable?) : RuntimeException(message, cause) {
        constructor(message: String) : this(message, null)
    }
}
