package com.devo.feeds.storage

import org.mapdb.Atomic
import org.mapdb.DB
import org.mapdb.DBMaker
import org.mapdb.HTreeMap
import org.mapdb.Serializer
import java.nio.file.Files
import java.nio.file.Path

abstract class LocalAttributeCache(private val db: DB) : AttributeCache {
    private val sentAttributes: HTreeMap.KeySet<String> = db.hashSet("sent-attributes")
        .serializer(Serializer.STRING)
        .createOrOpen()
    private val eventIds: HTreeMap<String, Long> = db.hashMap("event-ids")
        .keySerializer(Serializer.STRING)
        .valueSerializer(Serializer.LONG)
        .createOrOpen()
    private val attributeIds: HTreeMap<String, Long> = db.hashMap("attribute-ids")
        .keySerializer(Serializer.STRING)
        .valueSerializer(Serializer.LONG)
        .createOrOpen()
    private val attributeCounter: Atomic.Long = db.atomicLong("attribute-counter")
        .createOrOpen()
    private val eventCounter: Atomic.Long = db.atomicLong("event-counter")
        .createOrOpen()

    private fun attributeKey(feed: String, eventUUID: String, uuid: String): String = "$feed$eventUUID$uuid"
    private fun eventKey(feed: String, uuid: String): String = "$feed$uuid"

    override fun getEventId(feed: String, uuid: String): Long {
        val key = eventKey(feed, uuid)
        eventIds.putIfAbsent(key, eventCounter.incrementAndGet())
        return eventIds[key]!!
    }

    override fun getAttributeId(feed: String, eventId: String, uuid: String): Long {
        val key = attributeKey(feed, eventId, uuid)
        attributeIds.putIfAbsent(key, attributeCounter.incrementAndGet())
        return attributeIds[key]!!
    }

    override fun attributeHasSent(feed: String, eventId: String, uuid: String): Boolean {
        return sentAttributes.contains(attributeKey(feed, eventId, uuid))
    }

    override fun markAttributeSent(feed: String, eventId: String, uuid: String) {
        sentAttributes.add(attributeKey(feed, eventId, uuid))
    }

    override fun close() = db.close()
}

class FilesystemAttributeCache(path: Path) : LocalAttributeCache(run {
    if (!path.parent.toFile().exists()) {
        Files.createDirectories(path.parent)
    }
    DBMaker.fileDB(path.toString()).checksumHeaderBypass().make()
})

class InMemoryAttributeCache : LocalAttributeCache(DBMaker.memoryDB().make())
