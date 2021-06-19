package com.goel.peerlocator.repositories

import com.goel.peerlocator.listeners.AddFriendListener
import com.goel.peerlocator.listeners.EditCircleListener
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.listeners.InvitationListener
import com.goel.peerlocator.models.InviteModel
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.utils.firebase.database.InvitationDatabase
import com.google.firebase.firestore.DocumentReference


class InvitesRepository : UserRepository() {

    companion object {
        val instance = InvitesRepository()
    }

    fun getAllInvites (listener: GetListListener) {
        InvitationDatabase.instance.getAllInvites(listener)
    }

    fun sendInvitations (documentReference : DocumentReference, membersList : ArrayList<DocumentReference>, listener : EditCircleListener) {
        val membersUIds = ArrayList<String>()
        for (reference in membersList) {
            membersUIds.add(reference.id)
        }
        InvitationDatabase.instance.sendInvitations(documentReference, membersUIds, listener)
    }

    fun sendInvitation (recipient: UnknownUserModel, listener: AddFriendListener) {
        InvitationDatabase.instance.sendInvitation(recipient, listener)
    }

    fun getSentInvitations (documentReference: DocumentReference, listener: GetListListener) {
        InvitationDatabase.instance.getSentInvitations(documentReference, listener)
    }

    fun unSendInvitation (documentReference: DocumentReference,
                          model: UnknownUserModel,
                          listener: AddFriendListener) {
        InvitationDatabase.instance.unSendInvitation(documentReference, model, listener)
    }

    fun acceptInvitation (model: InviteModel, listener: InvitationListener) {
        InvitationDatabase.instance.checkSentInvitation(model, listener)
    }

    fun rejectInvitation (model: InviteModel, listener: InvitationListener) {
        InvitationDatabase.instance.rejectInvitation(model, listener)
    }
}