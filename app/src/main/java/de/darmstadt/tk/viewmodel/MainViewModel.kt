package de.darmstadt.tk.viewmodel

import android.Manifest
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.*

import com.google.android.gms.tasks.OnFailureListener

import com.google.android.gms.tasks.OnSuccessListener
import de.darmstadt.tk.BuildConfig
import de.darmstadt.tk.data.Event
import de.darmstadt.tk.service.ServiceLocator
import com.google.android.gms.location.GeofencingRequest
import androidx.core.content.ContextCompat.startActivity

import android.os.Build

import android.app.NotificationManager
import android.content.Context
import android.provider.Settings
import androidx.core.content.ContextCompat


class MainViewModel(var appCtx: Application) : AndroidViewModel(appCtx) {
    private val TAG: String = this::class.java.name
    val repo = ServiceLocator.getRepository()
    val ulb = ServiceLocator.getUlbService()

    var eventList = repo.fetchEvents()

    val TRANSITIONS_RECEIVER_ACTION =
        BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION"

    val SLEEP_RECEIVER_ACTION =
        BuildConfig.APPLICATION_ID + "SLEEP_RECEIVER_ACTION"

    val GEO_RECEIVER_ACTION =
        BuildConfig.APPLICATION_ID + "GEO_RECEIVER_ACTION"

    fun startTracking() {
        setupActivityTransition()
        setupSleepTransition()
        setupGeoFencing()
    }


    private fun setupGeoFencing() {
        val listOfFences = mutableListOf<Geofence>()
        val geofencingClient = LocationServices.getGeofencingClient(appCtx)


        listOfFences += ulb.geoFence

        val req = GeofencingRequest.Builder().addGeofences(listOfFences)
            .setInitialTrigger(
                GeofencingRequest.INITIAL_TRIGGER_DWELL or
                        GeofencingRequest.INITIAL_TRIGGER_ENTER or
                        GeofencingRequest.INITIAL_TRIGGER_EXIT
            )
            .build()

        val intent = Intent(GEO_RECEIVER_ACTION)

        val pendingIntent = PendingIntent.getBroadcast(
            appCtx,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (ActivityCompat.checkSelfPermission(
                appCtx,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "EEE- NO PERMISSIONS!")
            repo.insertEvent(Event("GeoFence-API", "NO PERMISSION"))
            return
        }
        val addGeofences = geofencingClient.addGeofences(req, pendingIntent)

        addGeofences.addOnSuccessListener(
            OnSuccessListener<Void?> {
                Log.i(TAG, "GeoFence Api was successfully registered.")
                repo.insertEvent(Event("GeoFence-API", "Successfully registered"))
            })
        addGeofences.addOnFailureListener(
            OnFailureListener { e ->
                Log.e(TAG, "GeoFence Api could NOT be registered: $e")
                repo.insertEvent(Event("GeoFence-API", "Could NOT be registered"))
            })
//

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
                repo.insertEvent(Event("Transitions-API", "Successfully registered"))
            })
        task.addOnFailureListener(
            OnFailureListener { e ->
                Log.e(TAG, "Transitions Api could NOT be registered: $e")
                repo.insertEvent(Event("Transitions-API", "Could NOT be registered"))
            })
//
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
//                repo.insertEvent(Event("Transitions-API-O", "Successfully registered"))
//            })
//        requestActivityUpdates.addOnFailureListener(
//            OnFailureListener { e ->
//                Log.e(TAG, "Transitions ApiOLD could NOT be registered: $e")
//                repo.insertEvent(Event("Transitions-API-O", "Could NOT be registered"))
//            })
    }

    private fun setupSleepTransition() {
        Log.d(TAG, "setupSleepTransition")
        val intent = Intent(SLEEP_RECEIVER_ACTION)
        val pendingIntent = PendingIntent.getBroadcast(
            appCtx,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val task =
            ActivityRecognition.getClient(appCtx).requestSleepSegmentUpdates(
                pendingIntent,
                SleepSegmentRequest.getDefaultSleepSegmentRequest()
            )

        task.addOnSuccessListener(
            OnSuccessListener<Void?> {
                Log.i(TAG, "SLEEP Api was successfully registered.")
                repo.insertEvent(Event("SLEEP-API", "Successfully registered"))
            })
        task.addOnFailureListener(
            OnFailureListener { e ->
                Log.e(TAG, "SLEEP Api could NOT be registered: $e")
                repo.insertEvent(Event("SLEEP-API", "Could NOT be registered"))
            })
    }

}