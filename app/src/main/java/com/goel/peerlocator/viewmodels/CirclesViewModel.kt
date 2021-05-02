package com.goel.peerlocator.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.CirclesAdapter
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.repositories.CirclesRepository


class CirclesViewModel(application: Application) : AndroidViewModel(application) {

    val circleList : MutableLiveData<ArrayList<CircleModel>> = MutableLiveData()
    init {
        circleList.value = CirclesRepository.instance.circleList
    }

    fun getAllCircles (circlesAdapter: CirclesAdapter, shimmer: ShimmerFrameLayout) {
        CirclesRepository.instance.getAllCircles(circlesAdapter, shimmer)
    }

}