package com.goel.peerlocator.viewmodels

import android.app.Application
import android.widget.LinearLayout
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.InvitesAdapter
import com.goel.peerlocator.models.InviteModel
import com.goel.peerlocator.repositories.InvitesRepository

class InvitesViewModel(application: Application) : AndroidViewModel(application) {

    val invitesList : MutableLiveData<ArrayList<InviteModel>> = MutableLiveData()

    init {
        invitesList.value = InvitesRepository.instance.invitesList
    }

    fun getAllInvites (invitesAdapter: InvitesAdapter, shimmer: ShimmerFrameLayout, nothingFound : LinearLayout) {
        InvitesRepository.instance.getAllInvites(invitesAdapter, shimmer, nothingFound)
    }
}