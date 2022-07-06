package com.example.yandexmapway.geo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.yandexmapway.MainActivity
import com.example.yandexmapway.viewmodel.MainViewModel
import com.google.android.gms.location.*
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
@RequiresApi(Build.VERSION_CODES.N)
class GeoTools(private val activity: MainActivity, private val viewModel: MainViewModel) {

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private val PERMISSION_ID = 44

    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object

                requestNewLocationData()
                mFusedLocationClient?.lastLocation
                    ?.addOnCompleteListener { task ->
                        val location = task.result
                        if (location == null) {
                            requestNewLocationData()
                        } else {
                            viewModel.lanLon.value = LatLon(location.latitude, location.longitude)
                        }
                    }
            } else {
                Toast.makeText(activity, "Please turn on your location...", Toast.LENGTH_LONG)
                    .show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivity(intent)
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 5
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            viewModel.lanLon.value = LatLon(mLastLocation!!.latitude, mLastLocation.longitude)
        }
    }

    // method to check for permissions
    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            activity, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), PERMISSION_ID
        )
    }

    // method to check
    // if location is enabled
    private fun isLocationEnabled(): Boolean {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

}
