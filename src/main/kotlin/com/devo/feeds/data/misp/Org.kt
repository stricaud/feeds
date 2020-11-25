package com.devo.feeds.data.misp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Org(
    val id: String? = null,
    val uuid: String? = null,
    val name: String? = null,
    val description: String? = null,
    val type: String? = null,
    val sector: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("created_by_email") val createdByEmail: String? = null,
    @SerialName("restricted_to_domain") val restrictedToEmail: List<String>? = null,
    @SerialName("date_modified") val dateModified: String? = null,
    @SerialName("date_created") val dateCreated: String? = null,
    val nationality: String? = null,
    @SerialName("user_count") val userCount: String? = null,
    val local: Boolean? = null,
    val contacts: String? = null,
    @SerialName("landing_page") val landingPage: String? = null
)
