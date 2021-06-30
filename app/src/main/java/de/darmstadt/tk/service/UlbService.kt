package de.darmstadt.tk.service

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import com.google.android.gms.location.Geofence
import de.darmstadt.tk.data.Event

class UlbService {
    val repo = ServiceLocator.getRepository()

    var still = false
    var inUlb = false
    var safedRinger = AudioManager.RINGER_MODE_NORMAL
    var safedMedia = 0
    var isActive = false


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

    fun updateTransition(context: Context, isStill: Boolean) {
        still = isStill
        update(context)
    }

    fun updateFence(context: Context, fenceEnter: Boolean) {
        inUlb = fenceEnter
        update(context)
    }

    fun update(context: Context) {
        if (inUlb && still) {
            silent(context)
        } else {
            unmute(context)
        }
    }

    fun silent(context: Context) {
        if (isActive)
            return
        isActive = true

        repo.insertEvent(Event("ULB-SILENT", "EVERYTHING IS SILENT!"))
        val audioManager: AudioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        safedRinger = audioManager.ringerMode
        safedMedia = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)


        if (notificationManager.isNotificationPolicyAccessGranted) {
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)

        } else {
            repo.insertEvent(
                Event(
                    "DoNotDistrube-Permission",
                    "Please grant the app Do Not Distrube rights"
                )
            )
        }


    }

    fun unmute(context: Context) {
        if (!isActive)
            return
        isActive = false

        repo.insertEvent(Event("ULB-SILENT", "BACK to NORMAL"))
        val audioManager: AudioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            audioManager.ringerMode = safedRinger
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, safedMedia, 0)
        } else {
            repo.insertEvent(
                Event(
                    "DoNotDistrube-Permission",
                    "Please grant the app Do Not Distrube rights"
                )
            )
        }
    }
}