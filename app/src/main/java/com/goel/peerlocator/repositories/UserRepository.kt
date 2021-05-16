package com.goel.peerlocator.repositories

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

    fun signIn (user : FirebaseUser) {
        Database.signIn(user, userRef)
    }

}