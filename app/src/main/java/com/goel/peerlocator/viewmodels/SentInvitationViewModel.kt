package com.goel.peerlocator.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.goel.peerlocator.listeners.AddFriendListener
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.repositories.InvitesRepository

class SentInvitationViewModel(application: Application) : AndroidViewModel(application) {

    val sentInvitationList = ArrayList<UnknownUserModel>()

    fun getSentInvitations(listener: GetListListener) {
        InvitesRepository.instance.getSentInvitations (listener)
    }

    fun removeInvitation(model: UnknownUserModel, listener: AddFriendListener) {
        InvitesRepository.instance.unSendInvitation(model, listener)
    }
}