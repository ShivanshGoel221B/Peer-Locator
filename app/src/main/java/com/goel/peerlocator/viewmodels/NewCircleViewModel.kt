package com.goel.peerlocator.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.utils.firebase.Database
import java.util.ArrayList

class NewCircleViewModel(application: Application) : AndroidViewModel(application) {
    val membersList = ArrayList<FriendModel>()

}