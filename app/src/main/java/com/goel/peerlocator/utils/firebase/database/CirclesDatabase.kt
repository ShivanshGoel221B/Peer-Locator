package com.goel.peerlocator.utils.firebase.database

import com.goel.peerlocator.listeners.*
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.InviteModel
import com.goel.peerlocator.models.MemberModel
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

        userRef.document(currentUser.uid).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    try {
                        circleArray = it[Constants.CIRCLES] as ArrayList<DocumentReference>
                    } catch (e: NullPointerException) {}
                    currentUser.circlesCount = circleArray.size.toLong()
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

    fun getAllMembers (documentReference: DocumentReference, listener: CircleDataListener) {
        var members = ArrayList<DocumentReference>()
        val inAccessible = ArrayList<String>()
        documentReference.get().addOnFailureListener { listener.onError() }
            .addOnSuccessListener {
                try {
                    members = it[Constants.MEMBERS] as ArrayList<DocumentReference>
                } catch (e: NullPointerException){}

                currentUserRef.get().addOnFailureListener { listener.onError() }
                    .addOnSuccessListener {me->
                        val myFriends = ArrayList<String>()
                        // Collect Friends
                        try {
                            val friendReferences = me[Constants.FRIENDS] as ArrayList<DocumentReference>
                            for (friend in friendReferences) {
                                myFriends.add(friend.id)
                            }
                        } catch (e: NullPointerException){}

                        //Collect Blocks
                        try {
                            val blocks = me[Constants.BLOCKS] as ArrayList<DocumentReference>
                            for (block in blocks) {
                                inAccessible.add(block.id)
                            }
                        } catch (e: java.lang.NullPointerException) {}

                        try {
                            val blocks = me[Constants.BLOCKED_BY] as ArrayList<DocumentReference>
                            for (block in blocks) {
                                inAccessible.add(block.id)
                            }
                        } catch (e: java.lang.NullPointerException) {}


                        createMemberModels (members, myFriends, inAccessible, listener)
                    }
            }
    }

    private fun createMemberModels (
        membersList: ArrayList<DocumentReference>,
        myFriends: ArrayList<String>,
        inAccessible: ArrayList<String>,
        listener: CircleDataListener
    ) {
        membersList.forEach {
            it.get().addOnFailureListener { listener.onError() }
                .addOnSuccessListener {member->
                    if (member.id == currentUser.uid) {
                        val name = "You"
                        val url = currentUser.imageUrl
                        val flag = Constants.ME
                        val model = MemberModel(documentReference = currentUserRef, uid = currentUser.uid,
                                    name = name, imageUrl = url, flag = flag)
                        listener.onMemberRetrieved(model)
                    }
                    else if (member.id in myFriends) {
                        val name = member[Constants.NAME].toString()
                        val url = member[Constants.DP].toString()
                        val flag = Constants.FRIEND
                        val model = MemberModel(documentReference = member.reference, uid = member.id,
                            name = name, imageUrl = url, flag = flag)
                        listener.onMemberRetrieved(model)
                    }
                    else if (member.id !in inAccessible) {
                        val visible = member[Constants.VISIBLE] as Boolean
                        if (visible) {
                            val name = member[Constants.NAME].toString()
                            val url = member[Constants.DP].toString()
                            val flag = Constants.UNKNOWN
                            val model = MemberModel(documentReference = member.reference, uid = member.id,
                                name = name, imageUrl = url, flag = flag)
                            listener.onMemberRetrieved(model)
                        }
                        else {
                            val name = Constants.INACCESSIBLE_NAME
                            val url = Constants.DEFAULT_IMAGE_URL
                            val flag = Constants.INACCESSIBLE

                            val model = MemberModel(documentReference = member.reference, uid = member.id,
                                name = name, imageUrl = url, flag = flag)
                            listener.onMemberRetrieved(model)
                        }
                    }
                    else {
                        val name = Constants.INACCESSIBLE_NAME
                        val url = Constants.DEFAULT_IMAGE_URL
                        val flag = Constants.INACCESSIBLE

                        val model = MemberModel(documentReference = member.reference, uid = member.id,
                            name = name, imageUrl = url, flag = flag)
                        listener.onMemberRetrieved(model)
                    }
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

    fun addMember (model: InviteModel, listener: InvitationListener) {
        val documentReference = model.documentReference
        var membersList = ArrayList<DocumentReference>()
        documentReference.get().addOnFailureListener { listener.onError() }
            .addOnSuccessListener { circle->
                try {
                    membersList = circle[Constants.MEMBERS] as ArrayList<DocumentReference>
                } catch (e: NullPointerException) {}
                membersList.add(currentUserRef)
                documentReference.update(Constants.MEMBERS, membersList)
                    .addOnFailureListener { listener.onError() }
                    .addOnSuccessListener { addCircle(model, listener) }
            }
    }

    fun removeMember (circleReference: DocumentReference, memberModel: MemberModel,
                      listener: RemoveMemberListener) {
        circleReference.get()
            .addOnFailureListener { listener.onError() }
            .addOnSuccessListener { circle->
                var membersList = ArrayList<DocumentReference>()
                try {
                    membersList = circle[Constants.MEMBERS] as ArrayList<DocumentReference>
                }catch (e: NullPointerException) {}

                val newMembersList = membersList.filter {
                    it.id != memberModel.uid
                }
                circleReference.update(Constants.MEMBERS, newMembersList)
                    .addOnFailureListener { listener.onError() }
                    .addOnSuccessListener {
                        memberModel.documentReference.get()
                            .addOnFailureListener {
                                circleReference.update(Constants.MEMBERS, membersList)
                                listener.onError()
                            }
                            .addOnSuccessListener {member ->
                                var circleList = ArrayList<DocumentReference>()
                                try {
                                    circleList = member[Constants.CIRCLES] as ArrayList<DocumentReference>
                                } catch (e: NullPointerException) {}

                                val newCircleList = circleList.filter {
                                    it.id != circleReference.id
                                }

                                memberModel.documentReference
                                    .update(Constants.CIRCLES, newCircleList)
                                    .addOnFailureListener {
                                        circleReference.update(Constants.MEMBERS, membersList)
                                        listener.onError()
                                    }
                                    .addOnSuccessListener { listener.memberRemoved(memberModel) }
                            }
                    }
            }
    }
}