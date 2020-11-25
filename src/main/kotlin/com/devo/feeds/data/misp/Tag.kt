package com.devo.feeds.data.misp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id: String? = null,
    val colour: String? = null,
    val name: String? = null,
    val exportable: Boolean? = null,
    @SerialName("org_id") val orgId: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("hide_tag") val hideTag: Boolean? = null,
    @SerialName("numerical_value") val numericalValue: Int? = null,
    val count: Int? = null,
    @SerialName("attribute_count") val attributeCount: Int? = null,
    val favourite: Boolean? = null
)
