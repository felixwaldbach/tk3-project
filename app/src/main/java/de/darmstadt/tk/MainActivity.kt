package de.darmstadt.tk

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import de.darmstadt.tk.background.ActivityReceiver
import de.darmstadt.tk.background.GeoFenceReceiver
import de.darmstadt.tk.background.SleepReceiver
import de.darmstadt.tk.background.SleepWorker
import de.darmstadt.tk.data.Event
import de.darmstadt.tk.service.ServiceLocator
import de.darmstadt.tk.ui.theme.SensingAppTheme
import de.darmstadt.tk.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import android.location.LocationManager
import android.net.Uri
import android.os.Looper
import android.widget.Toast
import com.google.android.gms.location.*
import kotlin.concurrent.fixedRateTimer

class MainActivity : ComponentActivity() {

    val TAG = "MainActivity"
    private val viewModel by viewModels<MainViewModel>()
    val repo = ServiceLocator.getRepository()

    private var mTransitionsReceiver: ActivityReceiver? = null;
    private var mSleepReceiver: SleepReceiver? = null;
    private var mGeofenceReceiver: GeoFenceReceiver? = null;
    private lateinit var notificationManager: NotificationManager

    lateinit var geofencingClient: GeofencingClient
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var lat = 0.0f
    private var long = 0.0f

    val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            Log.i(TAG, "DDD- CHANGED to $result")

            if (notificationManager.isNotificationPolicyAccessGranted)
                checkPermissions(emptyList())
            else
                ServiceLocator.getRepository().insertEvent(
                    Event(
                        "DoNotDisturb",
                        "Please enable do not Disturb for this app!"
                    )
                )

        }

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.i(TAG, "Perm was GRANTED")
                checkPermissions(emptyList())
            } else {
                ServiceLocator.getRepository()
                    .insertEvent(Event("PERMISSION MISSING", "No permission to run the app"))
                Log.i(TAG, "Perm not granted")
            }
        }


    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var allGranted = true
            permissions.entries.forEach {
                allGranted = allGranted && it.value
                if (it.value) {
                    Log.i(TAG, "${it.key} was GRANTED")
                } else {
                    ServiceLocator.getRepository()
                        .insertEvent(
                            Event(
                                "PERMISSION MISSING",
                                "No ${it.key} permission to run the app"
                            )
                        )
                    Log.i(TAG, "${it.key} not granted")
                }
            }
            checkPermissions(emptyList())
        }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            //repo.insertEvent(Event("mLastLocation", "Lat: " + mLastLocation.latitude.toString() + ", Long: " + mLastLocation.longitude.toString()))
        }
    }

    @TargetApi(30)
    private fun askBackgroundLocationPermissionAPI30() {
        AlertDialog.Builder(this)
            .setTitle("Need Background Location")
            .setMessage("Need location for geofencing API to ensure a smoother operation! Please grant this app the 'Always option'")
            .setPositiveButton("Grant 'Always'") { _, _ ->
                // this request will take user to Application's Setting page
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            .setNegativeButton("Abort") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun askDoNotDisturbePermission() {
        AlertDialog.Builder(this)
            .setTitle("Need Do Not Disturb access")
            .setMessage("Enable Do Not Disturb access for the app")
            .setPositiveButton("Grant") { _, _ ->
                // this request will take user to Application's Setting page

                startForResult.launch(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            }
            .setNegativeButton("Abort") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mTransitionsReceiver = ActivityReceiver()
        mSleepReceiver = SleepReceiver()
        mGeofenceReceiver = GeoFenceReceiver()
        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

//        setupWorkers()
        val permissions = mutableListOf(
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,

            )
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)


        checkPermissions(
            permissions
        )

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
        registerReceiver(mGeofenceReceiver, IntentFilter(viewModel.GEO_RECEIVER_ACTION));

        startLocationLogging()
    }

    private fun checkPermissions(permissions: List<String>) {
        val requestPerm = mutableListOf<String>()

        permissions.forEach { perm ->
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    perm
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(TAG, "Permission $perm GRANTED")
            } else {
                requestPerm.add(perm)
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
        if (requestPerm.isNotEmpty()) {
            viewModel.viewModelScope.launch(Dispatchers.Default) {
                requestMultiplePermissions.launch(
                    requestPerm.toTypedArray()
                )
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            )
                askBackgroundLocationPermissionAPI30()
            else if (!notificationManager.isNotificationPolicyAccessGranted)
                askDoNotDisturbePermission()
            else
                viewModel.startTracking()
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

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (isLocationEnabled()) {

            mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                var location: Location? = task.result
                //if (location == null) {
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
//                } else {
//                    if(lat != location.latitude.toFloat() || long != location.longitude.toFloat()) {
//                        lat = location.latitude.toFloat()
//                        long = location.longitude.toFloat()
//                        repo.insertEvent(Event("Last Location", "Lat: " + lat + ", Long: " + long))
//                    }
//                }
            }
        } else {
            Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

    private fun startLocationLogging() {
        fixedRateTimer("location", false, 0L, 1000) {
            this@MainActivity.runOnUiThread {
                getLocation()
            }
        }
    }


}

