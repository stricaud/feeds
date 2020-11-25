package com.devo.feeds.data.misp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedConfig(
    val id: String,
    val name: String,
    val provider: String,
    val url: String,
    val rules: String? = null,
    val enabled: Boolean = false,
    val distribution: String? = null,
    @SerialName("sharing_group_id") val sharingGroupId: String? = null,
    @SerialName("tag_id") val tagId: String? = null,
    val default: Boolean? = null,
    @SerialName("source_format") val sourceFormat: String,
    @SerialName("fixed_event") val fixedEvent: Boolean? = null,
    @SerialName("delta_merge") val deltaMerge: Boolean? = null,
    @SerialName("event_id") val eventId: String? = null,
    val publish: Boolean? = null,
    @SerialName("override_ids") val overrideIds: Boolean? = null,
    val settings: String? = null,
    @SerialName("input_source") val inputSource: String? = null,
    @SerialName("delete_local_file") val deleteLocalFile: Boolean? = null,
    @SerialName("lookup_visible") val lookupVisible: Boolean? = null,
    val headers: String? = null,
    @SerialName("caching_enabled") val cachingEnabled: Boolean? = null,
    @SerialName("force_to_ids") val forceToIds: Boolean? = null,
    @SerialName("orgc_id") val orgcId: String? = null,
    @SerialName("cache_timestamp") val cacheTimestamp: String? = null,
    val tags: List<String> = emptyList(),
)

@Serializable
data class FeedAndTag(
    @SerialName("Feed") val feed: FeedConfig,
    @SerialName("Tag") val tag: Tag? = null
)
