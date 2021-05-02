package com.goel.peerlocator.services

import android.Manifest
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.goel.peerlocator.utils.location.Location
import com.google.android.gms.location.*

class BackgroundLocationServices : JobService(){

    private val locationRequest: LocationRequest = LocationRequest.create()
    private var fusedLocationProviderClient : FusedLocationProviderClient? = null
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            Location.updateMyLocation(p0.lastLocation)
        }
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 500
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

         fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return true
        fusedLocationProviderClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        return true
    }

}