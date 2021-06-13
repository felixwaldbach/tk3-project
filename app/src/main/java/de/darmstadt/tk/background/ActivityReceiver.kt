package de.darmstadt.tk.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.animation.core.LinearOutSlowInEasing
import com.google.android.gms.location.ActivityTransitionResult

class ActivityReceiver : BroadcastReceiver() {
    val TAG = "ActivityReceiver"
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG,"Received: $intent")

        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)!!
            for (event in result.transitionEvents) {
                // chronological sequence of events....
            }
        }
    }
}