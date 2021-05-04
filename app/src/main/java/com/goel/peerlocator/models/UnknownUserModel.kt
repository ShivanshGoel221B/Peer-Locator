package com.goel.peerlocator.models

import com.google.firebase.firestore.DocumentReference

data class UnknownUserModel (val documentReference: DocumentReference, val uid : String = "",
                             var displayName : String = "", var photoUrl : String = "")