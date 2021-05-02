package com.goel.peerlocator.models

import com.google.firebase.firestore.DocumentReference

data class UserModel (val documentReference: DocumentReference, val uid : String = "",
                      var displayName : String = "", var photoUrl : String = "",
                      val email : String?, val phoneNumber : String?,
                      var friendsCount : Long = 0, var circlesCount : Long = 0)