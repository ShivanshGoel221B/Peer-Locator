package com.goel.peerlocator.models

import com.google.firebase.firestore.DocumentReference

data class MemberModel (val documentReference: DocumentReference,
                        val uid: String,
                        val name: String,
                        val imageUrl: String,
                        val flag: Int)