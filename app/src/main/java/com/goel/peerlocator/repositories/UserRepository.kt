package com.goel.peerlocator.repositories

import com.goel.peerlocator.listeners.UserDataListener
import com.goel.peerlocator.models.UserModel
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.database.Database
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

open class UserRepository {

    companion object {
        val instance = UserRepository()
    }

    private val userRef = Firebase.firestore.collection(Constants.USERS)

    fun signIn (user : FirebaseUser, listener: UserDataListener) {
        var url = Constants.DEFAULT_IMAGE_URL
        var name = ""
        user.displayName?.let { name = it }
        user.photoUrl?.let { url = it.toString() }
        val model = UserModel(documentReference = userRef.document(user.uid), uid = user.uid,
            name = name, imageUrl = url, email = user.email, phoneNumber = user.phoneNumber)
        Database.signIn(model, listener)
    }

    fun createProfile (name: String, listener: UserDataListener) {
        Database.currentUser.name = name
        Database.signIn(Database.currentUser, listener)
    }

}