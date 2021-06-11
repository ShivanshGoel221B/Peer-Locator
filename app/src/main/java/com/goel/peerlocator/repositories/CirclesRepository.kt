package com.goel.peerlocator.repositories

import android.widget.LinearLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.CirclesAdapter
import com.goel.peerlocator.listeners.CircleDataListener
import com.goel.peerlocator.listeners.EditCircleListener
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.utils.firebase.database.CirclesDatabase
import com.google.firebase.firestore.DocumentReference
import java.io.InputStream

class CirclesRepository : UserRepository () {

    companion object {
        val instance = CirclesRepository()
    }

    fun getAllCircles (listener: GetListListener) {
        CirclesDatabase.instance.getAllCircles(listener)
    }

    fun createNewCircle (name: String, imageStream: InputStream?,
                         membersList: java.util.ArrayList<FriendModel>, listener: EditCircleListener) {
        val referencesList = ArrayList<DocumentReference>()
        membersList.forEach {
            referencesList.add(it.documentReference)
        }
        CirclesDatabase.instance.createNewCircle(name, imageStream, referencesList, listener)
    }

    fun getAllMembers (documentReference: DocumentReference, listener: CircleDataListener) {
        CirclesDatabase.instance.getAllMembers(documentReference, listener)
    }
}