package com.devo.feeds.storage.unit

import com.devo.feeds.storage.AttributeCache
import com.devo.feeds.storage.InMemoryAttributeCache
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Before
import org.junit.Test

class InMemoryAttributeCacheTest {

    private lateinit var cache: AttributeCache

    @Before
    fun setUp() {
        cache = InMemoryAttributeCache().build()
    }

    @Test
    fun `Should increment eventId`() {
        assertThat(cache.getEventId("test", "test1"), equalTo(1L))
        assertThat(cache.getEventId("test", "test2"), equalTo(2L))
        assertThat(cache.getEventId("test", "test1"), equalTo(1L))
    }

    @Test
    fun `Should increment attributeId`() {
        assertThat(cache.getAttributeId("test", "test", "test1"), equalTo(1L))
        assertThat(cache.getAttributeId("test", "test", "test2"), equalTo(2L))
        assertThat(cache.getAttributeId("test", "test", "test1"), equalTo(1L))
    }

    @Test
    fun `Should check and mark attribute sent`() {
        assertThat(cache.attributeHasSent("test", "test", "test"), equalTo(false))
        cache.markAttributeSent("test", "test", "test")
        assertThat(cache.attributeHasSent("test", "test", "test"), equalTo(true))
    }
}
