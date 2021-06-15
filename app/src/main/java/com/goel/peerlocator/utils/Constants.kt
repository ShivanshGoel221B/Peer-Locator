package com.goel.peerlocator.utils

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi

object Constants {

    const val PREFS = "com.goel.peerlocator.shared"
    const val BACK_LOC = "background_location_access"
    const val USERS = "users"

    const val DEV_PAGE = "dev_page"
    const val ABOUT = "about_us"
    const val TERMS = "terms"
    const val PRIVACY_POLICY = "privacy_policy"

    const val INACCESSIBLE = -1
    const val UNKNOWN = 0
    const val FRIEND = 1
    const val ME = 2
    const val INACCESSIBLE_NAME = "Inaccessible User"

    const val NAME = "name"
    const val EMAIL = "email"
    const val PHONE = "phone"
    const val DP = "profile_url"
    const val CIRCLES = "circles"
    const val ADMIN = "admin"
    const val MEMBERS = "members"
    const val FRIENDS = "friends"
    const val INVITES = "invites"
    const val SENT_INVITES = "sent_invites"
    const val ONLINE = "online"
    const val VISIBLE = "visible"
    const val BLOCKS = "blocks"
    const val BLOCKED_BY = "blocked_by"
    const val DEFAULT_IMAGE_URL = "null"

    const val LOC = "last_locations"
    const val LAT = "lat"
    const val LON = "lon"
    const val NANOSECONDS = "nanoseconds"
    const val SECONDS = "seconds"

    const val FINE = Manifest.permission.ACCESS_FINE_LOCATION
    const val COARSE = Manifest.permission.ACCESS_COARSE_LOCATION
    @RequiresApi(Build.VERSION_CODES.Q)
    const val BACKGROUND = Manifest.permission.ACCESS_BACKGROUND_LOCATION

    const val LOCATION_PERMISSION_REQUEST_CODE = 69
    const val READ_STORAGE_PERMISSION_REQUEST_CODE = 50

    const val IMAGE_REQUEST_CODE = 100
    const val PROFILE_PICTURES = "profile_pictures"
    val IMAGE_FILE_TYPES = arrayOf("image/jpg", "image/jpeg")
    const val MAX_IMAGE_SIZE = 1*1024*1024
    const val MAX_CIRCLE_SIZE = 20

    const val DEFAULT_ZOOM = 18f

    private const val validCharacters = "qwertyuiopasdfghjklzxcvbnm QWERTYUIOPASDFGHJKLZXCVBNM"
    private const val emptyNameError = "Name should not be empty"
    private const val nameLengthError = "Name should not contain more than 20 characters"
    private const val invalidCharacterError = "Name should contain alphabets blank spaces only"
    private const val invalidSpaceError = "Name should not start or end with a blank space"

    fun isNameValid (name : String) : HashMap<Boolean, String> {
        if (name.isEmpty())
            return hashMapOf(Pair(false, emptyNameError))
        if (name.last() == ' ' || name[0] == ' ')
            return hashMapOf(Pair(false, invalidSpaceError))
        if (name.length > 20)
            return hashMapOf(Pair(false, nameLengthError))
        for (character in name) {
            if (character !in validCharacters)
                return hashMapOf(Pair(false, invalidCharacterError))
        }
        return hashMapOf(Pair(true, ""))
    }

}