package de.darmstadt.tk.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.*
import de.darmstadt.tk.data.Event
import de.darmstadt.tk.service.ServiceLocator


class AldiReceiver : BroadcastReceiver() {
    val TAG = "AldiReceiver"
    val repo = ServiceLocator.getRepository()

    override fun onReceive(context: Context?, intent: Intent?) {

        repo.insertEvent(Event("ALDI-API", "Processing..."))

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
        geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            repo.insertEvent(Event("ALDI-API", "Successfully dying"))
            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            // Get the transition details as a String.
            /*val geofenceTransitionDetails = getGeofenceTransitionDetails(
                this,
                geofenceTransition,
                triggeringGeofences
            )

            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails)
            Log.i(TAG, geofenceTransitionDetails)*/
        } else {
            repo.insertEvent(Event("ALDI-API", "Unsuccessfully dying"))
            // Log the error.
            // Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
               // geofenceTransition))
        }
    }

}