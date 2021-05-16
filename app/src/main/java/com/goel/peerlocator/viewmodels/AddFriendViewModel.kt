package com.goel.peerlocator.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.repositories.FriendsRepository

class AddFriendViewModel(application: Application) : AndroidViewModel(application) {

    val usersList = ArrayList<UnknownUserModel>()

    fun getAllUsers (listener : GetListListener) {
        FriendsRepository.instance.getUsers(listener)
    }
}