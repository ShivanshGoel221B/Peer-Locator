package com.goel.peerlocator.utils.firebase

import com.goel.peerlocator.listeners.ProfileDataListener
import com.goel.peerlocator.models.UserModel
import com.goel.peerlocator.utils.Constants
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.InputStream

object Storage {
    private val storage = Firebase.storage.reference
    private val profilePictureRef = storage.child(Constants.PROFILE_PICTURES)

    fun uploadProfileImage (model : UserModel, inputStream: InputStream, listener: ProfileDataListener) {

        val uploadTask = profilePictureRef.child(model.uid).putStream(inputStream)

        uploadTask.addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener {url ->
                listener.onPhotoChanged(url.toString())
                Database.changeImageUrl(model.documentReference, url.toString())
            }
        }
    }
}