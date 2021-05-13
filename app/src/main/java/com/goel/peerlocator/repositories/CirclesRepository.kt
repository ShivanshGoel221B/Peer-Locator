package com.goel.peerlocator.repositories

import android.widget.LinearLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.CirclesAdapter
import com.goel.peerlocator.listeners.EditCircleListener
import com.goel.peerlocator.utils.firebase.Database
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.utils.Constants
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
        Database.getAllCircles(userRef, circleList, circlesAdapter, shimmer, nothingFound)
    }

    fun createNewCircle (name: String, imageStream: InputStream?,
                         membersList: java.util.ArrayList<FriendModel>, listener: EditCircleListener) {
        val referenceList = ArrayList<DocumentReference>()
        membersList.forEach {
            referenceList.add(it.documentReference)
        }
        Database.createNewCircle(circlesReference, name, imageStream, referenceList, listener)
    }
}