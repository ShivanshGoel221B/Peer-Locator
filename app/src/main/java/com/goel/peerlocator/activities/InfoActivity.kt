package com.goel.peerlocator.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goel.peerlocator.R
import com.goel.peerlocator.adapters.CirclesAdapter
import com.goel.peerlocator.adapters.FriendsAdapter
import com.goel.peerlocator.databinding.ActivityInfoBinding
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.models.UserModel
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.CircleDataListener
import com.goel.peerlocator.utils.firebase.Database
import com.goel.peerlocator.utils.firebase.FriendDataListener
import com.google.firebase.firestore.DocumentReference
import com.squareup.picasso.Picasso

class InfoActivity : AppCompatActivity(), FriendDataListener, CircleDataListener, CirclesAdapter.CircleClickListener, FriendsAdapter.FriendClickListener {

    companion object {
        lateinit var model : Any
    }
    private lateinit var binding: ActivityInfoBinding
    private lateinit var commonList: ArrayList<CircleModel>
    private lateinit var membersList : ArrayList<FriendModel>
    private lateinit var adapter : Any

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createToolBar()
        setViews()
    }

    private fun createToolBar() {
        val toolbar : androidx.appcompat.widget.Toolbar = binding.infoToolbar.root
        setSupportActionBar(toolbar)
        binding.infoToolbar.backButton.setOnClickListener {onBackPressed()}
        binding.infoToolbar.backButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back))
        supportActionBar?.title = ""
    }

    private fun setViews () {
        when (model) {
            is UserModel -> {
                binding.profileInformation.visibility = View.GONE
                binding.unknownPrivacy.visibility = View.VISIBLE
                binding.sendInvitationButton.visibility = View.VISIBLE
                binding.removeButton.visibility = View.GONE
                binding.blockButton.visibility = View.VISIBLE

                setData (model as UserModel)
            }
            is FriendModel -> {
                binding.profileInformation.visibility = View.VISIBLE
                binding.unknownPrivacy.visibility = View.GONE
                binding.sendInvitationButton.visibility = View.GONE
                binding.removeButton.visibility = View.VISIBLE
                binding.blockButton.visibility = View.VISIBLE
                setData(model as FriendModel)
            }
            is CircleModel -> {
                binding.profileInformation.visibility = View.VISIBLE
                binding.unknownPrivacy.visibility = View.GONE
                binding.sendInvitationButton.visibility = View.GONE
                binding.removeButton.visibility = View.VISIBLE
                binding.blockButton.visibility = View.VISIBLE
                setData(model as CircleModel)
            }
        }
    }

    private fun setData(model: UserModel) {
        val photoUrl = model.photoUrl
        val name = model.displayName

    }

    private fun setData (model : FriendModel) {
        val photoUrl = model.imageUrl
        val name = model.friendName
        binding.infoToolbar.profileName.text = name
        binding.listTitle.text = getString(R.string.circles_in_common)
        Picasso.with(this).load(photoUrl).placeholder(R.drawable.ic_placeholder_user_big).into(binding.profileImageHolder)
        Database.getFriendInfo(this, model.friendReference)
    }

    private fun setData (model : CircleModel) {
        val photoUrl = model.imageUrl
        val name = model.circleName
        binding.infoToolbar.profileName.text = name
        binding.listTitle.text = getString(R.string.members)
        Picasso.with(this).load(photoUrl).placeholder(R.drawable.ic_placeholder_circle_big).into(binding.profileImageHolder)
        Database.getCircleInfo(this, model.circleReference)
    }

    private fun createRecyclerView () {
        when (model) {
            is CircleModel -> {
                membersList = ArrayList()
                adapter = FriendsAdapter(membersList, this, this)
                binding.infoRecyclerView.adapter = adapter as FriendsAdapter
            }
            is FriendModel -> {
                commonList = ArrayList()
                adapter = CirclesAdapter(commonList, this, this)
                binding.infoRecyclerView.adapter = adapter as CirclesAdapter
            }
        }
        val lm = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.infoRecyclerView.layoutManager = lm
    }


    // Friend Listeners
    override fun onCountComplete(circles: Long, friends: Long) {
        binding.infoCirclesCount.text = resources.getQuantityString(R.plurals.circles_count, circles.toInt(), circles)
        binding.infoFriendsCount.text = resources.getQuantityString(R.plurals.friends_count, friends.toInt(), friends)
    }

    override fun onCommonCirclesComplete(commonCircleList: ArrayList<DocumentReference>) {
        createRecyclerView()

        for(ref in commonCircleList) {
            ref.get().addOnSuccessListener {
                if (it.exists()) {
                    val circle = CircleModel(circleReference = it.reference, imageUrl = it[Constants.DP].toString(),
                            circleName = it[Constants.NAME].toString(), adminReference = it[Constants.ADMIN] as DocumentReference)
                    try {
                        circle.memberCount = (it[Constants.MEMBERS] as ArrayList<Any>).size
                    } catch (e: NullPointerException) { }
                    commonList.add(circle)
                    (adapter as CirclesAdapter).notifyDataSetChanged()
                }
            }
        }
    }

    // Circle Listeners
    override fun onMemberCountComplete(members: Long) {
        binding.infoFriendsCount.text = resources.getQuantityString(R.plurals.members_count, members.toInt(), members)
    }

    override fun onMembersRetrieved(references: ArrayList<DocumentReference>) {
        createRecyclerView()

        for (ref in references) {
            ref.get().addOnSuccessListener {
                if (it.exists()) {
                    val friend = FriendModel(friendReference = it.reference, uid = it.reference.path.substring(6),
                                            friendName = it[Constants.NAME].toString(), imageUrl = it[Constants.DP].toString())
                    if ((model as CircleModel).adminReference.path == friend.friendReference.path) {
                        friend.friendName += " (Admin)"
                    }
                    if (friend.uid != Database.currentUser?.uid) {
                        membersList.add(friend)
                        (adapter as FriendsAdapter).notifyDataSetChanged()
                        var friendCircles = ArrayList<DocumentReference>()
                        var myCircle = ArrayList<DocumentReference>()
                        try {
                            friend.friendReference.get().addOnSuccessListener { friendRef ->
                                friendCircles = friendRef[Constants.CIRCLES] as ArrayList<DocumentReference>
                                Database.currentUser?.documentReference?.get()?.addOnSuccessListener { myRef ->
                                    myCircle = myRef[Constants.CIRCLES] as ArrayList<DocumentReference>

                                    for (c1 in friendCircles) {
                                        for (c2 in myCircle) {
                                            if (c1.path == c2.path) {
                                                friend.commonCirclesCount++
                                                (adapter as FriendsAdapter).notifyDataSetChanged()
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (e: NullPointerException) {
                        }
                    }
                }
            }
        }
    }


    //Circles Click Listeners
    override fun onCircleClicked(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onCircleLongClicked(position: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun onCirclePhotoClicked(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onInfoClicked(position: Int) {
        TODO("Not yet implemented")
    }

    //Friends Click Listeners
    override fun onFriendClicked(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onFriendLongClicked(position: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun onFriendPhotoClicked(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onFriendInfoClicked(position: Int) {
        TODO("Not yet implemented")
    }
}