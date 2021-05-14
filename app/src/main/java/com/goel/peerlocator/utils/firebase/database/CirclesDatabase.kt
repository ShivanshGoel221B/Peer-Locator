package com.goel.peerlocator.utils.firebase.database

import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.CirclesAdapter
import com.goel.peerlocator.listeners.CircleDataListener
import com.goel.peerlocator.listeners.EditCircleListener
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.storage.Storage
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import java.io.InputStream

class CirclesDatabase : Database() {

    companion object {
        val instance = CirclesDatabase()
    }

    fun getAllCircles (
        userRef: CollectionReference, circleList: java.util.ArrayList<CircleModel>,
        circlesAdapter: CirclesAdapter, shimmer: ShimmerFrameLayout, nothingFound: LinearLayout
    ) {
        var circleArray = java.util.ArrayList<DocumentReference>()

        userRef.document(currentUser!!.uid).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    try {
                        circleArray = it[Constants.CIRCLES] as ArrayList<DocumentReference>
                    } catch (e: NullPointerException) {}
                    currentUser?.circlesCount = circleArray.size.toLong()
                    addToList(circleArray, circleList, circlesAdapter, shimmer, nothingFound)
                }
            }
            .addOnFailureListener {
                Log.d("Error", it.toString())
            }
    }

    // Adds circles to the list
    private fun addToList (circleArray: ArrayList<DocumentReference>, list: ArrayList<CircleModel>,
                           circlesAdapter: CirclesAdapter, shimmer: ShimmerFrameLayout, nothingFound: LinearLayout) {

        if (circleArray.isEmpty())
            nothingFound.visibility = View.VISIBLE
        else
            nothingFound.visibility = View.GONE
        for (circle in circleArray) {
            circle.get()
                .addOnSuccessListener {
                    var membersCount = 0
                    try {
                        membersCount = (it[Constants.MEMBERS] as ArrayList<DocumentReference>).size
                    } catch (e: java.lang.NullPointerException) {
                    }
                    val newCircle = CircleModel(name = it[Constants.NAME].toString(),
                        documentReference = it.reference,
                        imageUrl = it[Constants.DP].toString(),
                        adminReference = it[Constants.ADMIN] as DocumentReference,
                        memberCount = membersCount)

                    list.add(newCircle)
                    circlesAdapter.notifyDataSetChanged()
                    shimmer.visibility = View.GONE
                    shimmer.stopShimmerAnimation()
                }
                .addOnFailureListener {
                    Log.d("Error", it.toString())
                }
        }
        shimmer.visibility = View.GONE
        shimmer.stopShimmerAnimation()
    }

    fun getCircleInfo(listener: CircleDataListener, circleReference: DocumentReference) {
        circleReference.get().addOnSuccessListener {
            if (it.exists()) {
                var membersList = ArrayList<DocumentReference>()
                try {
                    membersList = it[Constants.MEMBERS] as ArrayList<DocumentReference>
                } catch (e : NullPointerException){}
                listener.onMemberCountComplete(membersList.size.toLong())
                listener.onMembersRetrieved(membersList)
            }
        }
    }


    //Create/Edit new Circle
    fun createNewCircle (
        circleReference: CollectionReference,
        name: String,
        imageStream: InputStream?,
        membersIdList: ArrayList<String>,
        listener: EditCircleListener
    ) {
        val reference = circleReference.document()

        val circleMap = HashMap<String, Any>()
        circleMap[Constants.NAME] = name
        circleMap[Constants.ADMIN] = currentUserRef
        circleMap[Constants.DP] = Constants.DEFAULT_IMAGE_URL
        circleMap[Constants.MEMBERS] = arrayListOf(currentUserRef)

        reference.set(circleMap).addOnFailureListener { listener.onError() }
            .addOnSuccessListener {
                var initialCircles = ArrayList<DocumentReference>()
                currentUserRef.get().addOnFailureListener { listener.onError() }
                    .addOnSuccessListener {myDocument->
                        try {
                            initialCircles = myDocument[Constants.CIRCLES] as ArrayList<DocumentReference>
                        }catch (e:NullPointerException){}
                        initialCircles.add(reference)
                        currentUserRef.update(Constants.CIRCLES, initialCircles)
                            .addOnFailureListener { listener.onError() }
                            .addOnSuccessListener {
                                if (imageStream == null) {
                                    listener.onCreationSuccessful()
                                    InvitationDatabase.instance.sentInvitations(reference, membersIdList, listener)
                                }
                                else {
                                    Storage.uploadProfileImage(reference, imageStream, membersIdList, listener)
                                }
                            }
                    }
            }
    }

    fun addMembers (documentReference: DocumentReference, initialMembers : ArrayList<DocumentReference>,
                    newMembers : ArrayList<DocumentReference>, listener: EditCircleListener) {
        newMembers.forEach {
            initialMembers.add(it)
        }
        documentReference.update(Constants.MEMBERS, initialMembers)
            .addOnFailureListener { listener.onError() }
            .addOnSuccessListener { listener.membersAdditionSuccessful() }
    }
}