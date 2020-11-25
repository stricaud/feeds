package com.devo.feeds.feed.unit

import com.devo.feeds.data.misp.Event
import com.devo.feeds.feed.Feed
import com.devo.feeds.feed.FeedSpec
import com.devo.feeds.storage.AttributeCache
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import java.time.Duration

@ObsoleteCoroutinesApi
open class MockFeed(attributeCache: AttributeCache) :
    Feed(FeedSpec("mock", Duration.ofSeconds(30), "https://localhost", attributeCache)) {

    private var toReturn = emptyList<Event>()

    override suspend fun pull(): Flow<Event> = toReturn.asFlow()

    fun setEvents(events: List<Event>) {
        toReturn = events
    }
}
