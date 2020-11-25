package com.devo.feeds.data.misp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ManifestEvent(
    val info: String? = null,
    @SerialName("Orgc") val orgC: Org? = null,
    val analysis: String? = null,
    @SerialName("Tag") val tags: List<Tag>? = emptyList(),
    @SerialName("published_timestamp") val publishedTimestamp: String? = null,
    val timestamp: String? = null,
    val date: String? = null,
    @SerialName("threat_level_id") val threatLevelId: String? = null,
)
