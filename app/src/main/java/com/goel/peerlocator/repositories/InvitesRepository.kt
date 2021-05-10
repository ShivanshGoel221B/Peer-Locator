package com.goel.peerlocator.repositories

import android.widget.LinearLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.InvitesAdapter
import com.goel.peerlocator.utils.firebase.Database
import com.goel.peerlocator.models.InviteModel


class InvitesRepository : UserRepository() {

    companion object {
        val instance = InvitesRepository()
    }

    val invitesList = ArrayList<InviteModel> ()

    fun getAllInvites (invitesAdapter: InvitesAdapter, shimmer: ShimmerFrameLayout, nothingFound : LinearLayout) {
        invitesList.clear()
        Database.getAllInvites(fireStoreDatabase, invitesList, invitesAdapter, shimmer, nothingFound)
    }
}