package de.darmstadt.tk.repo

import androidx.compose.runtime.*
import de.darmstadt.tk.data.Event

class MemEventRepo : EventRepo {

    var events = mutableStateListOf<Event>()
        private set

    override fun fetchEvents(): MutableList<Event> {
        return events
    }

    override fun fetchLatestEvent(): Event {
        TODO()
    }

    override fun insertEvent(event: Event) {
        events.add(event)
    }
}