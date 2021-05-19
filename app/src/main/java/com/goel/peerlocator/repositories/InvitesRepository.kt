package com.goel.peerlocator.repositories

import android.widget.LinearLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.InvitesAdapter
import com.goel.peerlocator.listeners.AddFriendListener
import com.goel.peerlocator.listeners.EditCircleListener
import com.goel.peerlocator.listeners.GetListListener
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

    fun getSentInvitations (listener: GetListListener) {
        InvitationDatabase.instance.getSentInvitations(listener)
    }

    fun unSendInvitation (model: UnknownUserModel, listener: AddFriendListener) {
        InvitationDatabase.instance.unSendInvitation(model, listener)
    }
}