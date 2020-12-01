package com.devo.feeds.integration

import com.devo.feeds.FeedsService
import com.devo.feeds.MispFeedServer
import com.devo.feeds.TestSyslogServer
import com.devo.feeds.data.misp.DevoMispAttribute
import com.devo.feeds.data.misp.FeedAndTag
import com.devo.feeds.data.misp.FeedConfig
import com.devo.feeds.output.DevoAttributeOutput
import com.devo.feeds.storage.InMemoryAttributeCache
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.awaitility.Awaitility.await
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.time.Duration
import java.util.UUID

class EndToEnd {

    private lateinit var mispServer: MispFeedServer
    private lateinit var outputServer: TestSyslogServer
    private lateinit var outputServerJob: Job
    private lateinit var config: Config

    private fun resourcePath(resource: String): String =
        javaClass.classLoader.getResource(resource)!!.path

    @Before
    fun setUp() {
        mispServer = MispFeedServer().also { it.start() }
        outputServer = TestSyslogServer()
        outputServerJob = GlobalScope.launch {
            outputServer.run()
        }
        val tempPath = Files.createTempFile("feeds-e2e", UUID.randomUUID().toString())
        val mispUrl = "http://localhost:${mispServer.port}"
        config = ConfigFactory.parseMap(
            mapOf(
                "feeds.mispUpdateInterval" to "1 second",
                "feeds.misp.url" to mispUrl,
                "feeds.misp.key" to "",
                "feeds.cache" to mapOf(
                    "class" to InMemoryAttributeCache::class.qualifiedName,
                    "path" to tempPath.toString()
                ),
                "feeds.outputs" to listOf(
                    mapOf(
                        "class" to DevoAttributeOutput::class.qualifiedName,
                        "host" to "localhost",
                        "port" to outputServer.port,
                        "chain" to resourcePath("rootCA.crt"),
                        "keystore" to resourcePath("clienta.p12"),
                        "keystorePass" to "changeit",
                        "threads" to 1
                    )
                )
            )
        ).withFallback(ConfigFactory.load())
    }

    @After
    fun tearDown() {
        outputServer.stop()
        runBlocking { outputServerJob.join() }
        mispServer.stop()
    }

    @ObsoleteCoroutinesApi
    @FlowPreview
    @KtorExperimentalAPI
    @InternalCoroutinesApi
    @Test
    fun `Should run successfully end to end`() {
        val service = FeedsService(config)

        val serviceJob = GlobalScope.launch {
            service.run()
        }

        // Assert all events come through
        val expectedAttributeCount = mispServer.feedCount * mispServer.attributesPerEvent * mispServer.manifestEvents
        await().atMost(Duration.ofSeconds(300)).until { outputServer.receivedMessages.size == expectedAttributeCount }
        val byEventId = outputServer.receivedMessages.map { (_, message) ->
            val bodyStart = message.indexOf('{')
            Json.decodeFromString<DevoMispAttribute>(message.substring(bodyStart, message.length))
        }.groupBy { it.event.uuid!! }
        assertThat(byEventId.size, equalTo(mispServer.feedCount * mispServer.manifestEvents))
        byEventId.forEach { (_, attributes) ->
            assertThat(attributes.size, equalTo(mispServer.attributesPerEvent))
        }

        // Change one feed and add a new one
        val firstFeed = mispServer.feeds.first()
        mispServer.feeds = listOf(firstFeed.copy(feed = firstFeed.feed.copy(provider = "updated")))
            .plus(mispServer.feeds.subList(1, mispServer.feeds.size))
            .plus(
                FeedAndTag(
                    FeedConfig(
                        id = "new",
                        name = "new",
                        provider = "new",
                        url = "http://localhost:${mispServer.port}/new",
                        enabled = true,
                        sourceFormat = "misp"
                    )
                )
            )

        await().atMost(Duration.ofSeconds(300)).until { outputServer.receivedMessages.size == 750 }

        runBlocking {
            service.stop()
            serviceJob.join()
        }
    }
}
