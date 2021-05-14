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

    val friendsList = ArrayList<FriendModel> ()

    fun getAllFriends (friendsAdapter: FriendsAdapter, shimmer: ShimmerFrameLayout, nothingFound : LinearLayout) {
        friendsList.clear()
        FriendsDatabase.instance.getAllFriends(userRef, friendsList, friendsAdapter, shimmer, nothingFound)
    }

    fun getFriendsList(addedMembers : ArrayList<FriendModel>, listener : GetListListener) {
        val addedIds = ArrayList<String>()
        addedMembers.forEach {
            addedIds.add(it.uid)
        }
        FriendsDatabase.instance.getAllFriends(addedIds, listener)
    }
}