package com.goel.peerlocator.utils.firebase.database

import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.InvitesAdapter
import com.goel.peerlocator.listeners.AddFriendListener
import com.goel.peerlocator.listeners.EditCircleListener
import com.goel.peerlocator.listeners.GetListListener
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

    fun getAllInvites (invitesList : ArrayList<InviteModel>,
                       invitesAdapter: InvitesAdapter, shimmer: ShimmerFrameLayout, nothingFound: LinearLayout
    ) {
        invitesReference.child(currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.hasChildren()) {
                        shimmer.visibility = View.GONE
                        shimmer.stopShimmerAnimation()
                        nothingFound.visibility = View.VISIBLE
                    }
                    else
                        nothingFound.visibility - View.GONE

                    for (data in snapshot.children) {
                        val reference = fireStoreDatabase.document(data.key.toString().toReferencePath())
                        val timeHash = data.value as HashMap<String, Long>
                        val timeStamp = Timestamp(timeHash[Constants.SECONDS]!!, timeHash[Constants.NANOSECONDS]!!.toInt())
                        val date = timeStamp.toDate()


                        reference.get().addOnSuccessListener {
                            val name = it[Constants.NAME].toString()
                            val photoUrl = it[Constants.DP].toString()
                            val newInvite = InviteModel(documentReference = reference, name = name, imageUrl = photoUrl, timeStamp = date.toString())
                            invitesList.add(newInvite)
                            invitesAdapter.notifyDataSetChanged()
                            shimmer.visibility = View.GONE
                            shimmer.stopShimmerAnimation()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Error: ", error.message)
                }

            })
    }

    fun sendInvitations (documentReference: DocumentReference, uIds: ArrayList<String>, listener : EditCircleListener) {
        if (uIds.isEmpty()) {
            listener.onInvitationSent(100, 100)
            return
        }
        var references = ArrayList<DocumentReference>()
        val timestamp = Timestamp.now()
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
                            listener.onInvitationSent(completion, completion+unit)
                            references.add(userRef.document(uId))
                            if (completion >= 100) {
                                documentReference.update(Constants.SENT_INVITES, references)
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
                        invitesReference.child(recipient.uid)
                            .child(currentUserRef.path.toInvitationPath())
                            .setValue(Timestamp.now())
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

    private fun String.toReferencePath () = this.replace('!', '/')
    private fun String.toInvitationPath () = this.replace('/', '!')
}