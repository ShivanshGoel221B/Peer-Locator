package com.goel.peerlocator.listeners

interface EditCircleListener {
    fun onCreationSuccessful ()
    fun membersAdditionSuccessful ()
    fun onInvitationSent (completedPercentage: Int, nextPercentage: Int)
    fun onError ()
}