package com.goel.peerlocator.repositories

import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.InvitesAdapter
import com.goel.peerlocator.utils.firebase.Database
import com.goel.peerlocator.models.InviteModel


class InvitesRepository : UserRepository() {

    companion object {
        val instance = InvitesRepository()
    }

    val invitesList = ArrayList<InviteModel> ()

    fun getAllInvites (invitesAdapter: InvitesAdapter, shimmer: ShimmerFrameLayout) {
        invitesList.clear()
        Database.getAllInvites(fireStoreDatabase, invitesList, invitesAdapter, shimmer)
    }
}