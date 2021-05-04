package com.goel.peerlocator.utils

import android.Manifest

object Constants {

    const val PREFS = "com.goel.peerlocator.shared"
    const val BACK_LOC = "background_location_access"

    const val NAME = "name"
    const val EMAIL = "email"
    const val PHONE = "phone"
    const val DP = "profile_url"
    const val CIRCLES = "circles"
    const val ADMIN = "admin"
    const val MEMBERS = "members"
    const val FRIENDS = "friends"
    const val INVITES = "invites"
    const val ONLINE = "online"
    const val VISIBLE = "visible"

    const val LOC = "last_locations"
    const val LAT = "lat"
    const val LON = "lon"

    const val FINE = Manifest.permission.ACCESS_FINE_LOCATION
    const val COARSE = Manifest.permission.ACCESS_COARSE_LOCATION

    const val PERMISSION_REQUEST_CODE = 69

}