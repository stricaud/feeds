package com.devo.feeds.feed.integration

import com.devo.feeds.MispFeedServer
import com.devo.feeds.data.misp.ManifestEvent
import com.devo.feeds.feed.FeedSpec
import com.devo.feeds.feed.MispFeed
import com.devo.feeds.storage.AttributeCache
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.ktor.util.KtorExperimentalAPI
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.AfterClass
import org.junit.Before
import org.junit.Test
import java.time.Duration

class MispFeedIntegrationTest {
    companion object {
        private val server = MispFeedServer().also {
            it.start()
        }

        @JvmStatic
        @AfterClass
        fun tearDownClass() {
            server.stop()
        }
    }

    @MockK
    private lateinit var attributeCache: AttributeCache

    @KtorExperimentalAPI
    @ObsoleteCoroutinesApi
    private lateinit var feed: MispFeed

    @KtorExperimentalAPI
    @ObsoleteCoroutinesApi
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        feed = MispFeed(
            FeedSpec(
                "test",
                Duration.ofSeconds(30),
                "http://localhost:${server.port}/0",
                attributeCache
            ),
        )
    }

    @KtorExperimentalAPI
    @ObsoleteCoroutinesApi
    @Test
    fun `Should fetch MISP manifest`() {
        runBlocking {
            val manifest = feed.fetchManifest()
            assertThat(
                Json.decodeFromString<Map<String, ManifestEvent>>(manifest),
                equalTo(server.manifest)
            )
        }
    }

    @InternalCoroutinesApi
    @ObsoleteCoroutinesApi
    @KtorExperimentalAPI
    @Test
    fun `Should fetch MISP events`() {
        runBlocking {
            val events = feed.fetchEvents(setOf("event1", "event2", "event3")).toList()
            assertThat(events.size, equalTo(3))
        }
    }
}


