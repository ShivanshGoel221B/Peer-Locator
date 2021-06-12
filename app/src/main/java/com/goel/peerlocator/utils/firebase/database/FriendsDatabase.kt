package com.goel.peerlocator.utils.firebase.database

import com.goel.peerlocator.listeners.EditFriendListener
import com.goel.peerlocator.listeners.FriendDataListener
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.listeners.InvitationListener
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.models.InviteModel
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.utils.Constants
import com.google.firebase.firestore.DocumentReference

class FriendsDatabase : Database() {
     companion object {
         val instance = FriendsDatabase()
     }

    fun getAllFriends (listener: GetListListener) {
        var circleArray =  java.util.ArrayList<DocumentReference>()
        var friendsArray =  java.util.ArrayList<DocumentReference>()

        userRef.document(currentUser.uid).get().addOnFailureListener { listener.onError() }
            .addOnSuccessListener {
                if (it.exists()) {
                    try {
                        circleArray = it[Constants.CIRCLES] as ArrayList<DocumentReference>
                    } catch (e: NullPointerException) { }
                    try {
                        friendsArray = it[Constants.FRIENDS] as ArrayList<DocumentReference>
                    } catch (e : java.lang.NullPointerException){}
                    currentUser.friendsCount = friendsArray.size.toLong()
                    addToList(friendsArray, circleArray, listener)
                }
            }
    }

    //Adding Members in a circle
    fun getAllFriends (addedIds : ArrayList<String>, listener : GetListListener) {
        currentUserRef.get().addOnFailureListener { listener.onError() }
            .addOnSuccessListener {
                var references = ArrayList<DocumentReference>()
                try {
                    references = it[Constants.FRIENDS] as ArrayList<DocumentReference>
                } catch (e : NullPointerException) {}

                if (addedIds.size >= references.size)
                    listener.foundEmptyList()

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
                           listener: GetListListener) {
        if (friendsArray.isEmpty())
            listener.foundEmptyList()
        else {
            for (friend in friendsArray) {
                friend.get().addOnFailureListener { listener.onError() }
                    .addOnSuccessListener {
                        var circles = ArrayList<DocumentReference>()
                        try {
                            circles = it[Constants.CIRCLES] as ArrayList<DocumentReference>
                        } catch (e: java.lang.NullPointerException) {
                        }
                        var count = 0
                        for (circleOne in circles) {
                            for (circleTwo in circleArray) {
                                if (circleOne.path == circleTwo.path)
                                    count++
                            }
                        }

                        val model = FriendModel(
                            name = it[Constants.NAME].toString(),
                            documentReference = it.reference,
                            imageUrl = it[Constants.DP].toString(),
                            commonCirclesCount = count, uid = it.reference.id
                        )
                        listener.onFriendRetrieved(model)
                    }
            }
        }
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

                val excludeList = friends + sendInvites + blocked + blockedBy + listOf(currentUser.uid)

                userRef.get().addOnFailureListener { listener.onError() }
                    .addOnSuccessListener { users ->
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

    fun addMeToFriend (model: InviteModel, listener: InvitationListener) {
        val documentReference = model.documentReference
        var friendsList = ArrayList<DocumentReference>()

        documentReference.get().addOnFailureListener { listener.onError() }
            .addOnSuccessListener {
                try {
                    friendsList = it[Constants.FRIENDS] as ArrayList<DocumentReference>
                } catch (e: java.lang.NullPointerException){}
                friendsList.add(currentUserRef)
                documentReference.update(Constants.FRIENDS, friendsList)
                    .addOnFailureListener { listener.onError() }
                    .addOnSuccessListener { addFriend(model, listener) }
            }
    }

    fun removeFriend ( documentReference: DocumentReference, listener: EditFriendListener) {
        currentUserRef.get()
            .addOnFailureListener { listener.onError() }
            .addOnSuccessListener { me->
                var myInitialFriends = ArrayList<DocumentReference>()
                try {
                    myInitialFriends = me[Constants.FRIENDS] as ArrayList<DocumentReference>
                } catch (e: java.lang.NullPointerException) {}

                val myNewFriends = myInitialFriends.filter { it.id != documentReference.id }

                currentUserRef.update(Constants.FRIENDS, myNewFriends)
                    .addOnFailureListener { listener.onError() }
                    .addOnSuccessListener {
                        documentReference.get()
                            .addOnFailureListener {
                                listener.onError()
                                currentUserRef.update(Constants.FRIENDS, myInitialFriends)
                            }
                            .addOnSuccessListener {friend->
                                var friends = ArrayList<DocumentReference>()
                                try {
                                    friends = friend[Constants.FRIENDS] as ArrayList<DocumentReference>
                                } catch (e: java.lang.NullPointerException) {}

                                val newFriends = friends.filter { it.id != currentUserRef.id }
                                documentReference.update(Constants.FRIENDS, newFriends)
                                    .addOnFailureListener {
                                        listener.onError()
                                        currentUserRef.update(Constants.FRIENDS, myInitialFriends)
                                    }
                                    .addOnSuccessListener { listener.onFriendRemoved() }
                            }
                    }
            }
    }
}