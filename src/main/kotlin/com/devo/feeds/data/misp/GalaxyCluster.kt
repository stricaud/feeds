package com.devo.feeds.data.misp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GalaxyCluster(
    val id: String? = null,
    val uuid: String? = null,
    @SerialName("collection_uuid") val collectionUuid: String? = null,
    val type: String? = null,
    val value: String? = null,
    @SerialName("tag_name") val tagName: String? = null,
    val description: String? = null,
    @SerialName("galaxy_id") val galaxyId: String? = null,
    val source: String? = null,
    val authors: List<String> = emptyList(),
    val version: String? = null,
    @SerialName("tag_id") val tagId: String? = null,
    val local: Boolean? = null,
    val meta: GalaxyClusterMeta? = null,
    @SerialName("Galaxy") val galaxy: Galaxy? = null
)
