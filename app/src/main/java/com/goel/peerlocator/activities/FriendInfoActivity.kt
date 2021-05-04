package com.goel.peerlocator.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goel.peerlocator.R
import com.goel.peerlocator.adapters.CirclesAdapter
import com.goel.peerlocator.databinding.ActivityFriendInfoBinding
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.Database
import com.goel.peerlocator.listeners.FriendDataListener
import com.google.firebase.firestore.DocumentReference
import com.squareup.picasso.Picasso

class FriendInfoActivity : AppCompatActivity(), FriendDataListener, CirclesAdapter.CircleClickListener {

    companion object {
        lateinit var model : FriendModel
    }

    private lateinit var binding: ActivityFriendInfoBinding
    private lateinit var commonList: ArrayList<CircleModel>
    private lateinit var adapter : CirclesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createToolBar()
        setData()
    }

    private fun createToolBar() {
        val toolbar : androidx.appcompat.widget.Toolbar = binding.infoToolbar.root
        setSupportActionBar(toolbar)
        binding.infoToolbar.backButton.setOnClickListener {onBackPressed()}
        binding.infoToolbar.backButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back))
        supportActionBar?.title = ""
    }

    private fun setData () {
        val photoUrl = model.imageUrl
        val name = model.friendName
        binding.infoToolbar.profileName.text = name
        Picasso.with(this).load(photoUrl).placeholder(R.drawable.ic_placeholder_user_big).into(binding.profileImageHolder)
        Database.getFriendInfo(this, model.friendReference)
    }

    private fun createRecyclerView () {
        commonList = ArrayList()
        adapter = CirclesAdapter(commonList, this, this)
        binding.infoCirclesRecyclerView.adapter = adapter
        val lm = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.infoCirclesRecyclerView.layoutManager = lm
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
                        circleName = it[Constants.NAME].toString(), adminReference = it[Constants.ADMIN] as DocumentReference
                    )
                    try {
                        circle.memberCount = (it[Constants.MEMBERS] as ArrayList<Any>).size
                    } catch (e: NullPointerException) { }
                    commonList.add(circle)
                    adapter.notifyDataSetChanged()
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
}