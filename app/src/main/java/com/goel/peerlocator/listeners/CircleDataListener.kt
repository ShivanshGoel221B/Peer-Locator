package com.goel.peerlocator.listeners

import com.google.firebase.firestore.DocumentReference

interface CircleDataListener {
    fun onMemberCountComplete (members : Long)
    fun onMembersRetrieved (references : ArrayList<DocumentReference>)
}