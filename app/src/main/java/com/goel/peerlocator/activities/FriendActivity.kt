package com.goel.peerlocator.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.goel.peerlocator.R
import com.goel.peerlocator.databinding.ActivityFriendBinding
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.services.ServicesHandler
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.Database
import com.goel.peerlocator.utils.location.Location
import com.goel.peerlocator.utils.location.LocationListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class FriendActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding : ActivityFriendBinding
    private lateinit var friend : FriendModel
    private lateinit var marker: MarkerOptions


    private val defaultZoom = 18f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        Location.locationListener = this
        marker = MarkerOptions()

        friend = Database.currentFriend!!

        binding.friendName.text = friend.friendName

        marker.title(friend.friendName)

        Location.currentFriend = friend
        Location.setListeners(friend)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    private fun startMyLocation () {
        try {
            ServicesHandler.stopBackgroundLocation(this)
            if (ContextCompat.checkSelfPermission(applicationContext, Constants.FINE) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(applicationContext, Constants.COARSE) == PackageManager.PERMISSION_GRANTED) {
                    ServicesHandler.startBackgroundLocation(this)
                }
            }
        } catch (e: SecurityException) {

        }
    }

    private fun goToLocation(latLng: LatLng) {
        marker.position(latLng)
        mMap.clear()
        mMap.addMarker(marker)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, defaultZoom))
    }

    private fun findMe (latLng: LatLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, defaultZoom))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        Location.getFriendLocation()
        startMyLocation()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false

        binding.findMe.setOnClickListener {
            val loc = mMap.myLocation
            loc?.let {
                findMe(LatLng(it.latitude, it.longitude))
            }
        }

        binding.findFriend.setOnClickListener {
            goToLocation(LatLng(friend.currentLocation!!.latitude, friend.currentLocation!!.longitude))
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