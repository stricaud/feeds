package com.devo.feeds

import com.devo.feeds.data.misp.Attribute
import com.devo.feeds.data.misp.Event
import com.devo.feeds.data.misp.EventResponse
import com.devo.feeds.data.misp.FeedAndTag
import com.devo.feeds.data.misp.FeedConfig
import com.devo.feeds.data.misp.ManifestEvent
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondBytes
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MispFeedServer {
    val port = NetworkUtils.findAvailablePort()
    var feedCount = 3
    var manifestEvents = 50
    var csvEvents = 50
    var attributesPerEvent = 5

    val manifest = (0 until manifestEvents).map { it.toString() to ManifestEvent() }.toMap()
    val csv = (0 until csvEvents).joinToString("\n") { it.toString() }

    private val server = embeddedServer(Netty, port = port) {
        routing {
            get("/{feed}/{name}.json") {
                val feed = call.parameters["feed"]
                when (val name = call.parameters["name"]) {
                    "manifest" -> call.respondText(Json.encodeToString(manifest), ContentType.Application.Json)
                    else -> {
                        val id = "$feed-$name"
                        val attributes =
                            (0 until attributesPerEvent).map { Attribute(id = "$id-$it", uuid = "$id-$it") }
                        val response =
                            EventResponse(Event(id = id, uuid = id, attributes = attributes))
                        call.respondText(Json.encodeToString(response), ContentType.Application.Json)
                    }
                }
            }
            get("/attributes.csv") {
                call.respondText(csv, ContentType.Application.Json)
            }
            get("/feeds") {
                val feeds = (0 until feedCount).map {
                    val id = it.toString()
                    FeedAndTag(
                        FeedConfig(
                            id = id,
                            name = id,
                            provider = id,
                            url = "http://localhost:$port/$id",
                            enabled = true,
                            sourceFormat = "misp"
                        )
                    )
                }
                call.respondText(Json.encodeToString(feeds), ContentType.Application.Json)
            }
        }
    }

    fun start() {
        server.start()
    }

    fun stop() {
        server.stop(1000, 1000)
    }
}
