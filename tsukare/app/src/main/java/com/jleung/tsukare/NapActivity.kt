package com.jleung.tsukare

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.random.Random

class NapActivity : AppCompatActivity() {

    private val tag = NapActivity::class.java.toString()

    private lateinit var currentLocation: Location

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback

    private lateinit var alarmManager: AlarmManager

    private val requestCheckSettings = 0x1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nap)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Create settings for location requests
        val locationRequestDetails = LocationRequest.create().apply {
            interval = 10000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        // Prep
        verifyLocationPermissions()
        activateLocation(locationRequestDetails)
        getLocationOnce()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {

                    // Action to take upon receiving a new location
                    Log.d(tag, "Current location: (${location.latitude}. ${location.longitude})")
                    currentLocation = location

                }
            }
        }

        // Request recurring location updates
        Log.d(tag, "Subscribing to location updates.")
        requestRecurringLocationUpdates(locationRequestDetails, locationCallback)




        verifySystemAlertWindowPermissions()

        alarmManager = getAlarmManager()
        verifyAlarmPermissions(alarmManager)
        val pendingIntent = setAlarmTarget(TsukareBroadcastReceiver::class.java)

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(System.currentTimeMillis() + 5000, pendingIntent),
            pendingIntent
        )
        Log.d(tag, "Set alarm clock to activate TsukareBroadcastReceiver.")

    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(tag, "Unsubscribing from location updates.")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // Private functions

    // Ensures location permissions are given; requests if not given.
    private fun verifyLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
        }
    }

    private fun requestRecurringLocationUpdates(
        locationRequest: LocationRequest, locationCallback: LocationCallback
    ) {

        // Duplicated from verifyLocationPermissions, else compiler throws error
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun requestLocationPermissions() {

        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when {
                    // Handle case where user rejects
                    permissions.getOrDefault(
                        Manifest.permission.ACCESS_COARSE_LOCATION, false
                    ) -> {
                        // Continue
                    }
                    permissions.getOrDefault(
                        Manifest.permission.ACCESS_FINE_LOCATION, false
                    ) -> {
                        // Continue
                    }
                    else -> {
                        Toast.makeText(
                            this, "This app requires location permissions to " +
                                    "check if you are nearing your station.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
            }
        }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    // Checks to see if Location is activated and requests user if not activated.
    private fun activateLocation(locationRequest: LocationRequest) {

        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        // Create a task to request location settings
        LocationServices
            .getSettingsClient(this)
            .checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                Log.d(tag, "Location settings successfully retrieved. Location is activated.")
            }
            .addOnFailureListener { exception ->
                Log.d(
                    tag, "Request to retrieve location settings failed. " +
                            "Location not currently activated."
                )
                askToActivateLocation(exception)

                finish()
            }

    }

    // If location is not available, ask the user for permission
    private fun askToActivateLocation(exception: Exception) {
        if (exception is ResolvableApiException) {
            try {
                exception.startResolutionForResult(this@NapActivity, requestCheckSettings)
            } catch (sendEx: IntentSender.SendIntentException) {
                // Ignore
            }
        }
    }

    private fun getLocationOnce() {
        try {
            fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
                CancellationTokenSource().token
            )
                .addOnSuccessListener { location: Location? ->
                    if (location == null) {
                        Log.d(tag, "No location found.")
                        return@addOnSuccessListener
                    }

                    Log.d(tag, "Current location: (${location.latitude}. ${location.longitude})")
                    currentLocation = location
                }
        } catch (e: SecurityException) {
            Log.e(tag, "Error: " + e.message)
            askToActivateLocation(e)
        }
    }

    // Alarm functions

    private fun getAlarmManager() : AlarmManager {
        return getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    private fun verifyAlarmPermissions(alarmManager: AlarmManager) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {

                // Request user to enable permissions
                Toast.makeText(
                    this,
                    "Alarm permissions are required for this application.",
                    Toast.LENGTH_LONG
                ).show()

                // Navigate to permissions
                startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))

                // Exit activity
                finish()
            }
        }

    }

    // Returns a pendingIntent to be used for AlarmManager.
    private fun setAlarmTarget(target: Class<*>): PendingIntent {
        return PendingIntent.getBroadcast(
            this, Random.nextInt(),
            Intent(this, target),
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun verifySystemAlertWindowPermissions() {

        if (!Settings.canDrawOverlays(this)) {

            // Request user to enable permissions
            Toast.makeText(
                this,
                "This app requires permission to be displayed over other apps, to alert you " +
                "when you have arrived.",
                Toast.LENGTH_LONG
            ).show()

            // Navigate to permissions
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))

            // Exit activity
            finish()
        }

    }

}