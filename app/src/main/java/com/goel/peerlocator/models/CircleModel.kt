package com.goel.peerlocator.models

import com.google.firebase.firestore.DocumentReference

data class CircleModel (val documentReference : DocumentReference,
                        val uid : String = "",
                        val imageUrl : String = "",
                        val name : String = "",
                        val adminReference :DocumentReference,
                        var memberCount : Int = 1)