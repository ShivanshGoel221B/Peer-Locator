package com.goel.peerlocator.repositories

import android.widget.LinearLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.FriendsAdapter
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.utils.firebase.database.FriendsDatabase

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
}