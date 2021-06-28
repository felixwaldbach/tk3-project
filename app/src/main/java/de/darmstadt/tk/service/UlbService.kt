package de.darmstadt.tk.service

import com.google.android.gms.location.Geofence
import de.darmstadt.tk.data.Event

class UlbService {
    val repo = ServiceLocator.getRepository()

    var still = false
    var inUlb = false
    val geoFence = Geofence.Builder()
        .setRequestId("ULB-Darmstadt")
        .setCircularRegion(
            49.876524,
            8.657471,
            57f
        )
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
        .setLoiteringDelay(500)
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .build()

    fun updateTransition(isStill:Boolean) {
        still = isStill
        update()
    }

    fun updateFence(fenceEnter:Boolean) {
        inUlb = fenceEnter
        update()
    }

    fun update() {
        if (inUlb && still) {
            silent()
        }
    }

    fun silent() {
        repo.insertEvent(Event("ULB-SILENT", "EVERYTHING IS SILENT!"))
    }
}