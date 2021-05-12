package com.goel.peerlocator.repositories

import android.widget.LinearLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.FriendsAdapter
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.utils.firebase.Database
import com.goel.peerlocator.models.FriendModel

class FriendsRepository : UserRepository() {

    companion object {
        val instance = FriendsRepository()
    }

    val friendsList = ArrayList<FriendModel> ()

    fun getAllFriends (friendsAdapter: FriendsAdapter, shimmer: ShimmerFrameLayout, nothingFound : LinearLayout) {
        friendsList.clear()
        Database.getAllFriends(userRef, friendsList, friendsAdapter, shimmer, nothingFound)
    }

    fun getFriendsList(listener : GetListListener) {
        Database.getAllFriends(listener)
    }
}