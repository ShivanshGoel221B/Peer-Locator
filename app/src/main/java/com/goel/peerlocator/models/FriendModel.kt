package com.goel.peerlocator.models

import com.google.firebase.firestore.DocumentReference

data class FriendModel (val friendReference : DocumentReference,
                        val uid : String = "",
                        val imageUrl : String = "",
                        var friendName : String = "",
                        var commonCirclesCount : Int = 0,
                        var latitude : Double = 0.0,
                        var longitude : Double = 0.0)