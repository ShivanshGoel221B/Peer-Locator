package com.goel.peerlocator.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.goel.peerlocator.listeners.AddFriendListener
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.repositories.FriendsRepository
import com.goel.peerlocator.repositories.InvitesRepository

class AddFriendViewModel(application: Application) : AndroidViewModel(application) {

    val usersList = ArrayList<UnknownUserModel>()

    fun getAllUsers (listener : GetListListener) {
        FriendsRepository.instance.getUsers(listener)
    }

    fun sendInvitation (position: Int, listener: AddFriendListener) {
        val model = usersList[position]
        InvitesRepository.instance.sendInvitation(model, listener)
    }
}