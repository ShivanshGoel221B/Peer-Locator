package com.goel.peerlocator.utils.firebase.database

import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.FriendsAdapter
import com.goel.peerlocator.listeners.FriendDataListener
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.utils.Constants
import com.google.firebase.firestore.DocumentReference

class FriendsDatabase : Database() {
     companion object {
         val instance = FriendsDatabase()
     }

    fun getAllFriends (friendsList : java.util.ArrayList<FriendModel>,
                       friendsAdapter: FriendsAdapter, shimmer: ShimmerFrameLayout, nothingFound: LinearLayout
    ) {
        var circleArray =  java.util.ArrayList<DocumentReference>()
        var friendsArray =  java.util.ArrayList<DocumentReference>()

        userRef.document(currentUser!!.uid).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    try {
                        circleArray = it[Constants.CIRCLES] as ArrayList<DocumentReference>
                    } catch (e: NullPointerException) { }
                    try {
                        friendsArray = it[Constants.FRIENDS] as ArrayList<DocumentReference>
                    } catch (e : java.lang.NullPointerException){}
                    currentUser?.friendsCount = friendsArray.size.toLong()
                    addToList(friendsArray, circleArray, friendsList, friendsAdapter, shimmer, nothingFound)
                }
            }
            .addOnFailureListener {
                Log.d("Error", it.toString())
            }
    }

    fun getAllFriends (addedIds : ArrayList<String>, listener : GetListListener) {
        currentUserRef.get().addOnFailureListener { listener.onError() }
            .addOnSuccessListener {
                var references = ArrayList<DocumentReference>()
                try {
                    references = it[Constants.FRIENDS] as ArrayList<DocumentReference>
                } catch (e : NullPointerException) {}

                for (reference in references) {
                    reference.get().addOnFailureListener { listener.onError() }
                        .addOnSuccessListener { friend ->
                            if (friend.reference.id !in addedIds) {
                                val name = friend[Constants.NAME].toString()
                                val imageUrl = friend[Constants.DP].toString()

                                val model = FriendModel(documentReference = friend.reference, name = name,
                                    imageUrl = imageUrl, uid = friend.reference.id
                                )
                                listener.onFriendRetrieved(model)
                            }
                        }
                }
            }
    }

    // Adds friends to the list
    private fun addToList (friendsArray: ArrayList<DocumentReference>, circleArray: ArrayList<DocumentReference>,
                           list: ArrayList<FriendModel>, friendsAdapter: FriendsAdapter,
                           shimmer: ShimmerFrameLayout, nothingFound: LinearLayout) {
        if (friendsArray.isEmpty())
            nothingFound.visibility = View.VISIBLE
        else
            nothingFound.visibility = View.GONE
        for (friend in friendsArray) {
            friend.get()
                .addOnSuccessListener {
                    var circles = ArrayList<DocumentReference>()
                    try {
                        circles =  it[Constants.CIRCLES] as ArrayList<DocumentReference>
                    } catch (e : java.lang.NullPointerException){}
                    var count = 0
                    for (circleOne in circles) {
                        for (circleTwo in circleArray) {
                            if (circleOne.path == circleTwo.path)
                                count++
                        }
                    }

                    val newFriend = FriendModel(name = it[Constants.NAME].toString(),
                        documentReference = it.reference,
                        imageUrl = it[Constants.DP].toString(),
                        commonCirclesCount = count, uid = it.reference.id
                    )
                    list.add(newFriend)
                    friendsAdapter.notifyDataSetChanged()
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

    fun getFriendInfo (listener: FriendDataListener, reference: DocumentReference) {
        reference.get().addOnSuccessListener {
            if (it.exists()) {
                var circleList = ArrayList<DocumentReference>()
                try {
                    circleList = it[Constants.CIRCLES] as ArrayList<DocumentReference>
                } catch (e: java.lang.NullPointerException) {}
                var friendsList = ArrayList<Any>()
                try {
                    friendsList = it[Constants.FRIENDS] as ArrayList<Any>
                } catch (e: java.lang.NullPointerException) { }

                listener.onCountComplete(circleList.size.toLong(), friendsList.size.toLong())

                currentUserRef.get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val commonCircle = ArrayList<DocumentReference>()
                        var circleArray = ArrayList<DocumentReference>()
                        try {
                            circleArray = snapshot[Constants.CIRCLES] as ArrayList<DocumentReference>
                        } catch (e: java.lang.NullPointerException) { }
                        for (circle in circleList) {
                            for (myCircle in circleArray)
                            {
                                if (circle.path == myCircle.path)
                                {
                                    commonCircle.add(circle)
                                }
                            }
                        }
                        listener.onCommonCirclesComplete(commonCircle)
                    }
                }
            }
        }
    }

    fun getUsers(listener: GetListListener) {
        val friends = ArrayList<String>()
        val sendInvites = ArrayList<String>()
        val blocked = ArrayList<String>()
        val blockedBy = ArrayList<String>()

        currentUserRef.get().addOnFailureListener { listener.onError() }
            .addOnSuccessListener {
                try {
                    val friendReferences = it[Constants.FRIENDS] as ArrayList<DocumentReference>
                    friendReferences.forEach {friend ->
                        friends.add(friend.id)
                    }
                } catch (e : NullPointerException) {}

                try {
                    val invitesReferences = it[Constants.SENT_INVITES] as ArrayList<DocumentReference>
                    invitesReferences.forEach {invitationRef ->
                        sendInvites.add(invitationRef.id)
                    }
                } catch (e: NullPointerException) {}

                try {
                    val blocksReference = it[Constants.BLOCKS] as ArrayList<DocumentReference>
                    blocksReference.forEach {ref ->
                        blocked.add(ref.id)
                    }
                } catch (e: NullPointerException) {}

                try {
                    val blocks = it[Constants.BLOCKED_BY] as ArrayList<DocumentReference>
                    blocks.forEach {blockRef ->
                        blockedBy.add(blockRef.id)
                    }
                } catch (e: NullPointerException) {}

                val excludeList = friends + sendInvites + blocked + blockedBy + listOf(currentUser?.uid)

                userRef.get().addOnFailureListener { listener.onError() }
                    .addOnSuccessListener { users ->
                        if (excludeList.size >= users.documents.size)
                            listener.foundEmptyList()
                        for (user in users.documents) {
                            val isVisible = user[Constants.VISIBLE] as Boolean
                            if (user.reference.id !in excludeList && isVisible) {
                                val userReference = user.reference
                                val name = user[Constants.NAME].toString()
                                val url = user[Constants.DP].toString()
                                val id = user.reference.id

                                val model = UnknownUserModel(userReference, id, name, url)
                                listener.onUserRetrieved(model)
                            }
                        }
                    }
            }
    }
}