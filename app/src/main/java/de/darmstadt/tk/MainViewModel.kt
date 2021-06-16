package de.patrick.keyattestation

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import de.darmstadt.tk.background.ActivityReceiver
import de.darmstadt.tk.background.SleepReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import androidx.annotation.NonNull

import com.google.android.gms.tasks.OnFailureListener

import com.google.android.gms.tasks.OnSuccessListener





class MainViewModel(var appCtx: Application) : AndroidViewModel(appCtx) {
    private val TAG: String = this::class.java.name

    var state by mutableStateOf(0);

    fun startTracking() {
        setupActivityTransition()
        setupSleepTransition()
    }

    fun startAttestation() {
        viewModelScope.launch(Dispatchers.Default) {

        }
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

        val pendingIntent = PendingIntent.getBroadcast(
            appCtx,
            0,
            Intent(appCtx, ActivityReceiver::class.java),
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val task = ActivityRecognition.getClient(appCtx)
            .requestActivityTransitionUpdates(request, pendingIntent)

        task.addOnSuccessListener(
            OnSuccessListener<Void?> {
                Log.i(TAG,"Transitions Api was successfully registered.")
            })
        task.addOnFailureListener(
            OnFailureListener { e ->
                Log.e(TAG, "Transitions Api could NOT be registered: $e")
            })
    }

    private fun setupSleepTransition() {
        Log.d(TAG, "setupSleepTransition")
        val pendingIntent = PendingIntent.getBroadcast(
            appCtx,
            0,
            Intent(appCtx, SleepReceiver::class.java),
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        ActivityRecognition.getClient(appCtx).requestSleepSegmentUpdates(
            pendingIntent,
            SleepSegmentRequest.getDefaultSleepSegmentRequest()
        )
    }

}