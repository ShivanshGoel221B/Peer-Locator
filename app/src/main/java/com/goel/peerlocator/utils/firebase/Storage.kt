package com.goel.peerlocator.utils.firebase

import com.goel.peerlocator.listeners.EditCircleListener
import com.goel.peerlocator.listeners.ProfileDataListener
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.UserModel
import com.goel.peerlocator.utils.Constants
import com.google.firebase.firestore.DocumentReference
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

    fun uploadProfileImage(model : CircleModel, documentReference: DocumentReference,
                            inputStream: InputStream, initialMembers : ArrayList<DocumentReference>,
                           newMembers : ArrayList<DocumentReference>,
                           listener: EditCircleListener) {
        val uploadTask = profilePictureRef.child(model.uid).putStream(inputStream)
        uploadTask.addOnFailureListener { listener.onError() }
        uploadTask.addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener {url ->
                documentReference.update(Constants.DP, url.toString())
                    .addOnFailureListener { listener.onError() }
                    .addOnSuccessListener {
                        listener.onCreationSuccessful()
                        Database.addMembers(documentReference, initialMembers, newMembers, listener)
                }
            }
        }
    }
}