package com.devo.feeds.data.misp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Galaxy(
    val id: String? = null,
    val uuid: String? = null,
    val name: String? = null,
    val type: String? = null,
    val description: String? = null,
    val version: String? = null,
    val icon: String? = null,
    val namespace: String? = null,
    @SerialName("kill_chain_order") val killChainOrder: String? = null,
    @SerialName("GalaxyCluster") val galaxyCluster: List<GalaxyCluster> = emptyList()
)
