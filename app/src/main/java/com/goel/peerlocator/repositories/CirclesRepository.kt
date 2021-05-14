package com.goel.peerlocator.repositories

import android.util.Log
import android.widget.LinearLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.CirclesAdapter
import com.goel.peerlocator.listeners.EditCircleListener
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.database.CirclesDatabase
import com.google.firebase.firestore.DocumentReference
import java.io.InputStream

class CirclesRepository : UserRepository () {

    companion object {
        val instance = CirclesRepository()
    }

    val circleList = ArrayList<CircleModel> ()
    private val circlesReference = fireStoreDatabase.collection(Constants.CIRCLES)

    fun getAllCircles (
        circlesAdapter: CirclesAdapter,
        shimmer: ShimmerFrameLayout,
        nothingFound: LinearLayout) {
        circleList.clear()
        CirclesDatabase.instance.getAllCircles(userRef, circleList, circlesAdapter, shimmer, nothingFound)
    }

    fun createNewCircle (name: String, imageStream: InputStream?,
                         membersList: java.util.ArrayList<FriendModel>, listener: EditCircleListener) {
        val uIds = ArrayList<String>()
        membersList.forEach {
            uIds.add(it.documentReference.id)
        }
        CirclesDatabase.instance.createNewCircle(circlesReference, name, imageStream, uIds, listener)
    }
}