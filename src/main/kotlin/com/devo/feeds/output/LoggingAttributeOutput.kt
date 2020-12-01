package com.devo.feeds.output

import com.typesafe.config.Config
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging

class LoggingAttributeOutput : AttributeOutput {

    private val log = KotlinLogging.logger { }

    override fun build(config: Config): AttributeOutput {
        return this
    }

    override suspend fun write(feed: String, eventUpdate: EventUpdate) {
        log.info { "feed: $feed, event: ${Json.encodeToString(eventUpdate)}" }
    }

    override fun close() {
        // noop
    }
}
