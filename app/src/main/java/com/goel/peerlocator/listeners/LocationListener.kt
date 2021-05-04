package com.goel.peerlocator.listeners

import com.google.android.gms.maps.model.LatLng

interface LocationListener {
    fun onLocationReady (latLng: LatLng)
    fun onFriendMoved (latLng: LatLng)
}