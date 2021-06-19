package com.goel.peerlocator.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.goel.peerlocator.listeners.AddFriendListener
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.repositories.InvitesRepository
import com.google.firebase.firestore.DocumentReference

class SentInvitationViewModel(application: Application) : AndroidViewModel(application) {

    val sentInvitationList = ArrayList<UnknownUserModel>()

    fun getSentInvitations(documentReference: DocumentReference, listener: GetListListener) {
        sentInvitationList.clear()
        InvitesRepository.instance.getSentInvitations (documentReference, listener)
    }

    fun removeInvitation(documentReference: DocumentReference,
                         model: UnknownUserModel,
                         listener: AddFriendListener) {
        InvitesRepository.instance.unSendInvitation(documentReference, model, listener)
    }
}