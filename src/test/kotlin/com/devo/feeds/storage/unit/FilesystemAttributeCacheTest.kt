package com.devo.feeds.storage.unit

import com.devo.feeds.storage.AttributeCache
import com.devo.feeds.storage.FilesystemAttributeCache
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

class FilesystemAttributeCacheTest {

    private lateinit var cache: AttributeCache

    @Before
    fun setUp() {
        val directory = Files.createTempDirectory("feeds-test")
        val path = Paths.get(directory.toString(), "attr-cache")
        cache = FilesystemAttributeCache().build(path)
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
