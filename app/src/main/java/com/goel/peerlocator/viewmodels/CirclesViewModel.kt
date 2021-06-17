package com.goel.peerlocator.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.repositories.CirclesRepository


class CirclesViewModel(application: Application) : AndroidViewModel(application) {

    val circleList = ArrayList<CircleModel>()

    fun getAllCircles (listener: GetListListener) {
        circleList.clear()
        CirclesRepository.instance.getAllCircles(listener)
    }

}