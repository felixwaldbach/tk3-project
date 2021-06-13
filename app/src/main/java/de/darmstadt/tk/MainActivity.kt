package de.darmstadt.tk

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.common.util.PlatformVersion
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import de.darmstadt.tk.background.ActivityReceiver
import de.darmstadt.tk.background.SleepReceiver
import de.darmstadt.tk.background.SleepWorker
import de.darmstadt.tk.ui.theme.SensingAppTheme
import de.patrick.keyattestation.MainViewModel
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    val TAG = "MainActivity"
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



//        setupWorkers()
        setupActivityTransition()
        setupSleepTransition()

        Log.d(TAG,"Is permission granted: ${checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)}")
        setContent {
            SensingAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Button(onClick = {
                    }) {
                        Text(text = "Click me")

                    }
                }
            }
        }
    }

    private fun setupWorkers() {
        val sleepWorker =
            PeriodicWorkRequestBuilder<SleepWorker>(5, TimeUnit.MINUTES).build()

        WorkManager.getInstance(applicationContext).enqueue(sleepWorker)
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
            applicationContext,
            0,
            Intent(applicationContext, ActivityReceiver::class.java),
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        ActivityRecognition.getClient(applicationContext)
            .requestActivityTransitionUpdates(request, pendingIntent)
    }

    private fun setupSleepTransition() {
        Log.d(TAG, "setupSleepTransition")
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            Intent(applicationContext, SleepReceiver::class.java),
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        ActivityRecognition.getClient(applicationContext).requestSleepSegmentUpdates(
            pendingIntent,
            SleepSegmentRequest.getDefaultSleepSegmentRequest()
        )
    }

}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SensingAppTheme {
        Greeting("Android")
    }
}