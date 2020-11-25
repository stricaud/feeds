package com.devo.feeds.data.misp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventTag(
    val id: String? = null,
    val name: String? = null,
    val colour: String? = null,
    val exportable: Boolean? = null,
    @SerialName("org_id") val orgId: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("hide_tag") val hideTag: Boolean? = null,
    @SerialName("numerical_value") val numericalValue: String? = null,
    val local: Boolean? = null,
    @SerialName("event_id") val eventId: Boolean? = null,
    @SerialName("tag_id") val tagId: String? = null,
    @SerialName("Tag") val tag: Tag? = null
)
