package com.goel.peerlocator.listeners

interface UserDataListener {
    fun newPhoneFound ()
    fun onUserCreated ()
    fun onError ()
}