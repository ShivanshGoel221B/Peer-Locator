package com.goel.peerlocator.repositories

import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.CirclesAdapter
import com.goel.peerlocator.utils.firebase.Database
import com.goel.peerlocator.models.CircleModel

class CirclesRepository : UserRepository () {

    companion object {
        val instance = CirclesRepository()
    }

    val circleList = ArrayList<CircleModel> ()

    fun getAllCircles (circlesAdapter: CirclesAdapter, shimmer: ShimmerFrameLayout) {
        circleList.clear()
        Database.getAllCircles(userRef, circleList, circlesAdapter, shimmer)
    }
}