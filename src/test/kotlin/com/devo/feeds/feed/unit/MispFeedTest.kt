package com.devo.feeds.feed.unit

import com.devo.feeds.feed.FeedSpec
import com.devo.feeds.feed.MispFeed
import com.devo.feeds.storage.AttributeCache
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.ktor.util.KtorExperimentalAPI
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.junit.Before
import org.junit.Test
import java.time.Duration

class MispFeedTest {

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
                "https://www.circl.lu/doc/misp/feed-osint",
                attributeCache
            ),
        )
    }

    @KtorExperimentalAPI
    @ObsoleteCoroutinesApi
    @Test
    fun `Should parse MISP manifest`() {
        val manifestString =
            javaClass.classLoader.getResourceAsStream("manifest.json")!!.readAllBytes().decodeToString()
        val manifestEvents = feed.parseManifest(manifestString)
        assertThat(manifestEvents.size, equalTo(1208))
        val event = manifestEvents["57cebd3b-3b9c-4f70-95ff-414d950d210f"]
        assertThat(event?.timestamp, equalTo("1473241492"))
        assertThat(event?.tags?.size, equalTo(3))
    }

    @KtorExperimentalAPI
    @ObsoleteCoroutinesApi
    @Test
    fun `Should parse MISP event`() {
        val eventString = javaClass.classLoader.getResourceAsStream("event.json")!!.readAllBytes().decodeToString()
        val event = feed.parseEvent(eventString)
        assertThat(event?.tags?.size, equalTo(5))
        assertThat(event?.uuid, equalTo("59a3d08d-5dc8-4153-bc7c-456d950d210f"))
    }
}
