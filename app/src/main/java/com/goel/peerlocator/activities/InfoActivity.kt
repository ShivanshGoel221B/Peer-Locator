package com.goel.peerlocator.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goel.peerlocator.R
import com.goel.peerlocator.adapters.CirclesAdapter
import com.goel.peerlocator.databinding.ActivityInfoBinding
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.models.UserModel
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.Database
import com.goel.peerlocator.utils.firebase.FriendDataListener
import com.google.firebase.firestore.DocumentReference
import com.squareup.picasso.Picasso

class InfoActivity : AppCompatActivity(), FriendDataListener, CirclesAdapter.CircleClickListener {

    companion object {
        lateinit var model : Any
    }
    private lateinit var binding: ActivityInfoBinding
    private lateinit var commonList: ArrayList<CircleModel>
    private lateinit var adapter : CirclesAdapter

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
        Picasso.with(this).load(photoUrl).placeholder(R.drawable.ic_placeholder_user_big).into(binding.profileImageHolder)
        val ref = model.friendReference
        Database.getFriendInfo(this, ref)
    }

    private fun setData (model : CircleModel) {

    }

    private fun createRecyclerView () {
        commonList = ArrayList()
        adapter = CirclesAdapter(commonList, this, this)
        binding.infoCommonCircles.adapter = adapter
        val lm = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.infoCommonCircles.layoutManager = lm
    }


    //Listeners
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
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onFriendRemoved() {
        TODO("Not yet implemented")
    }

    override fun onFriendBlocked() {
        TODO("Not yet implemented")
    }


    //Click Listeners
    override fun onCircleClicked(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onCirclePhotoClicked(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onInfoClicked(position: Int) {
        TODO("Not yet implemented")
    }
}