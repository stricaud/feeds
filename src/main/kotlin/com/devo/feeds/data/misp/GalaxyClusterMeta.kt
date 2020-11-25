package com.devo.feeds.data.misp

import kotlinx.serialization.Serializable

@Serializable
data class GalaxyClusterMeta(
    val refs: List<String> = emptyList(),
    val synonyms: List<String> = emptyList(),
    val type: List<String> = emptyList()
)
