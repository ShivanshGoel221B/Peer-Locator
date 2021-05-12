package com.goel.peerlocator.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.repositories.FriendsRepository


class AddMembersViewModel(application: Application) : AndroidViewModel(application) {
    val friendList = ArrayList<FriendModel>()
    val selectedList = ArrayList<FriendModel>()
    var addedCount = selectedList.size

    fun getFriendsList (addedMembers : ArrayList<FriendModel>, listener : GetListListener) {
        friendList.clear()
        FriendsRepository.instance.getFriendsList(addedMembers, listener)
    }
}