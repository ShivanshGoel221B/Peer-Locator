package com.goel.peerlocator.listeners

import com.goel.peerlocator.models.UnknownUserModel

interface BlockListener {
    fun noBlockFound()
    fun onBlockListUpdated(model: UnknownUserModel)
    fun onBlocked (name: String)
    fun onUnblocked()
    fun onNetworkError ()
}