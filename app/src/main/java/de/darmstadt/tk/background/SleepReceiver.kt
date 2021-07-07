package de.darmstadt.tk.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.SleepClassifyEvent
import com.google.android.gms.location.SleepSegmentEvent
import de.darmstadt.tk.data.Event
import de.darmstadt.tk.repo.MemEventRepo
import de.darmstadt.tk.service.ServiceLocator
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class SleepReceiver : BroadcastReceiver() {
    val TAG = "SleepReciever"
    var formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        .withLocale(Locale.GERMANY)
        .withZone(ZoneId.systemDefault())
    val repo = ServiceLocator.getRepository()
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive(): $intent")

        if (SleepSegmentEvent.hasEvents(intent)) {
            val sleepSegmentEvents: List<SleepSegmentEvent> =
                SleepSegmentEvent.extractEvents(intent)
            Log.d(TAG, "SleepSegmentEvent List: $sleepSegmentEvents")
            for (sleep in sleepSegmentEvents) {
                val startTime = Instant.ofEpochMilli(sleep.startTimeMillis)
                val endTime = Instant.ofEpochMilli(sleep.endTimeMillis)
                val sleepDurationmili = sleep.segmentDurationMillis
                val desc =
                    "${LocalTime.now()} :: Start Time: ${formatter.format(startTime)}, duration: ${sleepDurationmili / 1_000} sec, endtime: ${formatter.format(endTime)}"
//                repo.insertEvent(Event("Sleep-API", desc))
            }
        } else if (SleepClassifyEvent.hasEvents(intent)) {
            val sleepClassifyEvents: List<SleepClassifyEvent> =
                SleepClassifyEvent.extractEvents(intent)
            Log.d(TAG, "SleepClassifyEvent List: $sleepClassifyEvents")

            for (sleep in sleepClassifyEvents) {
                val desc = "${LocalTime.now()} :: Confidence: ${sleep.confidence}, light: ${sleep.light}, motion ${sleep.motion}"
//                repo.insertEvent(Event("Sleep-API", desc))
            }
        }
    }
}