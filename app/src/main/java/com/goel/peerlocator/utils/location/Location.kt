package com.goel.peerlocator.utils.location

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.utils.firebase.Database
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object Location {

    const val FINE = Manifest.permission.ACCESS_FINE_LOCATION
    const val COARSE = Manifest.permission.ACCESS_COARSE_LOCATION
    @RequiresApi(Build.VERSION_CODES.Q)
    const val BACKGROUND = Manifest.permission.ACCESS_BACKGROUND_LOCATION

    var currentLocation : Location? = null
    var currentFriend : FriendModel? = null
    var friendLocation : Location? = Location("")
    var locationListener : LocationListener? = null

    private const val LOC = "last_locations"
    private const val LAT = "lat"
    private const val LON = "lon"


    private val friendsLatitudeListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            friendLocation?.latitude = snapshot.getValue(Double::class.java) as Double
            currentFriend?.currentLocation = friendLocation
            locationListener?.onFriendMoved(LatLng(friendLocation?.latitude!!, friendLocation?.longitude!!))
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    private val friendsLongitudeListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            friendLocation?.longitude = snapshot.getValue(Double::class.java) as Double
            currentFriend?.currentLocation = friendLocation
            locationListener?.onFriendMoved(LatLng(friendLocation?.latitude!!, friendLocation?.longitude!!))
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun getFriendLocation () {
        FirebaseDatabase.getInstance().reference.child(LOC).child(currentFriend?.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val lat = snapshot.child(LAT).getValue(Double::class.java) as Double
                        val lon = snapshot.child(LON).getValue(Double::class.java) as Double
                        locationListener?.onLocationReady(LatLng(lat, lon))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    fun updateMyLocation(location: Location?) {
        currentLocation = location
        val ref = FirebaseDatabase.getInstance().reference.child(LOC).child(Database.currentUser!!.uid)
        ref.child(LAT).setValue(location?.latitude)
        ref.child(LON).setValue(location?.longitude)
    }

    fun setListeners (friend : FriendModel) {
        val lat = FirebaseDatabase.getInstance().reference.child(LOC).child(friend.uid).child(LAT)
        val lon = FirebaseDatabase.getInstance().reference.child(LOC).child(friend.uid).child(LON)

        lat.addValueEventListener(friendsLatitudeListener)
        lon.addValueEventListener(friendsLongitudeListener)
    }

    fun removeListeners (friend: FriendModel) {
        FirebaseDatabase.getInstance().reference.child(LOC).child(friend.uid)
            .child(LAT).removeEventListener(friendsLatitudeListener)
        FirebaseDatabase.getInstance().reference.child(LOC).child(friend.uid)
            .child(LON).removeEventListener(friendsLongitudeListener)
    }
}