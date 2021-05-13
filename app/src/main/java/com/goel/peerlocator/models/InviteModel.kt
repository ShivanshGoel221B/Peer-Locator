package com.goel.peerlocator.models

import com.google.firebase.firestore.DocumentReference

data class InviteModel (val documentReference: DocumentReference,
                        val name : String = "",
                        val imageUrl : String = "",
                        val timeStamp : String = "")