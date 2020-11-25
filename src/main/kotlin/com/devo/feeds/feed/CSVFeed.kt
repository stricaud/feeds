package com.devo.feeds.feed

import com.devo.feeds.data.csv.SingletonCSVRow
import com.devo.feeds.data.misp.Attribute
import com.devo.feeds.data.misp.Event
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.get
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import mu.KotlinLogging

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
class CSVFeed constructor(
    spec: FeedSpec,
    private val eventId: String,
    private val httpClient: HttpClient = HttpClient(CIO)
) : Feed(spec) {

    private val log = KotlinLogging.logger { }

    override suspend fun pull(): Flow<Event> =
        flowOf(fetchSingletonAttributeFile())
            .flowOn(Dispatchers.IO)
            .map { parseSingletonAttributes(it) }
            .flowOn(Dispatchers.Default)
            .map { Event(id = eventId, attributes = it) }

    private suspend fun fetchSingletonAttributeFile(): String = coroutineScope {
        log.info { "Fetching attributes from $url" }
        try {
            httpClient.get(url)
        } catch (e: ClientRequestException) {
            throw FeedException("Failed to fetch feed $name", e)
        }
    }

    private fun parseSingletonAttributes(body: String): List<Attribute> {
        return body.split('\n').map { rowToAttribute(SingletonCSVRow(it)) }
    }

    private fun rowToAttribute(row: SingletonCSVRow): Attribute {
        // Leave other IDs empty for id checking to fill in
        return Attribute(
            eventId = eventId,
            value = row.value
        )
    }
}
