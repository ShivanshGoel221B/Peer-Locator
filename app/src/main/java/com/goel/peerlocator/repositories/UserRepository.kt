package com.goel.peerlocator.repositories

import com.goel.peerlocator.utils.firebase.Database
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

open class UserRepository {

    companion object {
        val instance = UserRepository()
    }

    protected val fireStoreDatabase = Firebase.firestore
    protected val userRef = fireStoreDatabase.collection("users")

    fun signIn (user : FirebaseUser) {
        Database.signIn(user, userRef)
    }

}