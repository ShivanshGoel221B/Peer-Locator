package com.goel.peerlocator.utils.firebase.storage

import com.goel.peerlocator.listeners.EditCircleListener
import com.goel.peerlocator.listeners.ProfileDataListener
import com.goel.peerlocator.models.UserModel
import com.goel.peerlocator.repositories.InvitesRepository
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.database.Database
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

    fun uploadProfileImage(
        documentReference: DocumentReference, inputStream: InputStream,
        membersList : ArrayList<DocumentReference>, listener: EditCircleListener
    ) {
        val uploadTask = profilePictureRef.child(documentReference.id).putStream(inputStream)
        uploadTask.addOnFailureListener { listener.onError() }
        uploadTask.addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener {url ->
                documentReference.update(Constants.DP, url.toString())
                    .addOnFailureListener { listener.onError() }
                    .addOnSuccessListener {
                        listener.onCreationSuccessful()
                        InvitesRepository.instance.sendInvitations(documentReference, membersList, listener)
                }
            }
        }
    }
}