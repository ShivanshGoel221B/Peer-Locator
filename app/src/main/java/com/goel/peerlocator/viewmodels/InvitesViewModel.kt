package com.goel.peerlocator.viewmodels

import android.app.Application
import android.widget.LinearLayout
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.InvitesAdapter
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.models.InviteModel
import com.goel.peerlocator.repositories.InvitesRepository

class InvitesViewModel(application: Application) : AndroidViewModel(application) {

    val invitesList = ArrayList<InviteModel>()

    fun getAllInvites (listener: GetListListener) {
        invitesList.clear()
        InvitesRepository.instance.getAllInvites(listener)
    }
}