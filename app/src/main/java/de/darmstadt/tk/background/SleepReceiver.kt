package de.darmstadt.tk.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.SleepClassifyEvent
import com.google.android.gms.location.SleepSegmentEvent

class SleepReceiver: BroadcastReceiver() {
    val TAG = "SleepReciever"

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive(): $intent")

        if (SleepSegmentEvent.hasEvents(intent)) {
            val sleepSegmentEvents: List<SleepSegmentEvent> =
                SleepSegmentEvent.extractEvents(intent)
            Log.d(TAG, "SleepSegmentEvent List: $sleepSegmentEvents")

        } else if (SleepClassifyEvent.hasEvents(intent)) {
            val sleepClassifyEvents: List<SleepClassifyEvent> =
                SleepClassifyEvent.extractEvents(intent)
            Log.d(TAG, "SleepClassifyEvent List: $sleepClassifyEvents")

        }
    }
}