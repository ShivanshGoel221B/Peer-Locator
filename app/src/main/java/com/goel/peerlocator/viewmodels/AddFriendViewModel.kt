package com.goel.peerlocator.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.goel.peerlocator.models.UnknownUserModel

class AddFriendViewModel(application: Application) : AndroidViewModel(application) {

    val usersList = ArrayList<UnknownUserModel>()

    fun getAllUsers () {

    }
}