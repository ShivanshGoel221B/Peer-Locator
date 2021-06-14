package com.goel.peerlocator.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.job.JobParameters
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.goel.peerlocator.activities.MainActivity
import com.goel.peerlocator.activities.SplashActivity
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.location.Location
import com.google.android.gms.location.*

class BackgroundLocationServices : Service() {

    private val channelId = "LocationNotification"

    private val locationRequest: LocationRequest = LocationRequest.create()
    private var fusedLocationProviderClient : FusedLocationProviderClient? = null
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            Location.updateMyLocation(p0.lastLocation)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val message = "Peer Locator is accessing your location"
        val openAppIntent = Intent(this, SplashActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0,
                                                openAppIntent, 0)
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Access")
            .setContentText(message)
            .setSilent(true)
            .setSmallIcon(com.goel.peerlocator.R.drawable.ic_location)
            .setLargeIcon(BitmapFactory.decodeResource(resources, com.goel.peerlocator.R.mipmap.ic_launcher_foreground))
            .setContentIntent(pendingIntent)
            .build()

        startTracking()
        startForeground(1, notification)
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
    }

    private fun createNotificationChannel () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Notification channel",
                NotificationManager.IMPORTANCE_DEFAULT)

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startTracking() {
        locationRequest.interval = 1500
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

         fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)
        fusedLocationProviderClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

    }

}