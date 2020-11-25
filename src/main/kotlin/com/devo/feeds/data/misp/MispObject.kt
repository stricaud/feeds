package com.devo.feeds.data.misp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MispObject(
    val id: String,
    val name: String,
    @SerialName("meta-category") val metaCategory: String,
    val description: String,
    @SerialName("template_uuid") val templateUuid: String,
    @SerialName("template_version") val templateVersion: String,
    @SerialName("event_id") val eventId: String,
    val uuid: String,
    val timestamp: String,
    val distribution: String,
    @SerialName("sharing_group_id") val sharingGroupId: String,
    val comment: String,
    val deleted: String,
    @SerialName("first_seen") val firstSeen: String,
    @SerialName("last_seen") val lastSeen: String
)
