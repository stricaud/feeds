package com.devo.feeds.output

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging

class LoggingAttributeOutput : AttributeOutput {

    private val log = KotlinLogging.logger { }

    override suspend fun write(eventUpdate: EventUpdate) {
        log.info { Json.encodeToString(eventUpdate) }
    }

    override fun close() {
        // noop
    }
}
