package com.app.attcompse.util

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

class LocationUtility(private val activity: Activity) {

    private companion object {
        const val LOCATION_UPDATE_INTERVAL = 10000L // 10 seconds
        const val REQUEST_LOCATION_PERMISSION = 1001
        const val MINIMAL_DISTANCE = 10F // 10 meters
    }

    private val locationManager: LocationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
    private val settingsClient: SettingsClient = LocationServices.getSettingsClient(activity)
    private lateinit var locationRequest: LocationRequest
    //private var locationCallback: LocationCallback? = null

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.forEach { location ->
                onLocationChangeListener?.onLocationChange(location)
            }
        }
    }

    private var onLocationChangeListener: OnLocationChangeListener? = null
    private val handler = Handler(Looper.getMainLooper())

    fun startLocationUpdates() {
        if (isLocationPermissionGranted()) {
            if (isGpsEnabled()) {
                requestLocationUpdates()
            } else {
                showEnableGpsDialog()
            }
        } else {
            requestLocationPermission()
        }
    }

    fun stopLocationUpdates() {
        locationCallback.let {
            fusedLocationClient.removeLocationUpdates(it)
            handler.removeCallbacks(locationUpdateRunnable)
        }
    }

    private fun requestLocationUpdates() {
        //Deprecated
        /*locationRequest = LocationRequest.create().apply {
            interval = LOCATION_UPDATE_INTERVAL
            fastestInterval = LOCATION_UPDATE_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }*/

        // New builder
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL).apply {
            setMinUpdateDistanceMeters(MINIMAL_DISTANCE)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        settingsClient.checkLocationSettings(builder.build())
            .addOnSuccessListener(activity) {
                startLocationUpdatesWithCallback()
            }
            .addOnFailureListener(activity) { e ->
                if (e is ApiException) {
                    if (e.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        try {
                            val resolvable = e as ResolvableApiException
                            resolvable.startResolutionForResult(activity, REQUEST_LOCATION_PERMISSION)
                        } catch (sendEx: IntentSender.SendIntentException) {
                            // Handle the exception
                        }
                    }
                }
            }
    }

    private fun startLocationUpdatesWithCallback() {
        val callback = locationCallback
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
            handler.postDelayed(locationUpdateRunnable, LOCATION_UPDATE_INTERVAL)
        }
    }

    /*private fun startLocationUpdatesWithCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { location ->
                    onLocationChangeListener?.onLocationChange(location)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            handler.postDelayed(locationUpdateRunnable, LOCATION_UPDATE_INTERVAL)
        }
    }*/

    private val locationUpdateRunnable: Runnable = object : Runnable {
        override fun run() {
            onLocationChangeListener?.let { listener ->
                getLastKnownLocation { location ->
                    location?.let { listener.onLocationChange(it) }
                }
            }
            handler.postDelayed(this, LOCATION_UPDATE_INTERVAL)
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                // Permission denied, handle accordingly
            }
        }
    }

    private fun isGpsEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun showEnableGpsDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Enable GPS")
        builder.setMessage("GPS is required to access location. Please enable GPS.")
        builder.setPositiveButton("Enable") { dialog, _ ->
            dialog.dismiss()
            requestLocationUpdates()
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun getLastKnownLocation(listener: (Location?) -> Unit) {
        if (isLocationPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener(activity) { location ->
                    listener(location)
                }
        }
    }

    fun setOnLocationChangeListener(listener: OnLocationChangeListener) {
        onLocationChangeListener = listener
    }

    interface OnLocationChangeListener {
        fun onLocationChange(location: Location)
    }
}
