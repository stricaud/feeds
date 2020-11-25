package com.devo.feeds.feed.integration

import com.devo.feeds.MispFeedServer
import com.devo.feeds.feed.CSVFeed
import com.devo.feeds.feed.FeedSpec
import com.devo.feeds.storage.AttributeCache
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.greaterThan
import io.ktor.util.KtorExperimentalAPI
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.Before
import org.junit.Test
import java.time.Duration
import java.util.UUID

class CSVFeedIntegrationTest {

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

    private lateinit var eventId: String

    @KtorExperimentalAPI
    @ObsoleteCoroutinesApi
    private lateinit var feed: CSVFeed

    @KtorExperimentalAPI
    @ObsoleteCoroutinesApi
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        eventId = UUID.randomUUID().toString()
        feed = CSVFeed(
            FeedSpec("test", Duration.ofSeconds(30), "http://localhost:${server.port}/attributes.csv", attributeCache),
            eventId,
        )
    }

    @KtorExperimentalAPI
    @ObsoleteCoroutinesApi
    @Test
    fun `Should fetch attributes`() {
        runBlocking {
            val attrs = feed.pull().toList()
            assertThat(attrs.size, equalTo(1))
            val event = attrs[0]
            assertThat(event.id, equalTo(eventId))
            assertThat(event.attributes.size, greaterThan(0))
        }
    }
}
