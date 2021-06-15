package com.goel.peerlocator.models

import com.google.firebase.firestore.DocumentReference

data class LocationMemberModel (val documentReference : DocumentReference,
                                val uid : String = "",
                                val imageUrl : String = "",
                                var name : String = "",
                                var latitude : Double = 0.0,
                                var longitude : Double = 0.0)