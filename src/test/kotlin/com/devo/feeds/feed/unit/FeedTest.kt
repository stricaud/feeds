package com.devo.feeds.feed.unit

import com.devo.feeds.data.misp.Attribute
import com.devo.feeds.data.misp.Event
import com.devo.feeds.feed.Feed
import com.devo.feeds.feed.FeedException
import com.devo.feeds.storage.AttributeCache
import com.devo.feeds.output.AttributeOutput
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isA
import com.natpryce.hamkrest.isNullOrBlank
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.net.SocketException
import java.util.UUID
import kotlin.random.Random
import kotlin.test.assertFailsWith

class FeedTest {

    @ObsoleteCoroutinesApi
    private lateinit var feed: MockFeed
    private lateinit var cache: AttributeCache
    private lateinit var output: AttributeOutput

    @ObsoleteCoroutinesApi
    private val blankEventUUID = Feed.generateEventUUID(Event()).toString()

    @ObsoleteCoroutinesApi
    @Before
    fun setUp() {
        output = mockk()
        cache = mockk()
        feed = MockFeed(cache)
    }

    @ObsoleteCoroutinesApi
    private fun expectCacheLookups(event: Event) {
        val expectedEventUUID = Feed.generateEventUUID(event).toString()
        every { cache.getEventId(feed.name, expectedEventUUID) } returns 5L
        event.attributes.forEachIndexed { i, attr ->
            val expectedAttrUUID = Feed.generateAttributeUUID("5", attr).toString()
            every { cache.getAttributeId(feed.name, "5", expectedAttrUUID) } returns 5L + i
        }

    }

    @ObsoleteCoroutinesApi
    @Test
    fun `Should add ids to event`() {
        val expectedEvent = Event(id = "1", uuid = blankEventUUID)
        every { cache.getEventId(feed.name, expectedEvent.uuid!!) } returns 1L
        assertThat(feed.ensureEventIDs(Event()), equalTo(expectedEvent))
    }

    @ObsoleteCoroutinesApi
    @Test
    fun `Should not overwrite existing event ids`() {
        val event = Event(id = "2", uuid = "uuid1")
        assertThat(feed.ensureEventIDs(event), equalTo(event))
    }

    @ObsoleteCoroutinesApi
    @Test
    fun `Should add ids to attribute`() {
        val attribute = Attribute()
        val eventId = UUID.randomUUID().toString()
        val expectedAttribute =
            Attribute(eventId = eventId, uuid = Feed.generateAttributeUUID(eventId, attribute).toString(), id = "2")
        every { cache.getAttributeId(feed.name, eventId, expectedAttribute.uuid!!) } returns 2L
        assertThat(feed.ensureAttributeIDs(eventId, Attribute()), equalTo(expectedAttribute))
    }

    @ObsoleteCoroutinesApi
    @Test
    fun `Should not overwrite existing attribute ids`() {
        val attribute = Attribute(eventId = "1", uuid = "2", id = "3")
        assertThat(feed.ensureAttributeIDs("1", attribute), equalTo(attribute))
    }

    @ObsoleteCoroutinesApi
    @Test
    fun `Should overwrite eventId in attribute`() {
        val eventId = UUID.randomUUID().toString()
        val attribute = Attribute(eventId = "1", uuid = "2", id = "3")
        val expectedAttribute = attribute.copy(eventId = eventId)
        assertThat(feed.ensureAttributeIDs(eventId, attribute), equalTo(expectedAttribute))
    }

    @ObsoleteCoroutinesApi
    @Test
    fun `Should fill event and attribute ids`() {
        val event = Event(attributes = (0 until 5).map { Attribute(value = it.toString()) })
        expectCacheLookups(event)
        val withIds = feed.ensureIds(event)
        assertThat(withIds.id, equalTo("5"))
        assertThat(withIds.uuid, !isNullOrBlank)
        withIds.attributes.forEachIndexed { i, attr ->
            assertThat(attr.id, equalTo((5L + i).toString()))
            assertThat(attr.uuid, !isNullOrBlank)
            assertThat(withIds.id, equalTo("5"))
        }
    }

    @ObsoleteCoroutinesApi
    @Test
    fun `Should send event and attributes`() {
        every { cache.getEventId(feed.name, any()) } returns Random.nextLong()
        every { cache.getAttributeId(feed.name, any(), any()) } returns Random.nextLong()
        every { cache.attributeHasSent(feed.name, any(), any()) } returns false

        val eventCount = 5
        val attributeCount = 5
        val events = (0 until eventCount).map { i ->
            Event(attributes = (0 until attributeCount).map { j -> Attribute(value = (i + j).toString()) })
        }

        feed.setEvents(events)
        runBlocking {
            val eventUpdates = feed.run().toList()
            assertThat(eventUpdates.size, equalTo(eventCount))
            eventUpdates.forEach { update ->
                assertThat(update.event.uuid, !isNullOrBlank)
                assertThat(update.event.id, !isNullOrBlank)
                update.newAttributes.forEach {
                    assertThat(it.eventId, equalTo(update.event.id))
                    assertThat(it.uuid, !isNullOrBlank)
                    assertThat(it.id, !isNullOrBlank)
                }
            }
        }

    }

    @ObsoleteCoroutinesApi
    @Test
    fun `Should not send attributes that have already been sent`() {
        every { cache.getEventId(feed.name, any()) } returns Random.nextLong()
        every { cache.getAttributeId(feed.name, any(), any()) } returns Random.nextLong()
        every { cache.attributeHasSent(feed.name, any(), any()) } returns false

        val eventCount = 5
        val attributeCount = 10
        val events = (0 until eventCount).map { i ->
            Event(
                id = UUID.randomUUID().toString(),
                attributes = (0 until attributeCount).map { j ->
                    Attribute(
                        uuid = UUID.randomUUID().toString(),
                        value = (i + j).toString()
                    )
                }
            )
        }


        val duplicateEvent = events[0]
        duplicateEvent.attributes.forEach { attr ->
            every { cache.attributeHasSent(feed.name, duplicateEvent.id!!, attr.uuid!!) } returns true
        }

        val updatedEvent = events[1]
        updatedEvent.attributes.subList(0, 3).forEach { attr ->
            every { cache.attributeHasSent(feed.name, updatedEvent.id!!, attr.uuid!!) } returns true
        }

        feed.setEvents(events)

        runBlocking {
            val attributesByEventId =
                feed.run().toList().map { update -> update.event.id to update.newAttributes }.toMap()
            assertThat(attributesByEventId.size, equalTo(eventCount - 1))
            assertThat(attributesByEventId.containsKey(duplicateEvent.id), equalTo(false))
            assertThat(attributesByEventId[updatedEvent.id]!!.size, equalTo(attributeCount - 3))
        }

    }

    @ObsoleteCoroutinesApi
    @Test
    @ExperimentalCoroutinesApi
    fun `Should propagate exception`() {
        val badFeed = object : MockFeed(cache) {
            override suspend fun pull(): Flow<Event> {
                throw FeedException("BAD", SocketException("ALSO BAD"))
            }
        }
        val exception = assertFailsWith<FeedException> {
            runBlocking {
                badFeed.run()
            }
        }
        assertThat(exception.message, equalTo("BAD"))
        assertThat(exception.cause!!, isA<SocketException>())
        assertThat(exception.cause!!.message, equalTo("ALSO BAD"))
    }

}
