package com.devo.feeds.output

import com.devo.feeds.data.misp.Attribute
import com.devo.feeds.data.misp.Event

data class EventUpdate(val event: Event, val newAttributes: List<Attribute>)
