package com.goel.peerlocator.utils.firebase

import android.util.Log
import android.view.View
import com.facebook.shimmer.ShimmerFrameLayout
import com.goel.peerlocator.adapters.CirclesAdapter
import com.goel.peerlocator.adapters.FriendsAdapter
import com.goel.peerlocator.adapters.InvitesAdapter
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.models.InviteModel
import com.goel.peerlocator.models.UserModel
import com.goel.peerlocator.utils.Constants
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

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
                        currentUser?.displayName = it[Constants.NAME].toString()
                        currentUser?.photoUrl = it[Constants.DP].toString()
                        listener?.onUserCreated()
                    }
                }
                .addOnFailureListener {
                    Log.d("Error", it.toString())
                }
    }

    private fun createUserEntry (currentUserRef: DocumentReference, user: FirebaseUser) {
        user.let {
            val newUser = hashMapOf<String, String>()
            newUser[Constants.NAME] = user.displayName!!
            newUser[Constants.DP] = user.photoUrl!!.toString()
            currentUser?.displayName = user.displayName!!
            currentUser?.photoUrl = user.photoUrl!!.toString()
            listener?.onUserCreated()
            newUser[Constants.EMAIL] = user.email!!
            currentUserRef.set(newUser)
        }
    }

    fun getAllCircles (userRef: CollectionReference, circleList: java.util.ArrayList<CircleModel>,
                       circlesAdapter: CirclesAdapter, shimmer: ShimmerFrameLayout) {
        var circleArray = java.util.ArrayList<DocumentReference>()

        userRef.document(currentUser!!.uid).get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        try {
                            circleArray = it[Constants.CIRCLES] as ArrayList<DocumentReference>
                        } catch (e: NullPointerException) {}
                        currentUser?.circlesCount = circleArray.size.toLong()
                        addToList(circleArray, circleList, circlesAdapter, shimmer)
                    }
                }
                .addOnFailureListener {
                    Log.d("Error", it.toString())
                }
    }

    // Adds circles to the list
    private fun addToList (circleArray: ArrayList<DocumentReference>, list: ArrayList<CircleModel>,
                           circlesAdapter: CirclesAdapter, shimmer: ShimmerFrameLayout) {

        for (circle in circleArray) {
            circle.get()
                .addOnSuccessListener {
                    var membersCount = 0
                    try {
                        membersCount = (it[Constants.MEMBERS] as ArrayList<DocumentReference>).size
                    } catch (e: java.lang.NullPointerException) {
                    }
                    val newCircle = CircleModel(circleName = it[Constants.NAME].toString(),
                        circleReference = it.reference,
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
                       friendsAdapter: FriendsAdapter, shimmer: ShimmerFrameLayout) {
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
                    addToList(friendsArray, circleArray, friendsList, friendsAdapter, shimmer)
                }
            }
            .addOnFailureListener {
                Log.d("Error", it.toString())
            }
    }

    // Adds friends to the list
    private fun addToList (friendsArray: ArrayList<DocumentReference>, circleArray: ArrayList<DocumentReference>,
                           list: ArrayList<FriendModel>, friendsAdapter: FriendsAdapter, shimmer: ShimmerFrameLayout) {
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

                    val newFriend = FriendModel(friendName = it[Constants.NAME].toString(),
                        friendReference = it.reference,
                        imageUrl = it[Constants.DP].toString(),
                        commonCirclesCount = count, uid = it.reference.path.substring(6)
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
                       invitesAdapter: InvitesAdapter, shimmer: ShimmerFrameLayout) {
        FirebaseDatabase.getInstance().reference.child(Constants.INVITES).child(currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        val reference = database.document(data.key.toString().replace('!', '/'))
                        val timeStamp = data.value.toString()


                        reference.get().addOnSuccessListener {
                            val name = it[Constants.NAME].toString()
                            val photoUrl = it[Constants.DP].toString()
                            val newInvite = InviteModel(reference = reference, name = name, photoUrl = photoUrl, timeStamp = timeStamp)
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

}