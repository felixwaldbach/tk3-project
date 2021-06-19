package de.darmstadt.tk

import android.Manifest
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import de.darmstadt.tk.background.ActivityReceiver
import de.darmstadt.tk.background.SleepWorker
import de.darmstadt.tk.ui.theme.SensingAppTheme
import de.darmstadt.tk.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    val TAG = "MainActivity"
    private val viewModel by viewModels<MainViewModel>()

    private var mTransitionsReceiver: ActivityReceiver? = null;



    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.i(TAG, "Perm was GRANTED")
            } else {
                Log.i(TAG, "Perm not granted")
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


//        setupWorkers()
        checkPermission()
        mTransitionsReceiver = ActivityReceiver()


        setContent {
            SensingAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    MainScreen(viewModel::startTracking,
                    viewModel.eventList)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(mTransitionsReceiver, IntentFilter(viewModel.TRANSITIONS_RECEIVER_ACTION));
    }

    private fun checkPermission() {
        viewModel.viewModelScope.launch(Dispatchers.Default) {
            when {
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.i(TAG,"Permission ACTIVITY_RECOGNITION GRANTED")
                }
                else -> {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    requestPermissionLauncher.launch(
                        Manifest.permission.ACTIVITY_RECOGNITION
                    )
                }
            }
        }

    }

    private fun setupWorkers() {
        val sleepWorker =
            PeriodicWorkRequestBuilder<SleepWorker>(5, TimeUnit.MINUTES).build()

        WorkManager.getInstance(applicationContext).enqueue(sleepWorker)
    }


}

