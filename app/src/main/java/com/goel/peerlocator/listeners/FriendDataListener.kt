package com.goel.peerlocator.listeners

import com.google.firebase.firestore.DocumentReference

interface FriendDataListener {
    fun onCountComplete (circles : Long, friends : Long)
    fun onCommonCirclesComplete (commonCircleList : ArrayList<DocumentReference>)
}