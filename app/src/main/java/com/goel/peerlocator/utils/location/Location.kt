package com.goel.peerlocator.utils.location

import android.location.Location
import com.goel.peerlocator.listeners.LocationListener
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.database.Database
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object Location {

    private var currentLocation : Location? = null
    var currentFriend : FriendModel? = null
    var friendLocation : Location? = Location("")
    var locationListener : LocationListener? = null

    private val friendsLatitudeListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists())
                try {
                    friendLocation?.latitude = snapshot.getValue(Double::class.java) as Double
                } catch (e: NullPointerException) {
                    friendLocation?.latitude = 0.0
                }
            else
                friendLocation?.latitude = 0.0
            currentFriend?.latitude = friendLocation!!.latitude
            locationListener?.onFriendMoved(LatLng(friendLocation?.latitude!!, friendLocation?.longitude!!))
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    private val friendsLongitudeListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists())
                try {
                    friendLocation?.longitude = snapshot.getValue(Double::class.java) as Double
                } catch (e: NullPointerException) {
                    friendLocation?.longitude = 0.0
                }
            else
                friendLocation?.longitude = 0.0
            currentFriend?.longitude = friendLocation!!.longitude

            locationListener?.onFriendMoved(LatLng(friendLocation?.latitude!!, friendLocation?.longitude!!))
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun getFriendLocation () {
        FirebaseDatabase.getInstance().reference.child(Constants.LOC).child(currentFriend?.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val latLng = try {
                            val lat = snapshot.child(Constants.LAT).value as Double
                            val lon = snapshot.child(Constants.LON).value as Double
                            LatLng(lat, lon)
                        } catch (e: NullPointerException) {
                            LatLng(0.0, 0.0)
                        }
                        locationListener?.onLocationReady(latLng)
                    }
                    else {
                        locationListener?.onLocationReady(LatLng(0.0, 0.0))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    fun updateMyLocation(location: Location?) {
        currentLocation = location
        val ref = FirebaseDatabase.getInstance().reference.child(Constants.LOC).child(Database.currentUser.uid)
        ref.child(Constants.LAT).setValue(location?.latitude)
        ref.child(Constants.LON).setValue(location?.longitude)
    }

    fun setListeners (friend : FriendModel) {
        val lat = FirebaseDatabase.getInstance().reference.child(Constants.LOC).child(friend.uid).child(Constants.LAT)
        val lon = FirebaseDatabase.getInstance().reference.child(Constants.LOC).child(friend.uid).child(Constants.LON)

        lat.addValueEventListener(friendsLatitudeListener)
        lon.addValueEventListener(friendsLongitudeListener)
    }

    fun removeListeners (friend: FriendModel) {
        FirebaseDatabase.getInstance().reference.child(Constants.LOC).child(friend.uid)
            .child(Constants.LAT).removeEventListener(friendsLatitudeListener)
        FirebaseDatabase.getInstance().reference.child(Constants.LOC).child(friend.uid)
            .child(Constants.LON).removeEventListener(friendsLongitudeListener)
    }
}