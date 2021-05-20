package com.goel.peerlocator.utils.firebase.database

import android.util.Log
import com.goel.peerlocator.listeners.*
import com.goel.peerlocator.models.*
import com.goel.peerlocator.utils.Constants
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

abstract class Database {

    protected val fireStoreDatabase = Firebase.firestore
    protected val userRef = fireStoreDatabase.collection(Constants.USERS)
    protected val circlesReference = fireStoreDatabase.collection(Constants.CIRCLES)

    companion object {
        lateinit var currentUserRef : DocumentReference
        var currentUser : UserModel? = null
        var userDataListener : UserDataListener? = null

        fun signIn(user : FirebaseUser, userRef: CollectionReference) {
            currentUserRef = userRef.document(user.uid)
            currentUser = UserModel(documentReference = currentUserRef, uid = user.uid,
                email = user.email, phoneNumber = user.phoneNumber)

            currentUserRef.get()
                .addOnSuccessListener {
                    if (!it.exists()) {
                        createUserEntry(currentUserRef, user)
                    }
                    else {
                        currentUser?.name = it[Constants.NAME].toString()
                        currentUser?.imageUrl = it[Constants.DP].toString()
                        userDataListener?.onUserCreated()
                    }
                }
                .addOnFailureListener {
                    Log.d("Error", it.toString())
                }
        }

        private fun createUserEntry (currentUserRef: DocumentReference, user: FirebaseUser) {
            user.let {
                val newUser = hashMapOf<String, Any>()
                newUser[Constants.NAME] = user.displayName!!
                newUser[Constants.DP] = user.photoUrl!!.toString()
                newUser[Constants.ONLINE] = true
                newUser[Constants.VISIBLE] = true
                currentUser?.name = user.displayName!!
                currentUser?.imageUrl = user.photoUrl!!.toString()
                userDataListener?.onUserCreated()
                newUser[Constants.EMAIL] = user.email!!
                currentUserRef.set(newUser)
            }
        }

        fun getMyData (listener : ProfileDataListener) {
            currentUser!!.documentReference.get().addOnFailureListener {
                listener.networkError()
            }
            currentUser!!.documentReference.get().addOnSuccessListener {
                var friendCount = 0
                try {
                    val friendList = it[Constants.FRIENDS] as ArrayList<*>
                    friendCount = friendList.size
                } catch (e : NullPointerException){}
                listener.friendsCountComplete(friendCount.toLong())

                var circleCount = 0
                try {
                    val circleList = it[Constants.CIRCLES] as ArrayList<*>
                    circleCount = circleList.size
                } catch (e : NullPointerException){}
                listener.circlesCountComplete(circleCount.toLong())

                listener.onlineStatusFetched(it[Constants.ONLINE] as Boolean)
                listener.visibilityStatusFetched(it[Constants.VISIBLE] as Boolean)

                if (it[Constants.EMAIL] == null) {
                    listener.onEmailFound(false, "N/A")
                }else {
                    listener.onEmailFound(true, it[Constants.EMAIL].toString())
                }

                if (it[Constants.PHONE] == null) {
                    listener.onPhoneFound(false, "N/A")
                } else {
                    listener.onPhoneFound(true, it[Constants.PHONE].toString())
                }
            }
        }

        fun findUser(reference: DocumentReference, listener: UserSearchListener) {
            reference.get().addOnSuccessListener {
                val userPath = currentUser?.documentReference?.path
                try {
                    val friends = it[Constants.FRIENDS] as ArrayList<DocumentReference>
                    for(ref in friends) {
                        if (userPath == ref.path) {
                            val friend = FriendModel(documentReference = reference, uid = getUid(reference),
                                name = it[Constants.NAME].toString(), imageUrl = it[Constants.DP].toString())
                            listener.friendFound(friend)
                            return@addOnSuccessListener
                        }
                    }
                }catch (e : java.lang.NullPointerException){ }

                try {
                    val blocks = it[Constants.BLOCKS] as ArrayList<DocumentReference>
                    for (ref in blocks) {
                        if (userPath == ref.path) {
                            listener.blockedFound()
                            return@addOnSuccessListener
                        }
                    }
                }catch (e : NullPointerException){}

                listener.userFound(UnknownUserModel(reference, getUid(reference),
                    name = it[Constants.NAME].toString(), imageUrl = it[Constants.DP].toString()))
            }
        }

        // To check the possibility of removing this redundant method
        private fun getUid (reference: DocumentReference) = reference.path.substring(6)

        fun changeName(documentReference: DocumentReference, newName: String, listener: ProfileDataListener) {
            documentReference.update(Constants.NAME, newName).addOnSuccessListener { listener.onNameChanged(newName) }
                .addOnFailureListener { listener.networkError() }
        }

        fun changeImageUrl (documentReference: DocumentReference, url : String) {
            documentReference.update(Constants.DP, url)
        }

        fun changeOnlineStatus(status: Boolean, listener: ProfileDataListener) {
            currentUserRef.update(Constants.ONLINE, status).addOnSuccessListener { listener.onOnlineStatusChanged(status) }
        }

        fun changeVisibilityStatus(status: Boolean, listener: ProfileDataListener) {
            currentUserRef.update(Constants.VISIBLE, status).addOnSuccessListener { listener.onVisibilityStatusChanged(status) }
        }

        fun getMyBlockList(listener: BlockListener) {
            currentUserRef.get()
                .addOnFailureListener { listener.onNetworkError() }
                .addOnSuccessListener {
                    var blocks = ArrayList<DocumentReference>()
                    try {
                        blocks = it[Constants.BLOCKS] as ArrayList<DocumentReference>
                    }catch (e : NullPointerException){}
                    if (blocks.isEmpty())
                        listener.noBlockFound()
                    for (reference in blocks){
                        reference.get().addOnFailureListener { listener.onNetworkError() }
                            .addOnSuccessListener { user ->
                                val model = UnknownUserModel(user.reference, name = user[Constants.NAME].toString(),
                                    imageUrl = user[Constants.DP].toString())
                                listener.onBlockListUpdated(model)
                            }
                    }
                }
        }

        fun unblockSelected (list: List<String>, listener: BlockListener) {
            var fullBlockList = ArrayList<DocumentReference>()

            currentUserRef.get().addOnFailureListener { listener.onNetworkError() }
                .addOnSuccessListener {
                    try {
                        fullBlockList = it[Constants.BLOCKS] as ArrayList<DocumentReference>
                    } catch (e : NullPointerException) {}

                    val toUnblockList = ArrayList<DocumentReference>()
                    for (block in fullBlockList) {
                        if (block.path in list)
                            toUnblockList.add(block)
                    }
                    for (block in toUnblockList) {
                        fullBlockList.remove(block)
                        block.get().addOnFailureListener { listener.onNetworkError() }
                            .addOnSuccessListener { blockedRef ->
                                val blockedByList = blockedRef[Constants.BLOCKED_BY] as ArrayList<DocumentReference>
                                blockedByList.remove(currentUserRef)
                                block.update(Constants.BLOCKED_BY, blockedByList)
                            }
                    }
                    currentUserRef.update(Constants.BLOCKS, fullBlockList).addOnFailureListener { listener.onNetworkError() }
                        .addOnSuccessListener { listener.onUnblocked() }
                }

        }

    }

