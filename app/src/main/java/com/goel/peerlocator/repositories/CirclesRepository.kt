package com.goel.peerlocator.repositories

import com.goel.peerlocator.listeners.CircleDataListener
import com.goel.peerlocator.listeners.EditCircleListener
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.listeners.RemoveMemberListener
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.models.MemberModel
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

    fun removeMember (documentReference: DocumentReference, member: MemberModel, listener: RemoveMemberListener) {
        CirclesDatabase.instance.removeMember(documentReference, member, listener)
    }

    fun leaveCircle(
        documentReference: DocumentReference,
        isAdmin: Boolean,
        listener: RemoveMemberListener) {
        CirclesDatabase.instance.leaveCircle(documentReference, isAdmin, listener)
    }
}