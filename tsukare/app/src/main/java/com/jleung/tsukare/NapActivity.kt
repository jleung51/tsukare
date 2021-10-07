package com.jleung.tsukare

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices

class NapActivity : AppCompatActivity() {

    private val tag = NapActivity::class.java.toString()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var requestPermissionLauncher: Any

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nap)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)



        // Request location permissions




        verifyLocationPermissions()

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location == null) {
                        Log.d(tag, "No location found.")
                    } else {
                        Log.d(tag, "Current location: (" +
                                location.latitude + "," + location.longitude + ")")
                    }
                }
        } catch (e: SecurityException) {
            Log.e(tag, "Error: " + e.message)
        }
    }

    private fun verifyLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
        }
    }

    private fun requestLocationPermissions() {

        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when {
                    !permissions.getOrDefault(
                        Manifest.permission.ACCESS_COARSE_LOCATION, false
                    ) -> {
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

        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }
}