package com.goel.peerlocator.utils.firebase

import com.goel.peerlocator.utils.Constants
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

object Storage {
    private val storage = Firebase.storage.reference
    private val profilePictureRef = storage.child(Constants.PROFILE_PICTURES)
}