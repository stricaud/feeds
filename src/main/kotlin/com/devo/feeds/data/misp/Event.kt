package com.devo.feeds.data.misp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@kotlinx.serialization.Serializable
data class Event(
    val id: String? = null,
    val info: String? = null,
    @SerialName("org_id") val orgId: String? = null,
    @SerialName("orgc_id") val orgcId: String? = null,
    val distribution: String? = null,
    @SerialName("sharing_group_id") val sharingGroupId: String? = null,
    @SerialName("Tag") val tags: List<Tag> = emptyList(),
    @SerialName("publish_timestamp") val publishTimestamp: String? = null,
    val timestamp: String? = null,
    val analysis: String? = null,
    @SerialName("Attribute") val attributes: List<Attribute> = emptyList(),
    @SerialName("extends_uuid") val extendsUuid: String? = null,
    val published: Boolean? = null,
    val date: String? = null,
    @SerialName("Orgc") val orgc: Org? = null,
    @SerialName("threat_level_id") val threatLevelId: String? = null,
    val uuid: String? = null,
    @SerialName("Galaxy") val galaxy: List<Galaxy> = emptyList(),
    @SerialName("GalaxyCluster") val galaxyCluster: List<GalaxyCluster> = emptyList(),
    @SerialName("attribute_count") val attributeCount: String? = null,
    @SerialName("proposal_email_lock") val proposalEmailLock: Boolean? = null,
    val locked: Boolean? = null,
    @SerialName("sighting_timestamp") val sightingTimestamp: String? = null,
    @SerialName("disable_correlation") val disableCorrelation: Boolean? = null,
    val org: Org? = null,
    val eventTag: List<EventTag> = emptyList(),
    @SerialName("user_id") val userId: String? = null
)

@Serializable
data class EventResponse(@SerialName("Event") val event: Event)
