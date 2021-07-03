package de.darmstadt.tk.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import com.google.android.gms.location.Geofence
import de.darmstadt.tk.MainActivity
import de.darmstadt.tk.data.Event

class ReweService {
    val repo = ServiceLocator.getRepository()

    var still = false
    var inRewe = false
    var isActive = false


    val geoFence = Geofence.Builder()
        .setRequestId("Home")
        .setCircularRegion(
            49.88242929671279,
            8.656686416217237,
            50f
        )
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
        .setLoiteringDelay(500)
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .build()

    fun updateTransition(context: Context, isStill: Boolean) {
        still = isStill
        update(context)
    }

    fun updateFence(context: Context, fenceEnter: Boolean) {
        inRewe = fenceEnter
        update(context)
    }

    fun update(context: Context) {
        if (inRewe) {
            openWebsite(context)
        } else {
            closeWebsite(context)
        }
    }

    fun openWebsite(context: Context) {
        if (isActive)
            return
        isActive = true

        repo.insertEvent(Event("REWE Enter", "DISCOUNTS INCOMING!"))
        // open website here
        val url = "https://www.rewe.de/angebote/darmstadt/240270/rewe-markt-liebfrauenstrasse-34/"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        context.startActivity(intent)
    }

    fun closeWebsite(context: Context) {
        if (!isActive)
            return
        isActive = false

        repo.insertEvent(Event("REWE Exit", "REWE Exit Event"))

    }
}