package com.goel.peerlocator.listeners

import com.goel.peerlocator.models.FriendModel
import com.google.firebase.firestore.DocumentReference

interface NewCircleListener {
    fun onCreationSuccessful (reference : DocumentReference)
    fun membersAdditionSuccessful ()
    fun photoUploaded (url : String)
    fun onMembersAdded (list : Array<FriendModel>)
    fun onError ()
}