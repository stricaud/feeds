package com.devo.feeds.output.unit

import com.devo.feeds.TestSyslogServer
import com.devo.feeds.data.misp.Attribute
import com.devo.feeds.data.misp.DevoMispAttribute
import com.devo.feeds.data.misp.Event
import com.devo.feeds.output.DevoAttributeOutput
import com.devo.feeds.output.EventUpdate
import com.devo.feeds.storage.AttributeCache
import com.devo.feeds.storage.InMemoryAttributeCache
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.endsWith
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.greaterThan
import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.awaitility.Awaitility.await
import org.junit.After
import org.junit.Before
import org.junit.Test

class DevoAttributeOutputTest {

    private lateinit var attributeCache: AttributeCache
    private lateinit var server: TestSyslogServer
    private lateinit var output: DevoAttributeOutput

    @ObsoleteCoroutinesApi
    @Before
    fun setUp() {
        server = TestSyslogServer()
        attributeCache = InMemoryAttributeCache().build()
        val loader = javaClass.classLoader
        Thread(server).start()
        server.waitUntilStarted()
        output = DevoAttributeOutput().also {
            it.build(
                ConfigFactory.parseMap(
                    mapOf(
                        "host" to "localhost",
                        "port" to server.port,
                        "chain" to loader.getResource("rootCA.crt")!!.path,
                        "keystore" to loader.getResource("clienta.p12")!!.path,
                        "keystorePass" to "changeit",
                        "threads" to 1
                    )
                )
            )
        }
    }

    @After
    fun tearDown() {
        attributeCache.close()
        output.close()
        server.stop()
    }

    @InternalCoroutinesApi
    @ObsoleteCoroutinesApi
    @Test
    fun `Should write events to syslog server`() {
        val attributeCount = 20
        val eventId = "0"
        val testEvent = Event(
            id = eventId,
            attributes = (0 until attributeCount).map { Attribute(eventId = eventId, id = it.toString()) }
        )
        runBlocking {
            output.write("feed", EventUpdate(testEvent, testEvent.attributes))
        }
        assertThat(server.receivedMessages.size, greaterThan(0))
        await().until { server.receivedMessages.size == attributeCount }
        server.receivedMessages.forEachIndexed { index, received ->
            val message = received.message
            val bodyStart = message.indexOf('{')
            val header = message.substring(0, bodyStart).trim()
            assertThat(header, endsWith("threatintel.misp.attributes:"))
            val body = message.substring(bodyStart, message.length).trim()
            val devoMispAttribute = Json.decodeFromString<DevoMispAttribute>(body)
            assertThat(devoMispAttribute.event, equalTo(testEvent))
            assertThat(devoMispAttribute.attribute, equalTo(testEvent.attributes[index]))
        }
    }
}
