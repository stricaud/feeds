package com.devo.feeds.integration

import com.devo.feeds.FeedsService
import com.devo.feeds.MispFeedServer
import com.devo.feeds.TestSyslogServer
import com.devo.feeds.data.misp.DevoMispAttribute
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.typesafe.config.ConfigFactory
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.awaitility.Awaitility.await
import org.junit.Test
import java.time.Duration

class EndToEnd {

    private fun resourcePath(resource: String): String =
        javaClass.classLoader.getResource(resource)!!.path

    @ObsoleteCoroutinesApi
    @FlowPreview
    @KtorExperimentalAPI
    @InternalCoroutinesApi
    @Test
    fun `Should run successfully end to end`() {
        val mispServer = MispFeedServer().also { it.start() }
        val mispUrl = "http://localhost:${mispServer.port}"
        val outputServer = TestSyslogServer()
        val outputServerJob = GlobalScope.launch {
            outputServer.run()
        }

        val config = ConfigFactory.parseMap(
            mapOf(
                "feeds.misp.url" to mispUrl,
                "feeds.misp.key" to "",
                "feeds.outputs" to listOf(
                    mapOf(
                        "type" to "DEVO",
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
        val service = FeedsService(config)

        val serviceJob = GlobalScope.launch {
            service.run()
        }

        val expectedAttributeCount = mispServer.feedCount * mispServer.attributesPerEvent * mispServer.manifestEvents
        await().until {
            outputServer.receivedMessages.size == expectedAttributeCount
        }
        val byEventId = outputServer.receivedMessages.map { (_, message) ->
            val bodyStart = message.indexOf('{')
            Json.decodeFromString<DevoMispAttribute>(message.substring(bodyStart, message.length))
        }.groupBy { it.event.uuid!! }
        assertThat(byEventId.size, equalTo(mispServer.feedCount * mispServer.manifestEvents))
        byEventId.forEach { (_, attributes) ->
            assertThat(attributes.size, equalTo(mispServer.attributesPerEvent))
        }

        service.stop()
        runBlocking { serviceJob.join() }
        outputServer.stop()
        runBlocking { outputServerJob.join() }
        mispServer.stop()
    }
}
