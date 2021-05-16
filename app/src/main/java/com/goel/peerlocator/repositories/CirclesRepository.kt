package com.goel.peerlocator.repositories

import android.widget.LinearLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.CirclesAdapter
import com.goel.peerlocator.listeners.EditCircleListener
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.utils.firebase.database.CirclesDatabase
import com.google.firebase.firestore.DocumentReference
import java.io.InputStream

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
        CirclesDatabase.instance.getAllCircles(circleList, circlesAdapter, shimmer, nothingFound)
    }

    fun createNewCircle (name: String, imageStream: InputStream?,
                         membersList: java.util.ArrayList<FriendModel>, listener: EditCircleListener) {
        val referencesList = ArrayList<DocumentReference>()
        membersList.forEach {
            referencesList.add(it.documentReference)
        }
        CirclesDatabase.instance.createNewCircle(name, imageStream, referencesList, listener)
    }
}