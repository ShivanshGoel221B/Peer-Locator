package com.goel.peerlocator.listeners

import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.models.UnknownUserModel

interface GetListListener {
    fun onFriendRetrieved(friend: FriendModel)
    fun onCircleRetrieved(circle: CircleModel)
    fun onUserRetrieved(user: UnknownUserModel)
    fun foundEmptyList()
    fun onError ()
}