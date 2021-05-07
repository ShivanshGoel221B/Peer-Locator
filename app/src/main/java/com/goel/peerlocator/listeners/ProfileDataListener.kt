package com.goel.peerlocator.listeners

interface ProfileDataListener {
    fun friendsCountComplete (count : Long)
    fun circlesCountComplete (count : Long)
    fun onlineStatusFetched (online : Boolean)
    fun visibilityStatusFetched (visible : Boolean)
    fun onEmailFound (exist : Boolean, email : String)
    fun onPhoneFound (exist: Boolean, phone : String)
    fun onPhotoChanged (url : String)
    fun onNameChanged ( name : String)
    fun onOnlineStatusChanged (online : Boolean)
    fun onVisibilityStatusChanged (visible: Boolean)
    fun networkError ()
}