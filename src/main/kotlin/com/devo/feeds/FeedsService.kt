package com.devo.feeds

import com.devo.feeds.data.misp.FeedAndTag
import com.devo.feeds.feed.CSVFeed
import com.devo.feeds.feed.Feed
import com.devo.feeds.feed.FeedException
import com.devo.feeds.feed.FeedSpec
import com.devo.feeds.feed.MispFeed
import com.devo.feeds.storage.InMemoryAttributeCache
import com.devo.feeds.output.AttributeOutput
import com.devo.feeds.output.DevoAttributeOutput
import com.devo.feeds.output.LoggingAttributeOutput
import com.devo.feeds.output.X509Credentials
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.time.Duration

class FeedsService(private val config: Config) {

    enum class OutputType {
        LOG, DEVO
    }

    private val log = KotlinLogging.logger { }
    private var running = false
    private val attributeCache = InMemoryAttributeCache()
    private val format = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val feedSchedule = Duration.ofHours(1)

    @KtorExperimentalAPI
    private val httpClient = HttpClient(CIO) {
        engine {
            threadsCount = 1
            requestTimeout
        }
    }

    private var outputs: List<AttributeOutput> = emptyList()
    private val tickers: MutableList<ReceiveChannel<Unit>> = mutableListOf()

    @KtorExperimentalAPI
    @FlowPreview
    @InternalCoroutinesApi
    @ObsoleteCoroutinesApi
    @Suppress("TooGenericExceptionCaught")
    suspend fun run() = coroutineScope {
        running = true
        outputs = getOutputs(config)
        log.info { "Starting Feeds process" }
        val configuredFeeds = getConfiguredFeeds(config)
        log.info { "Found ${configuredFeeds.size} configured feeds" }

        // Create flow from feeds
        getFeeds(configuredFeeds).asFlow().cancellable().flowOn(Dispatchers.Default).flatMapMerge { feed ->
            ticker(feedSchedule.toMillis(), 0).also {
                // keep track of the underlying ticker so we can stop it
                tickers.add(it)
            }.receiveAsFlow()
                .flatMapMerge {
                    try {
                        feed.run()
                    } catch (fe: FeedException) {
                        log.error { "Failed to fetch feed ${feed.name}: ${fe.message}" }
                        emptyFlow()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        log.error(e) { "Failed to fetch feed ${feed.name}" }
                        emptyFlow()
                    }
                }
        }.collect { update ->
            log.info { "Writing ${update.newAttributes.size} attributes for ${update.event.uuid}" }
            outputs.forEach {
                log.info { "Writing ${update.event.uuid}" }
                it.write(update)
            }
        }
    }

    fun stop() {
        if (running) {
            runBlocking {
                log.info { "Stopping Feeds process" }
                running = false
                tickers.forEach { it.cancel() }
                attributeCache.close()
            }
        }
    }

    @KtorExperimentalAPI
    @ObsoleteCoroutinesApi
    internal fun getFeeds(configuredFeeds: List<FeedAndTag>): List<Feed> =
        configuredFeeds.mapNotNull {
            val feedConfig = it.feed
            if (feedConfig.enabled) {
                val feedSpec = FeedSpec(feedConfig.name, feedSchedule, feedConfig.url, attributeCache)
                when (feedConfig.sourceFormat.toLowerCase()) {
                    "misp" -> MispFeed(feedSpec, httpClient)
                    "csv" -> CSVFeed(feedSpec, feedConfig.eventId!!, httpClient)
                    else -> {
                        log.info { "Ignoring unsupported feed ${feedConfig.name} with type ${feedConfig.sourceFormat}" }
                        null
                    }
                }
            } else {
                log.info { "Ignoring disabled feed ${feedConfig.name}" }
                null
            }
        }.also {
            log.info { "Found ${it.size} enabled feeds" }
        }

    private fun getOutputs(config: Config): List<AttributeOutput> =
        config.getConfigList("feeds.outputs").map {
            when (it.extract<OutputType>("type")) {
                OutputType.DEVO -> buildDevoOutput(it, it.getInt("threads"))
                OutputType.LOG -> LoggingAttributeOutput()
            }
        }

    private fun buildDevoOutput(config: Config, count: Int): DevoAttributeOutput {
        val host = config.getString("host")
        val port = config.getInt("port")
        val credentials = X509Credentials(
            config.getString("keystore"),
            config.getString("keystorePass"),
            mapOf("chain" to config.getString("chain"))
        )
        return DevoAttributeOutput(host, port, credentials, count)
            .apply { connect() }
    }

    @KtorExperimentalAPI
    internal suspend fun getConfiguredFeeds(config: Config): List<FeedAndTag> {
        val mispUrl = config.getString("feeds.misp.url")
        log.info { "Fetching config from $mispUrl/feeds" }
        val response = httpClient.get<String>("$mispUrl/feeds") {
            headers {
                append("Authorization", config.getString("feeds.misp.key"))
                append("Accept", "application/json")
            }
        }
        return format.decodeFromString(response)
    }
}

@ObsoleteCoroutinesApi
@FlowPreview
@KtorExperimentalAPI
@InternalCoroutinesApi
fun main() {
    val feeds = FeedsService(ConfigFactory.load())
    runBlocking {
        try {
            feeds.run()
        } finally {
            feeds.stop()
        }
    }
}
