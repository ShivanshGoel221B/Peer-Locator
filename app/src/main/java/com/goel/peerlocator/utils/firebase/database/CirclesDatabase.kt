package com.goel.peerlocator.utils.firebase.database

import com.goel.peerlocator.listeners.CircleDataListener
import com.goel.peerlocator.listeners.EditCircleListener
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.repositories.InvitesRepository
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.storage.Storage
import com.google.firebase.firestore.DocumentReference
import java.io.InputStream

class CirclesDatabase : Database() {

    companion object {
        val instance = CirclesDatabase()
    }

    fun getAllCircles (listener: GetListListener) {
        var circleArray = java.util.ArrayList<DocumentReference>()

        userRef.document(currentUser!!.uid).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    try {
                        circleArray = it[Constants.CIRCLES] as ArrayList<DocumentReference>
                    } catch (e: NullPointerException) {}
                    currentUser?.circlesCount = circleArray.size.toLong()
                    addToList(circleArray, listener)
                }
            }
            .addOnFailureListener {
                listener.onError()
            }
    }

    // Adds circles to the list
    private fun addToList (circleArray: ArrayList<DocumentReference>, listener: GetListListener) {
        if (circleArray.isEmpty())
            listener.foundEmptyList()
        else {
            for (circle in circleArray) {
                circle.get().addOnFailureListener { listener.onError() }
                    .addOnSuccessListener {
                        var membersCount = 0
                        try {
                            membersCount = (it[Constants.MEMBERS] as ArrayList<DocumentReference>).size
                        } catch (e: java.lang.NullPointerException) {
                        }
                        val model = CircleModel(name = it[Constants.NAME].toString(),
                            documentReference = it.reference,
                            imageUrl = it[Constants.DP].toString(),
                            adminReference = it[Constants.ADMIN] as DocumentReference,
                            memberCount = membersCount)
                            listener.onCircleRetrieved(model)
                    }
            }
        }
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
        name: String,
        imageStream: InputStream?,
        membersList: ArrayList<DocumentReference>,
        listener: EditCircleListener
    ) {
        val reference = circlesReference.document()

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
                                    InvitesRepository.instance.sendInvitations(reference, membersList, listener)
                                }
                                else {
                                    Storage.uploadProfileImage(reference, imageStream, membersList, listener)
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