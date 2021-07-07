package de.darmstadt.tk.service

import android.app.NotificationManager
import android.app.SearchManager
import android.content.*
import android.net.Uri
import android.provider.MediaStore
import com.google.android.gms.location.Geofence
import de.darmstadt.tk.MainActivity
import de.darmstadt.tk.data.Event

class HerrngartenService {
    val repo = ServiceLocator.getRepository()

    var still = false
    var inHerrngarten = false
    var isActive = false


    val geoFence = Geofence.Builder()
        .setRequestId("Herrngarten")
        .setCircularRegion(
            49.87809153136653,
            8.652307956476271,
            100f
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
        inHerrngarten = fenceEnter
        update(context)
    }

    fun update(context: Context) {
        if (inHerrngarten) {
            startParty(context)
        } else {
            endParty(context)
        }
    }

    fun startParty(context: Context) {
        if (isActive)
            return
        isActive = true

        repo.insertEvent(Event("Herrngarten ENTER", "Get ready to party"))
        // open website here
        val intent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
            putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)
            putExtra(MediaStore.EXTRA_MEDIA_ARTIST, "Final Countdown")
            putExtra(SearchManager.QUERY, "Final Countdown")
        }
        context.startActivity(intent)
    }

    fun endParty(context: Context) {
        if (!isActive)
            return
        isActive = false

        repo.insertEvent(Event("Herrngarten ENTER", "Back to work"))

    }
}