package com.devo.feeds.output

import com.cloudbees.syslog.sender.TcpSyslogMessageSender
import com.devo.feeds.data.misp.DevoMispAttribute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging

class DevoAttributeOutput(
    private val host: String,
    private val port: Int,
    private val credentials: X509Credentials,
    private val connections: Int,
    private val tag: String = "threatintel.misp.attributes",
) : AttributeOutput {

    private val log = KotlinLogging.logger { }

    private var syslogSenders: List<Pair<String, TcpSyslogMessageSender>> = emptyList()

    fun connect() {
        log.info { "Connecting to Devo at $host:$port with $connections clients" }
        syslogSenders = (0 until connections).map {
            "$host:$port-$it" to TcpSyslogMessageSender().apply {
                syslogServerHostname = host
                syslogServerPort = port
                defaultAppName = tag
                isSsl = true
                sslContext = credentials.sslContext
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun serializeDevoAttribute(attribute: DevoMispAttribute): String {
        log.debug { "Serializing event:${attribute.event.uuid} attribute:${attribute.attribute.uuid}" }
        return Json.encodeToString(attribute)
    }

    @ObsoleteCoroutinesApi
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun sendMessageAsync(message: String) = withContext(Dispatchers.IO) {
        async {
            val (name, sender) = syslogSenders.random()
            log.info { "Sending ${message.length} bytes with $name" }
            sender.sendMessage(message)
            log.info { "Finished sending ${message.length} bytes" }
        }
    }

    private fun getDevoAttributesFromEvent(
        eventUpdate: EventUpdate
    ): Flow<DevoMispAttribute> = eventUpdate.newAttributes.asFlow().map { attr ->
        DevoMispAttribute(
            attribute = attr,
            event = eventUpdate.event,
            eventTags = eventUpdate.event.eventTag
        )
    }.onEach {
        log.trace { "Created Devo Attribute for event: ${it.event.uuid}, attribute:${it.attribute.uuid}" }
    }

    @InternalCoroutinesApi
    @ObsoleteCoroutinesApi
    override suspend fun write(eventUpdate: EventUpdate) = coroutineScope {
        if (syslogSenders.isEmpty()) {
            throw AttributeOutput.WriteException("DevoAttributeOutput must be connected before writing")
        } else {
            getDevoAttributesFromEvent(eventUpdate).map {
                log.info { "Writing event: ${it.event.uuid}, attribute: ${it.attribute.uuid}" }
                serializeDevoAttribute(it)
            }.map { sendMessageAsync(it) }.collect {
                it.await()
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override fun close() {
        syslogSenders.forEach { (_, sender) ->
            try {
                sender.close()
            } catch (npe: NullPointerException) {
                // ignore
            }
        }
    }
}
