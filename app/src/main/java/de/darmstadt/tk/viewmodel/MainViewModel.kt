package de.darmstadt.tk.viewmodel

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.*

import com.google.android.gms.tasks.OnFailureListener

import com.google.android.gms.tasks.OnSuccessListener
import de.darmstadt.tk.BuildConfig
import de.darmstadt.tk.background.ActivityReceiver
import de.darmstadt.tk.background.SleepReceiver
import de.darmstadt.tk.data.Event
import de.darmstadt.tk.repo.MemEventRepo


class MainViewModel(var appCtx: Application) : AndroidViewModel(appCtx) {
    private val TAG: String = this::class.java.name


    var eventList = MemEventRepo.fetchEvents()

    val TRANSITIONS_RECEIVER_ACTION =
        BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION"

    fun startTracking() {
        setupActivityTransition()
        setupSleepTransition()
    }


    private fun setupActivityTransition() {
        Log.d(TAG, "setupActivityTransition")
        val transitions = mutableListOf<ActivityTransition>()

        transitions += ActivityTransition.Builder()
            .setActivityType(DetectedActivity.STILL)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build()
        transitions += ActivityTransition.Builder()
            .setActivityType(DetectedActivity.STILL)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build()

        val request = ActivityTransitionRequest(transitions)

        val intent = Intent(TRANSITIONS_RECEIVER_ACTION)
        val intent2 = Intent(appCtx,ActivityReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            appCtx,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val client = ActivityRecognition.getClient(appCtx)
        val task = client
            .requestActivityTransitionUpdates(request, pendingIntent)

        task.addOnSuccessListener(
            OnSuccessListener<Void?> {
                Log.i(TAG, "Transitions Api was successfully registered.")
                MemEventRepo.insertEvent(Event("Transitions-API", "Successfully registered"))
            })
        task.addOnFailureListener(
            OnFailureListener { e ->
                Log.e(TAG, "Transitions Api could NOT be registered: $e")
                MemEventRepo.insertEvent(Event("Transitions-API", "Could NOT be registered"))
            })

//        val intentAC = Intent(TRANSITIONS_RECEIVER_ACTION)
//        PendingIntent.getBroadcast(appCtx, 0, intent, 0)
//
//        val pendingIntentAC = PendingIntent.getBroadcast(
//            appCtx,
//            0,
//            intentAC,
//            PendingIntent.FLAG_UPDATE_CURRENT
//        )
//        val requestActivityUpdates = client.requestActivityUpdates(20000, pendingIntentAC)
//        requestActivityUpdates.addOnSuccessListener(
//            OnSuccessListener<Void?> {
//                Log.i(TAG, "Transitions ApiOLD was successfully registered.")
//                MemEventRepo.insertEvent(Event("Transitions-API-O", "Successfully registered"))
//            })
//        requestActivityUpdates.addOnFailureListener(
//            OnFailureListener { e ->
//                Log.e(TAG, "Transitions ApiOLD could NOT be registered: $e")
//                MemEventRepo.insertEvent(Event("Transitions-API-O", "Could NOT be registered"))
//            })
    }

    private fun setupSleepTransition() {
        Log.d(TAG, "setupSleepTransition")
        val pendingIntent = PendingIntent.getBroadcast(
            appCtx,
            0,
            Intent(appCtx, SleepReceiver::class.java),
            0
        )

        val task =
            ActivityRecognition.getClient(appCtx).requestSleepSegmentUpdates(
                pendingIntent,
                SleepSegmentRequest.getDefaultSleepSegmentRequest()
            )

        task.addOnSuccessListener(
            OnSuccessListener<Void?> {
                Log.i(TAG, "SLEEP Api was successfully registered.")
                MemEventRepo.insertEvent(Event("SLEEP-API", "Successfully registered"))
            })
        task.addOnFailureListener(
            OnFailureListener { e ->
                Log.e(TAG, "SLEEP Api could NOT be registered: $e")
                MemEventRepo.insertEvent(Event("SLEEP-API", "Could NOT be registered"))
            })
    }

}