    protected fun addCircle (model: InviteModel, listener: InvitationListener) {
        val documentReference = model.documentReference
        var circlesList = ArrayList<DocumentReference>()

        currentUserRef.get().addOnFailureListener { listener.onError() }
            .addOnSuccessListener {
                try {
                    circlesList = it[Constants.CIRCLES] as ArrayList<DocumentReference>
                } catch (e: java.lang.NullPointerException){}
                circlesList.add(documentReference)
                currentUserRef.update(Constants.CIRCLES, circlesList)
                    .addOnFailureListener { listener.onError() }
                    .addOnSuccessListener { listener.onInvitationAccepted(model) }
            }
    }

    protected fun addFriend (model: InviteModel, listener: InvitationListener) {
        val documentReference = model.documentReference
        var friendsList = ArrayList<DocumentReference>()

        currentUserRef.get().addOnFailureListener { listener.onError() }
            .addOnSuccessListener {
                try {
                    friendsList = it[Constants.FRIENDS] as ArrayList<DocumentReference>
                } catch (e: java.lang.NullPointerException){}
                friendsList.add(documentReference)
                currentUserRef.update(Constants.FRIENDS, friendsList)
                    .addOnFailureListener { listener.onError() }
                    .addOnSuccessListener { listener.onInvitationAccepted(model) }
            }
    }

}