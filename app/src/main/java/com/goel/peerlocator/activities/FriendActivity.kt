package com.goel.peerlocator.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.goel.peerlocator.R
import com.goel.peerlocator.databinding.ActivityFriendBinding
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.services.ServicesHandler
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.location.Location
import com.goel.peerlocator.listeners.LocationListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class FriendActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    companion object {
        lateinit var friend : FriendModel
    }
    private lateinit var mMap: GoogleMap
    private lateinit var binding : ActivityFriendBinding
    private lateinit var marker: MarkerOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        Location.locationListener = this
        marker = MarkerOptions()

        binding.friendName.text = friend.name

        marker.title(friend.name)

        Location.currentFriend = friend
        Location.setListeners(friend)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    private fun startMyLocation () {
        try {
            ServicesHandler.stopBackgroundLocation(this)
            if (ContextCompat.checkSelfPermission(applicationContext, Constants.FINE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(applicationContext, Constants.COARSE) == PackageManager.PERMISSION_GRANTED) {
                    ServicesHandler.startBackgroundLocation(this)
                }
            else {
                Toast.makeText(this, "Location Permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        } catch (e: SecurityException) {

        }
    }

    private fun goToLocation(latLng: LatLng) {
        marker.position(latLng)
        mMap.clear()
        if (latLng.latitude + latLng.longitude > 0.0) {
            mMap.addMarker(marker)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, Constants.DEFAULT_ZOOM))
        }
        else
            Toast.makeText(this, "Failed to get peer's location", Toast.LENGTH_SHORT).show()
    }

    private fun findMe () {
        val myLocation = mMap.myLocation
        if (myLocation == null)
            Toast.makeText(this, "Getting your Location", Toast.LENGTH_SHORT).show()
        myLocation?.let {
            if (checkGPS()) {
                val latLng = LatLng(it.latitude, it.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, Constants.DEFAULT_ZOOM))
            }
            else
                alertForGPS()
        }
    }

    private fun checkGPS(): Boolean {
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun alertForGPS() {
        AlertDialog.Builder(this)
            .setTitle("Turn On GPS")
            .setMessage("We recommend you to turn on GPS for better accuracy")
            .setPositiveButton("Settings") {dialog, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                dialog.dismiss()
            }.show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        Location.getFriendLocation()
        startMyLocation()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location Permission denied", Toast.LENGTH_SHORT).show()
            finish()
        }
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false

        findMe()
        binding.findMe.setOnClickListener {
            findMe()
        }

        binding.findFriend.setOnClickListener {
            goToLocation(LatLng(friend.latitude, friend.longitude))
        }
    }

    override fun onLocationReady(latLng: LatLng) {
        goToLocation(latLng)
    }

    override fun onFriendMoved(latLng: LatLng) {
        goToLocation(latLng)
    }

    override fun onDestroy() {
        super.onDestroy()
        Location.removeListeners(friend)
        val preferences = getSharedPreferences(Constants.PREFS, MODE_PRIVATE)
        val per = preferences.getBoolean(Constants.BACK_LOC, true)
        if (!per) {
            ServicesHandler.stopBackgroundLocation(this)
        }
    }
}