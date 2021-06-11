package com.goel.peerlocator.listeners

import com.goel.peerlocator.models.MemberModel

interface CircleDataListener {
    fun onMemberCountComplete (members : Long)
    fun onMemberRetrieved (member: MemberModel)
    fun onError ()
}