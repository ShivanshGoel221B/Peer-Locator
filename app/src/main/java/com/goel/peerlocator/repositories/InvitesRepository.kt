package com.goel.peerlocator.repositories

import android.widget.LinearLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.InvitesAdapter
import com.goel.peerlocator.models.InviteModel
import com.goel.peerlocator.utils.firebase.database.InvitationDatabase


class InvitesRepository : UserRepository() {

    companion object {
        val instance = InvitesRepository()
    }

    val invitesList = ArrayList<InviteModel> ()

    fun getAllInvites (invitesAdapter: InvitesAdapter, shimmer: ShimmerFrameLayout, nothingFound : LinearLayout) {
        invitesList.clear()
        InvitationDatabase.instance.getAllInvites(fireStoreDatabase, invitesList, invitesAdapter, shimmer, nothingFound)
    }
}