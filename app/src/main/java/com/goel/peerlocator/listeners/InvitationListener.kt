package com.goel.peerlocator.listeners

import com.goel.peerlocator.models.InviteModel

interface InvitationListener {
    fun onInvitationAccepted (model: InviteModel)
    fun onInvitationRejected (model: InviteModel)
    fun onError ()
}