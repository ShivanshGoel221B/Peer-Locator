package com.goel.peerlocator.utils.firebase

import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.CirclesAdapter
import com.goel.peerlocator.adapters.FriendsAdapter
import com.goel.peerlocator.adapters.InvitesAdapter
import com.goel.peerlocator.listeners.*
import com.goel.peerlocator.models.*
import com.goel.peerlocator.utils.Constants
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.io.InputStream

object Database {

    private lateinit var currentUserRef : DocumentReference

    var listener : UserDataListener? = null

    var currentUser : UserModel? = null

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
                        listener?.onUserCreated()
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
            listener?.onUserCreated()
            newUser[Constants.EMAIL] = user.email!!
            currentUserRef.set(newUser)
        }
    }

    fun getAllCircles (
        userRef: CollectionReference, circleList: java.util.ArrayList<CircleModel>,
        circlesAdapter: CirclesAdapter, shimmer: ShimmerFrameLayout, nothingFound: LinearLayout) {
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

    fun getAllFriends (userRef: CollectionReference, friendsList : java.util.ArrayList<FriendModel>,
                       friendsAdapter: FriendsAdapter, shimmer: ShimmerFrameLayout, nothingFound: LinearLayout) {
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
                            if (getUid(friend.reference) !in addedIds) {
                                val name = friend[Constants.NAME].toString()
                                val imageUrl = friend[Constants.DP].toString()

                                val model = FriendModel(documentReference = friend.reference, name = name,
                                            imageUrl = imageUrl, uid = getUid(friend.reference)
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
                        commonCirclesCount = count, uid = getUid(it.reference)
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


    fun getAllInvites (database : FirebaseFirestore, invitesList : ArrayList<InviteModel>,
                       invitesAdapter: InvitesAdapter, shimmer: ShimmerFrameLayout, nothingFound: LinearLayout) {
        FirebaseDatabase.getInstance().reference.child(Constants.INVITES).child(currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.hasChildren()) {
                        shimmer.visibility = View.GONE
                        shimmer.stopShimmerAnimation()
                        nothingFound.visibility = View.VISIBLE
                    }
                    else
                        nothingFound.visibility - View.GONE

                    for (data in snapshot.children) {
                        val reference = database.document(data.key.toString().toInvitationPath())
                        val timeStamp = data.value.toString()


                        reference.get().addOnSuccessListener {
                            val name = it[Constants.NAME].toString()
                            val photoUrl = it[Constants.DP].toString()
                            val newInvite = InviteModel(documentReference = reference, name = name, imageUrl = photoUrl, timeStamp = timeStamp)
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

    //Methods for Getting Friend Info
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
                }
                currentUserRef.update(Constants.BLOCKS, fullBlockList).addOnFailureListener { listener.onNetworkError() }
                    .addOnSuccessListener { listener.onUnblocked() }
            }

    }

    fun createNewCircle (
        circleReference: CollectionReference,
        name: String,
        imageStream: InputStream?,
        membersList: ArrayList<DocumentReference>,
        listener: EditCircleListener
    ) {
        val reference = circleReference.document()
        val uid = reference.id
        val model = CircleModel(documentReference = reference, uid = uid,
            adminReference = currentUserRef, memberCount = membersList.size+1,
            name = name, imageUrl = Constants.DEFAULT_IMAGE_URL)

        val circleMap = HashMap<String, Any>()
        circleMap[Constants.NAME] = model.name
        circleMap[Constants.ADMIN] = model.adminReference
        circleMap[Constants.DP] = model.imageUrl
        circleMap[Constants.MEMBERS] = arrayListOf(currentUserRef)

        reference.set(circleMap).addOnFailureListener { listener.onError() }
            .addOnSuccessListener {
                if (imageStream == null) {
                    listener.onCreationSuccessful()
                    addMembers(reference,
                        arrayListOf(currentUserRef), membersList, listener)
                }
                else {
                    Storage.uploadProfileImage(model, reference, imageStream, arrayListOf(
                        currentUserRef), membersList, listener)
                }

            }
    }

    fun inviteMembers (documentReference: DocumentReference, membersList: ArrayList<DocumentReference>) {
        val ids = ArrayList<String>()
        membersList.forEach {
            ids.add(getUid(it))
        }
        val invitationPath = documentReference.path.toInvitationPath()
        TODO("send invitations")
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

    private fun getUid (reference: DocumentReference) = reference.path.substring(6)
    private fun String.toInvitationPath () = this.replace('!', '/')

}