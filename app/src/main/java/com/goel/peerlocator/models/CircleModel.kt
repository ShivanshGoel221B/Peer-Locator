package com.goel.peerlocator.models

import com.google.firebase.firestore.DocumentReference

data class CircleModel (val circleReference : DocumentReference,
                       val imageUrl : String = "",
                       val circleName : String = "",
                       val adminReference :DocumentReference,
                       val memberCount : Int = 1)