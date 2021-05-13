package com.goel.peerlocator.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.goel.peerlocator.listeners.EditCircleListener
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.repositories.CirclesRepository
import java.io.InputStream
import java.util.ArrayList

class NewCircleViewModel(application: Application) : AndroidViewModel(application) {
    val membersList = ArrayList<FriendModel>()

    fun createCircle (name : String, imageStream: InputStream?, listener : EditCircleListener) {
        CirclesRepository.instance.createNewCircle(name, imageStream, membersList, listener)
    }
}