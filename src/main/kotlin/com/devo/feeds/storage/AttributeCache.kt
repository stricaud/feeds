package com.devo.feeds.storage

interface AttributeCache {
    fun getEventId(feed: String, uuid: String): Long
    fun getAttributeId(feed: String, eventId: String, uuid: String): Long
    fun attributeHasSent(feed: String, eventId: String, uuid: String): Boolean
    fun markAttributeSent(feed: String, eventId: String, uuid: String)
    fun close()
}
