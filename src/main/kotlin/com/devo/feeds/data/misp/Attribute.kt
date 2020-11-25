package com.devo.feeds.data.misp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Attribute(
    val id: String? = null,
    @SerialName("event_id") val eventId: String? = null,
    @SerialName("object_id") val objectId: String? = null,
    @SerialName("object_relation") val objectRelation: String? = null,
    val comment: String? = null,
    val category: String? = null,
    val value1: String? = null,
    val value2: String? = null,
    val uuid: String? = null,
    val timestamp: String? = null,
    @SerialName("to_ids") val toIds: Boolean? = null,
    val distribution: String? = null,
    @SerialName("sharing_group_id") val sharingGroupId: String? = null,
    val value: String? = null,
    @SerialName("disable_correlation") val disableCorrelation: Boolean? = null,
    val deleted: Boolean? = null,
    @SerialName("first_seen") val firstSeen: Boolean? = null,
    @SerialName("last_seen") val lastSeen: Boolean? = null,
    val type: String? = null,
    @SerialName("Tag") val tags: List<Tag>? = emptyList()
)
