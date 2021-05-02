package com.goel.peerlocator.utils.firebase

import com.google.firebase.firestore.DocumentReference

interface FriendDataListener {
    fun onCountComplete (circles : Long, friends : Long)
    fun onCommonCirclesComplete (commonCircleList : ArrayList<DocumentReference>)
    fun onFriendRemoved ()
    fun onFriendBlocked ()
}