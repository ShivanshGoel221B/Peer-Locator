package com.goel.peerlocator.repositories

import android.widget.LinearLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.CirclesAdapter
import com.goel.peerlocator.listeners.NewCircleListener
import com.goel.peerlocator.utils.firebase.Database
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel

class CirclesRepository : UserRepository () {

    companion object {
        val instance = CirclesRepository()
    }

    val circleList = ArrayList<CircleModel> ()

    fun getAllCircles (
        circlesAdapter: CirclesAdapter,
        shimmer: ShimmerFrameLayout,
        nothingFound: LinearLayout) {
        circleList.clear()
        Database.getAllCircles(userRef, circleList, circlesAdapter, shimmer, nothingFound)
    }

    fun createNewCircle (circleModel: CircleModel, membersList : Array<FriendModel>, listener : NewCircleListener) {

    }
}