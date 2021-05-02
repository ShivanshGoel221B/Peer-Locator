package com.goel.peerlocator.utils.location

import android.location.Location
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.Database
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
                friendLocation?.latitude = snapshot.getValue(Double::class.java) as Double
            else
                friendLocation?.latitude = 0.0
            currentFriend?.currentLocation = friendLocation
            locationListener?.onFriendMoved(LatLng(friendLocation?.latitude!!, friendLocation?.longitude!!))
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    private val friendsLongitudeListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists())
                friendLocation?.longitude = snapshot.getValue(Double::class.java) as Double
            else
                friendLocation?.longitude = 0.0
            currentFriend?.currentLocation = friendLocation
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
                        val lat = snapshot.child(Constants.LAT).getValue(Double::class.java) as Double
                        val lon = snapshot.child(Constants.LON).getValue(Double::class.java) as Double
                        locationListener?.onLocationReady(LatLng(lat, lon))
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
        val ref = FirebaseDatabase.getInstance().reference.child(Constants.LOC).child(Database.currentUser!!.uid)
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