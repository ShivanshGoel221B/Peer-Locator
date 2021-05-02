package com.goel.peerlocator.models

import com.google.firebase.firestore.DocumentReference

data class InviteModel (val reference: DocumentReference, val name : String = "", val photoUrl : String = "", val timeStamp : String = "")