package com.goel.peerlocator.viewmodels

import android.app.Application
import android.widget.LinearLayout
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.FriendsAdapter
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.repositories.FriendsRepository

class FriendsViewModel(application: Application) : AndroidViewModel(application) {

    val friendsList : MutableLiveData<ArrayList<FriendModel>> = MutableLiveData()
    init {
        friendsList.value = FriendsRepository.instance.friendsList
    }

    fun getAllFriends (friendsAdapter: FriendsAdapter, shimmer: ShimmerFrameLayout, nothingFound : LinearLayout) {
        FriendsRepository.instance.getAllFriends(friendsAdapter, shimmer, nothingFound)
    }

}