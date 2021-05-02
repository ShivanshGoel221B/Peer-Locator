package com.goel.peerlocator.utils.location

import com.google.android.gms.maps.model.LatLng

interface LocationListener {
    fun onLocationReady (latLng: LatLng)
    fun onFriendMoved (latLng: LatLng)
}