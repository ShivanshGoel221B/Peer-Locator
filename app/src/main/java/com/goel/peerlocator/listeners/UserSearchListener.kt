package com.goel.peerlocator.listeners

import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.models.UnknownUserModel

interface UserSearchListener {
    fun userFound (user : UnknownUserModel)
    fun friendFound (friend : FriendModel)
    fun blockedFound ()
    fun networkError ()
}