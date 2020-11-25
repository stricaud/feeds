package com.devo.feeds.integration

import com.devo.feeds.FeedsService
import com.devo.feeds.MispFeedServer
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.typesafe.config.ConfigFactory
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.Before
import org.junit.Test

class FeedsServiceIntegrationTest {
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

    private lateinit var service: FeedsService

    @Before
    fun setUp() {
        service = FeedsService(ConfigFactory.empty())
    }

    @KtorExperimentalAPI
    @Test
    fun `Should retrieve configured feeds`() {
        val config = ConfigFactory
            .parseMap(
                mapOf(
                    "feeds.misp.url" to "http://localhost:${server.port}",
                    "feeds.misp.key" to ""
                )
            )
        runBlocking {
            val feedsAndTags = service.getConfiguredFeeds(config)
            assertThat(feedsAndTags.size, equalTo(3))
            feedsAndTags.withIndex().forEach { (i, value) ->
                assertThat(value.feed.id, equalTo(i.toString()))
                assertThat(value.feed.name, equalTo(i.toString()))
            }
        }
    }
}
