package de.darmstadt.tk.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.animation.core.LinearOutSlowInEasing
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import de.darmstadt.tk.data.Event
import de.darmstadt.tk.repo.MemEventRepo
import java.time.Instant
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.google.android.gms.location.DetectedActivity




class ActivityReceiver : BroadcastReceiver() {
    val TAG = "ActivityReceiver"
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, "Received: $intent")

        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)!!
            for (event in result.transitionEvents) {
                val name = getActivityString(event.activityType)
                val type = getActivityTransitionString(event.transitionType)

                val desc = "${LocalTime.now()} :: $name ($type) - Elapsed: ${event.elapsedRealTimeNanos/1_000_000_000} sec"
                MemEventRepo.insertEvent(Event("Transitions-API", desc))
            }
        }
    }


    fun getActivityString(detectedActivityType: Int): String {
        return when (detectedActivityType) {
            DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
            DetectedActivity.ON_BICYCLE -> "ON_BICYCLE"
            DetectedActivity.ON_FOOT -> "ON_FOOT"
            DetectedActivity.RUNNING -> "RUNNING"
            DetectedActivity.STILL -> "STILL"
            DetectedActivity.TILTING -> "TILTING"
            DetectedActivity.UNKNOWN -> "UNKNOWN"
            DetectedActivity.WALKING -> "WALKING"
            else -> "UNKNOWN"
        }
    }

    fun getActivityTransitionString(transion: Int): String {
        return when (transion) {
            ActivityTransition.ACTIVITY_TRANSITION_ENTER -> "ENTER"
            ActivityTransition.ACTIVITY_TRANSITION_EXIT -> "EXIST"
            else -> "UNKNOWN"
        }
    }

}