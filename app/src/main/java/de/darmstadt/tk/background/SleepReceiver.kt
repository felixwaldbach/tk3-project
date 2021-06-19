package de.darmstadt.tk.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.SleepClassifyEvent
import com.google.android.gms.location.SleepSegmentEvent
import de.darmstadt.tk.data.Event
import de.darmstadt.tk.repo.MemEventRepo
import java.time.Instant
import java.time.format.DateTimeFormatter

class SleepReceiver: BroadcastReceiver() {
    val TAG = "SleepReciever"

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive(): $intent")

        if (SleepSegmentEvent.hasEvents(intent)) {
            val sleepSegmentEvents: List<SleepSegmentEvent> =
                SleepSegmentEvent.extractEvents(intent)
            Log.d(TAG, "SleepSegmentEvent List: $sleepSegmentEvents")
            for (sleep in sleepSegmentEvents) {
                val desc =
                    sleep.toString() + " TIMESTAMP: " + DateTimeFormatter.ISO_TIME.format(Instant.now())
                MemEventRepo.insertEvent(Event("Sleep-API", desc))
            }
        } else if (SleepClassifyEvent.hasEvents(intent)) {
            val sleepClassifyEvents: List<SleepClassifyEvent> =
                SleepClassifyEvent.extractEvents(intent)
            Log.d(TAG, "SleepClassifyEvent List: $sleepClassifyEvents")

            for (sleep in sleepClassifyEvents) {
                val desc =
                    sleep.toString() + " TIMESTAMP: " + DateTimeFormatter.ISO_TIME.format(Instant.now())
                MemEventRepo.insertEvent(Event("Sleep-API", desc))
            }
        }
    }
}