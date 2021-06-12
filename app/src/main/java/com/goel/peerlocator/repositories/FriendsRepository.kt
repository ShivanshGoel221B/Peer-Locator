package com.goel.peerlocator.repositories

import com.goel.peerlocator.listeners.EditFriendListener
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.utils.firebase.database.FriendsDatabase
import com.google.firebase.firestore.DocumentReference

class FriendsRepository : UserRepository() {

    companion object {
        val instance = FriendsRepository()
    }

    fun getAllFriends (listener: GetListListener) {
        FriendsDatabase.instance.getAllFriends(listener)
    }

    fun getFriendsList(addedMembers : ArrayList<FriendModel>, listener : GetListListener) {
        val addedIds = ArrayList<String>()
        addedMembers.forEach {
            addedIds.add(it.uid)
        }
        FriendsDatabase.instance.getAllFriends(addedIds, listener)
    }

    fun getUsers (listener: GetListListener) {
        FriendsDatabase.instance.getUsers(listener)
    }

    fun removeFriend (documentReference: DocumentReference, listener: EditFriendListener) {
        FriendsDatabase.instance.removeFriend(documentReference, listener)
    }

    fun blockFriend(documentReference: DocumentReference, listener: EditFriendListener) {
        FriendsDatabase.instance.blockFriend(documentReference, listener)
    }

    fun blockUser(documentReference: DocumentReference, listener: EditFriendListener) {
        FriendsDatabase.instance.blockUser(documentReference, listener)
    }
}