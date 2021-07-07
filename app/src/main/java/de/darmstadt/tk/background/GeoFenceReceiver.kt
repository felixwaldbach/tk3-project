package de.darmstadt.tk.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.*
import de.darmstadt.tk.data.Event
import de.darmstadt.tk.service.ServiceLocator
import android.text.TextUtils

import com.google.android.gms.location.Geofence


class GeoFenceReceiver : BroadcastReceiver() {
    val TAG = "GeoFenceReceiver"
    val repo = ServiceLocator.getRepository()
    val ulb = ServiceLocator.getUlbService()
    val rewe = ServiceLocator.getReweService()
    val herrngarten = ServiceLocator.getHerrngartenService()

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, "GEO-Received: $intent")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Get the geofences that were triggered. A single event can trigger
        // multiple geofences.
        val triggeringGeofences = geofencingEvent.triggeringGeofences

        // Get the transition details as a String.
        val geofenceTransitionDetails = getGeofenceTransitionDetails(
            geofenceTransition,
            triggeringGeofences
        )
        Log.i(TAG, "GeoFences: $geofenceTransitionDetails")

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {



            if (ulb.geoFence.requestId in triggeringGeofences.map { e->e.requestId }) {
                ulb.updateFence(context!!,geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)

                repo.insertEvent(Event("GeoFence-API", "Inside ULB fence?: ${ulb.inUlb})"))
            }


            if (rewe.geoFence.requestId in triggeringGeofences.map { e->e.requestId }) {
                rewe.updateFence(context!!,geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)

                repo.insertEvent(Event("GeoFence-API", "Entered (${rewe.inRewe}) REWE"))
            }

            if (herrngarten.geoFence.requestId in triggeringGeofences.map { e->e.requestId }) {
                herrngarten.updateFence(context!!,geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)

                repo.insertEvent(Event("GeoFence-API", "Entered (${herrngarten.inHerrngarten}) Herrngarten"))
            }
        } else {
            // Log the error.
            Log.e(TAG, "UNKNOWN TYPE")
        }
    }

    private fun getGeofenceTransitionDetails(
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>
    ): String {
        val geofenceTransitionString: String = when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "transition - enter"
            Geofence.GEOFENCE_TRANSITION_DWELL -> "transition - DWEL"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "transition - exit"
            else -> "unknown transition"
        }


        // Get the Ids of each geofence that was triggered.
        val triggeringGeofencesIdsList = ArrayList<Any>()
        for (geofence in triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.requestId)
        }
        val triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList)
        return "$geofenceTransitionString: $triggeringGeofencesIdsString"
    }

}