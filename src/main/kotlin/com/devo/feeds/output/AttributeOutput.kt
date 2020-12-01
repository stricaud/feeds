package com.devo.feeds.output

import com.typesafe.config.Config

interface AttributeOutput {
    suspend fun write(feed: String, eventUpdate: EventUpdate)
    fun build(config: Config): AttributeOutput
    fun close()

    class WriteException(message: String, cause: Throwable?) : RuntimeException(message, cause) {
        constructor(message: String) : this(message, null)
    }
}
