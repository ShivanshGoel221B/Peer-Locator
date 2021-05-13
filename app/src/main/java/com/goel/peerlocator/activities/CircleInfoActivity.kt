package com.goel.peerlocator.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goel.peerlocator.R
import com.goel.peerlocator.adapters.FriendsAdapter
import com.goel.peerlocator.databinding.ActivityCircleInfoBinding
import com.goel.peerlocator.fragments.ImageViewFragment
import com.goel.peerlocator.listeners.CircleDataListener
import com.goel.peerlocator.listeners.UserSearchListener
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.Database
import com.google.firebase.firestore.DocumentReference
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class CircleInfoActivity : AppCompatActivity(), CircleDataListener, FriendsAdapter.FriendClickListener {

    companion object {
        lateinit var model : CircleModel
    }
    private lateinit var binding: ActivityCircleInfoBinding
    private lateinit var membersList : ArrayList<FriendModel>
    private lateinit var adapter : FriendsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCircleInfoBinding.inflate(layoutInflater)
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
        val name = model.name
        binding.infoToolbar.profileName.text = name
        Picasso.with(this).load(photoUrl).placeholder(R.drawable.ic_placeholder_circle_big).into(binding.profileImageHolder)
        Database.getCircleInfo(this, model.documentReference)
    }

    private fun createRecyclerView () {
        membersList = ArrayList()
        adapter = FriendsAdapter(membersList, this, this)
        binding.infoMembersRecyclerView.adapter = adapter
        val lm = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.infoMembersRecyclerView.layoutManager = lm
    }


    private fun showBlockedMessage () {

    }

    // Circle Listeners
    override fun onMemberCountComplete(members: Long) {
        binding.infoMembersCount.text = resources.getQuantityString(R.plurals.members_count, members.toInt(), members)
    }

    override fun onMembersRetrieved(references: ArrayList<DocumentReference>) {
        createRecyclerView()

        for (ref in references) {
            ref.get().addOnSuccessListener {
                if (it.exists()) {
                    val friend = FriendModel(documentReference = it.reference, uid = it.reference.path.substring(6),
                                            name = it[Constants.NAME].toString(), imageUrl = it[Constants.DP].toString())
                    if (model.adminReference.path == friend.documentReference.path) {
                        binding.infoAdminCard.cardProfileName.text = friend.name
                        Picasso.with(this).load(friend.imageUrl).placeholder(R.drawable.ic_placeholder_user)
                            .transform(CropCircleTransformation())
                            .into(binding.infoAdminCard.cardProfileImage)
                        if (friend.documentReference.path == Database.currentUser?.documentReference?.path) {
                            binding.infoAdminCard.cardProfileName.text = getString(R.string.you)
                            binding.infoAdminCard.cardAdditionalDetail.visibility = View.GONE
                            binding.infoAdminCard.cardInfo.visibility = View.GONE
                        }
                        else {
                            try {
                                var friendCircles: ArrayList<DocumentReference>
                                var myCircle: ArrayList<DocumentReference>
                                friend.documentReference.get().addOnSuccessListener { friendRef ->
                                    friendCircles = friendRef[Constants.CIRCLES] as ArrayList<DocumentReference>
                                    Database.currentUser?.documentReference?.get()?.addOnSuccessListener { myRef ->
                                        myCircle = myRef[Constants.CIRCLES] as ArrayList<DocumentReference>
                                        var counter = 1
                                        for (c1 in friendCircles) {
                                            for (c2 in myCircle) {
                                                if (c1.path == c2.path) {
                                                    counter++
                                                }
                                            }
                                        }
                                        binding.infoAdminCard.
                                        cardAdditionalDetail.text = resources
                                            .getQuantityString(R.plurals.common_circles_count, counter, counter)
                                    }
                                }
                            } catch (e: NullPointerException) { }
                        }
                    }
                    else if (friend.uid != Database.currentUser?.uid) {
                        membersList.add(friend)
                        adapter.notifyDataSetChanged()
                        var friendCircles: ArrayList<DocumentReference>
                        var myCircle: ArrayList<DocumentReference>
                        try {
                            friend.documentReference.get().addOnSuccessListener { friendRef ->
                                friendCircles = friendRef[Constants.CIRCLES] as ArrayList<DocumentReference>
                                Database.currentUser?.documentReference?.get()?.addOnSuccessListener { myRef ->
                                    myCircle = myRef[Constants.CIRCLES] as ArrayList<DocumentReference>

                                    for (c1 in friendCircles) {
                                        for (c2 in myCircle) {
                                            if (c1.path == c2.path) {
                                                friend.commonCirclesCount++
                                                adapter.notifyDataSetChanged()
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (e: NullPointerException) { }
                    }
                }
            }
        }
    }


    //Friends Click Listeners
    override fun onFriendClicked(position: Int) {
        Database.findUser(membersList[position].documentReference, object : UserSearchListener {
            override fun userFound(user: UnknownUserModel) {
                UserInfoActivity.model = user
                startActivity(Intent(applicationContext, UserInfoActivity::class.java))
                Toast.makeText(applicationContext, "You must be friend with this person to track", Toast.LENGTH_LONG).show()
            }

            override fun friendFound(friend: FriendModel) {
                FriendActivity.friend = friend
                startActivity(Intent(applicationContext, FriendActivity::class.java))
            }

            override fun blockedFound() {
                showBlockedMessage ()
            }

            override fun networkError() {
                Toast.makeText(applicationContext, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onFriendLongClicked(position: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun onFriendPhotoClicked(position: Int) {
        val model = membersList[position]
        val imageViewFragment = ImageViewFragment.newInstance(url = model.imageUrl, isCircle = false)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.addToBackStack(Constants.DP)
        transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
        transaction.replace(R.id.profile_photo_container, imageViewFragment, Constants.DP)
        transaction.commit()
    }

    override fun onFriendInfoClicked(position: Int) {
        Database.findUser(membersList[position].documentReference, object : UserSearchListener {
            override fun userFound(user: UnknownUserModel) {
                UserInfoActivity.model = user
                startActivity(Intent(applicationContext, UserInfoActivity::class.java))
            }

            override fun friendFound(friend: FriendModel) {
                FriendInfoActivity.model = friend
                startActivity(Intent(applicationContext, FriendInfoActivity::class.java))
            }

            override fun blockedFound() {
                showBlockedMessage ()
            }

            override fun networkError() {
                Toast.makeText(applicationContext, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}