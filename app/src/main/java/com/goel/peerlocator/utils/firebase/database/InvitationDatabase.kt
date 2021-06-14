package com.goel.peerlocator.utils.firebase.database

import com.goel.peerlocator.listeners.AddFriendListener
import com.goel.peerlocator.listeners.EditCircleListener
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.listeners.InvitationListener
import com.goel.peerlocator.models.InviteModel
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.utils.Constants
import com.google.firebase.Timestamp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentReference
import kotlin.math.ceil

class InvitationDatabase : Database() {

    companion object {
        val instance = InvitationDatabase()
    }
    private val invitesReference = FirebaseDatabase.getInstance().reference.child(Constants.INVITES)

    fun getAllInvites (listener: GetListListener) {
        invitesReference.child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.hasChildren()) {
                        listener.foundEmptyList()
                    }
                    else {
                        for (data in snapshot.children) {
                            val reference =
                                fireStoreDatabase.document(data.key.toString().toReferencePath())
                            val timeHash = data.value as HashMap<String, Long>
                            val timeStamp = Timestamp(
                                timeHash[Constants.SECONDS]!!,
                                timeHash[Constants.NANOSECONDS]!!.toInt()
                            )
                            val date = timeStamp.toDate()


                            reference.get().addOnSuccessListener {
                                val name = it[Constants.NAME].toString()
                                val photoUrl = it[Constants.DP].toString()
                                val model = InviteModel(
                                    documentReference = reference,
                                    name = name,
                                    imageUrl = photoUrl,
                                    timeStamp = date.toString()
                                )
                                listener.onInvitationRetrieved(model)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    listener.onError()
                }

            })
    }

    fun sendInvitations (documentReference: DocumentReference, uIds: ArrayList<String>, listener : EditCircleListener) {
        if (uIds.isEmpty()) {
            listener.membersAdditionSuccessful()
            return
        }
        var references = ArrayList<DocumentReference>()
        val timestamp = HashMap<String, Long>()
        val time = Timestamp.now()
        timestamp[Constants.NANOSECONDS] = time.nanoseconds.toLong()
        timestamp[Constants.SECONDS] = time.seconds
        val invitationPath = documentReference.path.toInvitationPath()
        var completion = 30
        val unit = ceil((70/uIds.size).toDouble()).toInt()
        documentReference.get().addOnFailureListener { listener.onError() }
            .addOnSuccessListener {
                try {
                    references = it[Constants.SENT_INVITES] as ArrayList<DocumentReference>
                } catch (e: java.lang.NullPointerException) {}
                for (uId in uIds) {
                    invitesReference.child(uId).child(invitationPath.toInvitationPath()).setValue(timestamp)
                        .addOnFailureListener { listener.onError() }
                        .addOnSuccessListener {
                            completion+= unit
                            references.add(userRef.document(uId))
                            if (completion >= 100) {
                                documentReference.update(Constants.SENT_INVITES, references)
                                    .addOnFailureListener { listener.onError() }
                                    .addOnSuccessListener { listener.membersAdditionSuccessful() }
                            }
                        }
                }
            }
    }

    fun sendInvitation(recipient: UnknownUserModel, listener: AddFriendListener) {
        var initialSent = ArrayList<DocumentReference>()
        currentUserRef.get().addOnFailureListener { listener.onError() }
            .addOnSuccessListener {
                try {
                    initialSent = it[Constants.SENT_INVITES] as ArrayList<DocumentReference>
                } catch (e: NullPointerException) {}

                initialSent.add(recipient.documentReference)

                currentUserRef.update(Constants.SENT_INVITES, initialSent)
                    .addOnFailureListener { listener.onError() }
                    .addOnSuccessListener {
                        val timestamp = HashMap<String, Long>()
                        val time = Timestamp.now()
                        timestamp[Constants.NANOSECONDS] = time.nanoseconds.toLong()
                        timestamp[Constants.SECONDS] = time.seconds
                        invitesReference.child(recipient.uid)
                            .child(currentUserRef.path.toInvitationPath())
                            .setValue(timestamp)
                            .addOnFailureListener { listener.onError() }
                            .addOnSuccessListener { listener.onInvitationSent(recipient) }
                    }
            }
    }

    fun getSentInvitations (listener: GetListListener) {
        currentUserRef.get().addOnFailureListener { listener.onError() }
            .addOnSuccessListener {
                var referenceList = ArrayList<DocumentReference>()
                try {
                    referenceList = it[Constants.SENT_INVITES] as ArrayList<DocumentReference>
                } catch (e : NullPointerException) {}
                if (referenceList.isEmpty())
                    listener.foundEmptyList()
                for (reference in referenceList) {
                    reference.get().addOnFailureListener { listener.onError() }
                        .addOnSuccessListener { user ->
                            val name = user[Constants.NAME].toString()
                            val url = user[Constants.DP].toString()
                            val model = UnknownUserModel(user.reference, user.reference.id, name, url)
                            listener.onUserRetrieved(model)
                        }
                }
            }
    }

    fun unSendInvitation (model: UnknownUserModel, listener: AddFriendListener) {
        val reference = model.documentReference
        var initialSentList = ArrayList<DocumentReference>()
        val updatedSentList = ArrayList<DocumentReference>()
        val path = currentUserRef.path
        currentUserRef.get().addOnFailureListener { listener.onError() }
            .addOnSuccessListener {
                try {
                    initialSentList = it[Constants.SENT_INVITES] as ArrayList<DocumentReference>
                } catch (e: NullPointerException) {}
                for (documentReference in initialSentList) {
                    if (documentReference.id != reference.id)
                        updatedSentList.add(documentReference)
                }
                currentUserRef.update(Constants.SENT_INVITES, updatedSentList)
                    .addOnFailureListener { listener.onError() }
                    .addOnSuccessListener {
                        invitesReference.child(reference.id).child(path.toInvitationPath()).removeValue()
                            .addOnFailureListener { listener.onError() }
                            .addOnSuccessListener { listener.onInvitationUnsent(model) }

                    }
            }
    }

    fun checkSentInvitation (inviteModel: InviteModel, listener: InvitationListener) {
        val id = inviteModel.documentReference.id
        val friendListener = object : AddFriendListener {
            override fun onInvitationSent(model: UnknownUserModel) {}
            override fun onInvitationUnsent(model: UnknownUserModel) {
                acceptInvitation(inviteModel, listener)
            }
            override fun onError() {
                listener.onError()
            }
        }

        val mySentInvitations = ArrayList<String>()
        currentUserRef.get().addOnFailureListener { listener.onError() }
            .addOnSuccessListener {
                try {
                    val sentList = it[Constants.SENT_INVITES] as ArrayList<DocumentReference>
                    for (reference in sentList) {
                        mySentInvitations.add(reference.id)
                    }
                } catch (e: NullPointerException) {}

                if (id in mySentInvitations)
                {
                    val userModel = UnknownUserModel(documentReference = inviteModel.documentReference,
                                                    uid = inviteModel.documentReference.id,
                                                    name = inviteModel.name,
                                                    imageUrl = inviteModel.imageUrl)
                    unSendInvitation(userModel, friendListener)
                }
                else
                    acceptInvitation(inviteModel, listener)
            }
    }

    private fun acceptInvitation (inviteModel: InviteModel, listener: InvitationListener) {
        rejectInvitation(inviteModel, object : InvitationListener {
            override fun onInvitationAccepted(model: InviteModel) {}
            override fun onInvitationRejected(model: InviteModel) {
                if (Constants.CIRCLES in model.documentReference.path) {
                    CirclesDatabase.instance.addMember(model, listener)
                }
                else if (Constants.USERS in model.documentReference.path) {
                    FriendsDatabase.instance.addMeToFriend(model, listener)
                }
            }

            override fun onError() {
                listener.onError()
            }
        })
    }

    fun rejectInvitation (inviteModel: InviteModel, listener: InvitationListener) {
        val reference = inviteModel.documentReference

        invitesReference.child(currentUserRef.id).child(reference.path.toInvitationPath()).removeValue()
            .addOnFailureListener { listener.onError() }
            .addOnSuccessListener {
                val updatedSentList = ArrayList<DocumentReference>()
                reference.get().addOnFailureListener { listener.onError() }
                    .addOnSuccessListener { document->
                        try {
                            val initialList = document[Constants.SENT_INVITES] as ArrayList<DocumentReference>
                            for (documentReference in initialList) {
                                if (documentReference.id != currentUserRef.id)
                                    updatedSentList.add(documentReference)
                            }
                        } catch (e: java.lang.NullPointerException){}

                        reference.update(Constants.SENT_INVITES, updatedSentList)
                            .addOnFailureListener { listener.onError() }
                            .addOnSuccessListener { listener.onInvitationRejected(inviteModel) }
                    }
            }
    }

    private fun String.toReferencePath () = this.replace('!', '/')
    private fun String.toInvitationPath () = this.replace('/', '!')
}