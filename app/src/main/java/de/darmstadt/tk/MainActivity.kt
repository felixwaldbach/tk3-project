package de.darmstadt.tk

import android.Manifest
import android.annotation.SuppressLint
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
import de.darmstadt.tk.background.AldiReceiver
import de.darmstadt.tk.background.SleepReceiver
import de.darmstadt.tk.background.SleepWorker
import de.darmstadt.tk.data.Event
import de.darmstadt.tk.service.ServiceLocator
import de.darmstadt.tk.ui.theme.SensingAppTheme
import de.darmstadt.tk.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import android.app.PendingIntent;
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.gms.location.R
import kotlin.concurrent.fixedRateTimer

class MainActivity : ComponentActivity() {

    val TAG = "MainActivity"
    private val viewModel by viewModels<MainViewModel>()

    private var mTransitionsReceiver: ActivityReceiver? = null;
    private var mAldiReceiver: AldiReceiver? = null;
    private var mSleepReceiver: SleepReceiver? = null;

    lateinit var geofencingClient: GeofencingClient
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    var geofenceList: MutableList<Geofence> = mutableListOf()

    val repo = ServiceLocator.getRepository()

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, AldiReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            repo.insertEvent(Event("mLastLocation", "Lat: " + mLastLocation.latitude.toString() + ", Long: " + mLastLocation.longitude.toString()))
        }
    }

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.i(TAG, "Perm was GRANTED")
                viewModel.startTracking()
            } else {
                ServiceLocator.getRepository()
                    .insertEvent(Event("PERMISSION MISSING", "No permission to run the app"))
                Log.i(TAG, "Perm not granted")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fixedRateTimer("location", false, 0L, 1000) {
            this@MainActivity.runOnUiThread {
                getLastLocation()
            }
        }

        // getLastLocation()

//        setupWorkers()

        geofencingClient = LocationServices.getGeofencingClient(this)

        geofenceList.add(
            Geofence.Builder()
                .setRequestId("home")
                .setCircularRegion(
                    37.4219983,
                    -122.084,
                    2f
                )
                .setExpirationDuration(45678903274320743)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
        )

        Log.i(TAG, geofenceList.get(0).toString())

        checkPermission()
        mTransitionsReceiver = ActivityReceiver()
        mAldiReceiver = AldiReceiver()
        mSleepReceiver = SleepReceiver()

        geofencingClient?.addGeofences(getGeofencingRequest(), geofencePendingIntent)?.run {
            addOnSuccessListener {
                // Geofences added
                // ...
            }
            addOnFailureListener {
                // Failed to add geofences
                // ...
            }
        }



        setContent {
            SensingAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    MainScreen(
                        viewModel::startTracking,
                        viewModel.eventList
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(mTransitionsReceiver, IntentFilter(viewModel.TRANSITIONS_RECEIVER_ACTION));
        registerReceiver(mSleepReceiver, IntentFilter(viewModel.SLEEP_RECEIVER_ACTION));
        registerReceiver(mAldiReceiver, IntentFilter(viewModel.ALDI_RECEIVER_ACTION))
        isLocationEnabled()
        // getUrlFromIntent()
    }

    private fun checkPermission() {
        viewModel.viewModelScope.launch(Dispatchers.Default) {
            when {
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.i(TAG, "Permission ACTIVITY_RECOGNITION GRANTED")
                    viewModel.startTracking()
                }
                else -> {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    requestPermissionLauncher.launch(
                        Manifest.permission.ACTIVITY_RECOGNITION
                    )
                }
            }
            when {
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.i(TAG, "Permission ACCESS_FINE_LOCATION GRANTED")
                    viewModel.startTracking()
                }
                else -> {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this requests
                    requestPermissionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }
            }
            when {
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.i(TAG, "Permission ACCESS_COARSE_LOCATION GRANTED")
                    viewModel.startTracking()
                }
                else -> {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    requestPermissionLauncher.launch(
                        Manifest.permission.ACCESS_COARSE_LOCATION
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

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }

    private fun getUrlFromIntent() {
        val url = "https://www.aldi-sued.de/de/angebote.html"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (isLocationEnabled()) {

            mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                var location: Location? = task.result
                if (location == null) {
                    requestNewLocationData()
                } else {
                    repo.insertEvent(Event("Last Location", "Lat: " + location.latitude.toString() + ", Long: " + location.longitude.toString()))
                }
            }
        } else {
            Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }


}

