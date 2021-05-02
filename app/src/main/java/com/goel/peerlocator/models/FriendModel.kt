package com.goel.peerlocator.models

import android.location.Location
import com.google.firebase.firestore.DocumentReference

data class FriendModel (val friendReference : DocumentReference,
                        val uid : String = "",
                        val imageUrl : String = "",
                        val friendName : String = "",
                        val commonCirclesCount : Int = 0,
                        var currentLocation: Location? = null)