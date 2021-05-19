package com.goel.peerlocator.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.repositories.FriendsRepository

class FriendsViewModel(application: Application) : AndroidViewModel(application) {

    val friendsList = ArrayList<FriendModel>()

    fun getAllFriends (listener: GetListListener) {
        friendsList.clear()
        FriendsRepository.instance.getAllFriends(listener)
    }

}