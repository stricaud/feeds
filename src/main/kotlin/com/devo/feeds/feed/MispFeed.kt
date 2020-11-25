package com.devo.feeds.feed

import com.devo.feeds.data.misp.Event
import com.devo.feeds.data.misp.EventResponse
import com.devo.feeds.data.misp.ManifestEvent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.get
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
class MispFeed(
    spec: FeedSpec,
    private val httpClient: HttpClient = HttpClient(CIO)
) : Feed(spec) {

    companion object {
        const val MAX_RETRIES = 3
    }

    private val log = KotlinLogging.logger { }
    private val format = Json { ignoreUnknownKeys = true }

    @KtorExperimentalAPI
    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    override suspend fun pull(): Flow<Event> = fetchEvents(
        parseManifest(fetchManifest()).keys
    ).mapNotNull {
        parseEvent(it)
    }.onEach {
        log.debug { "Parsed event ${it.uuid}" }
    }

    internal suspend fun fetchManifest(): String = coroutineScope {
        val manifestUrl = "$url/manifest.json"
        log.info { "Fetching manifest from $manifestUrl" }
        try {
            httpClient.get(manifestUrl)
        } catch (e: ClientRequestException) {
            throw FeedException("Failed to fetch feed $name", e)
        }
    }

    internal fun parseManifest(data: String): Map<String, ManifestEvent> {
        log.trace { "Parsing manifest" }
        val parsed: Map<String, ManifestEvent> = format.decodeFromString(data)
        log.debug { "Parsed manifest with ${parsed.size} events" }
        return parsed
    }

    @KtorExperimentalAPI
    @Suppress("TooGenericExceptionCaught")
    internal suspend fun fetchEvents(uuids: Set<String>): Flow<String> = coroutineScope {
        uuids.asFlow().cancellable().map {
            var attempt = 0
            var result: String? = null
            var exception: Exception? = null
            val eventUrl = "$url/$it.json"
            while (result == null && attempt < MAX_RETRIES) {
                log.info { "Fetching event from $eventUrl" }
                try {
                    result = httpClient.get<String>(eventUrl)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    log.error(e) { "Failed to fetch $eventUrl - will retry" }
                    exception = e
                    attempt++
                }
            }
            when (result) {
                null -> {
                    log.error(exception) { "Unable to fetch $eventUrl" }
                    null
                }
                else -> result
            }
        }.filterNotNull()
    }

    @Suppress("TooGenericExceptionCaught")
    internal fun parseEvent(data: String): Event? {
        log.trace { "Parsing event" }
        return try {
            val parsed: Event = format.decodeFromString<EventResponse>(data).event
            log.debug { "Parsed event ${parsed.uuid}" }
            parsed
        } catch (e: Exception) {
            log.error(e) { "Failed to parse event: $data" }
            null
        }
    }
}
