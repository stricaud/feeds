package com.devo.feeds.data.misp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DevoMispAttribute(
    @SerialName("Attribute") val attribute: Attribute,
    @SerialName("Event") val event: Event,
    @SerialName("Object") val mispObject: MispObject? = null,
    @SerialName("EventTags") val eventTags: List<EventTag> = emptyList()
)
