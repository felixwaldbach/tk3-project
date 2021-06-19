package de.darmstadt.tk.repo

import androidx.compose.runtime.MutableState
import de.darmstadt.tk.data.Event

interface EventRepo {

    fun fetchEvents() : MutableList<Event>
    fun fetchLatestEvent(): Event
    fun insertEvent(event: Event)
}