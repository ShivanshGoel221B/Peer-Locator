package com.goel.peerlocator.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.listeners.InvitationListener
import com.goel.peerlocator.models.InviteModel
import com.goel.peerlocator.repositories.InvitesRepository

class InvitesViewModel(application: Application) : AndroidViewModel(application) {

    val invitesList = ArrayList<InviteModel>()

    fun getAllInvites (listener: GetListListener) {
        invitesList.clear()
        InvitesRepository.instance.getAllInvites(listener)
    }

    fun acceptInvitation (model: InviteModel, listener: InvitationListener) {
        InvitesRepository.instance.acceptInvitation(model, listener)
    }

    fun rejectInvitation (model: InviteModel, listener: InvitationListener) {
        InvitesRepository.instance.rejectInvitation(model, listener)
    }
}