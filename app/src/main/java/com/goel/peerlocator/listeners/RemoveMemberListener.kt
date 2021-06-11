package com.goel.peerlocator.listeners

import com.goel.peerlocator.models.MemberModel

interface RemoveMemberListener {
    fun memberRemoved (member: MemberModel)
    fun onError ()
}