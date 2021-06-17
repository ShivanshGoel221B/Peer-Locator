package com.goel.peerlocator.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goel.peerlocator.R
import com.goel.peerlocator.adapters.CirclesAdapter
import com.goel.peerlocator.databinding.ActivityFriendInfoBinding
import com.goel.peerlocator.dialogs.LoadingBasicDialog
import com.goel.peerlocator.fragments.ImageViewFragment
import com.goel.peerlocator.listeners.EditFriendListener
import com.goel.peerlocator.listeners.FriendDataListener
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.repositories.FriendsRepository
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.database.FriendsDatabase
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
        setClickListeners()
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
        Picasso.with(this).load(photoUrl).placeholder(R.drawable.ic_placeholder_user_big).into(binding.profileImageHolder)
        FriendsDatabase.instance.getFriendInfo(this, model.documentReference)
    }

    private fun createRecyclerView () {
        commonList = ArrayList()
        adapter = CirclesAdapter(commonList, this, this)
        binding.infoCirclesRecyclerView.adapter = adapter
        val lm = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.infoCirclesRecyclerView.layoutManager = lm
    }

    private fun setClickListeners () {
        binding.removeButton.setOnClickListener {
            showRemoveWarning()
        }
        binding.blockButton.setOnClickListener {
            showBlockWarning()
        }
    }

    private fun showRemoveWarning () {
        AlertDialog.Builder(this)
            .setTitle(R.string.remove_friend)
            .setMessage("Are you sure you want to remove ${model.name} as your friend?")
            .setNegativeButton(R.string.no) {dialog, _ -> dialog.dismiss()}
            .setPositiveButton(R.string.yes) { dialog, _ ->
                dialog.dismiss()
                removeFriend()
            }.show()
    }

    private fun showBlockWarning () {
        AlertDialog.Builder(this)
            .setTitle(R.string.block)
            .setMessage("Are you sure you want to block ${model.name}?")
            .setNegativeButton(R.string.no) {dialog, _ -> dialog.dismiss()}
            .setPositiveButton(R.string.yes) { dialog, _ ->
                dialog.dismiss()
                blockFriend()
            }.show()
    }

    private fun removeFriend() {
        val loading = LoadingBasicDialog("Removing Friend")
        loading.show(supportFragmentManager, "Remove Friend")

        FriendsRepository.instance.removeFriend(model.documentReference, object : EditFriendListener {
            override fun onFriendRemoved() {
                loading.dismiss()
                Toast.makeText(this@FriendInfoActivity, "Friend Removed", Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onFriendBlocked() {}

            override fun onError() {
                loading.dismiss()
                Toast.makeText(this@FriendInfoActivity, R.string.error_message, Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun blockFriend () {
        val loading = LoadingBasicDialog("Removing Friend")
        loading.show(supportFragmentManager, "Remove Friend")

        FriendsRepository.instance.blockFriend(model.documentReference, object : EditFriendListener {
            override fun onFriendRemoved() {
                loading.setMessage("Blocking User")
            }

            override fun onFriendBlocked() {
                loading.dismiss()
                Toast.makeText(this@FriendInfoActivity, "${model.name} blocked", Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onError() {
                loading.dismiss()
                Toast.makeText(this@FriendInfoActivity, R.string.error_message, Toast.LENGTH_SHORT).show()
                finish()
            }
        })
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
                    val circle = CircleModel(documentReference = it.reference, imageUrl = it[Constants.DP].toString(),
                        name = it[Constants.NAME].toString(), adminReference = it[Constants.ADMIN] as DocumentReference
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
        CircleActivity.model = commonList[position]
        startActivity(Intent(this, CircleActivity::class.java))
    }

    override fun onCirclePhotoClicked(position: Int) {
        val model = commonList[position]
        val imageViewFragment = ImageViewFragment.newInstance(url = model.imageUrl, isCircle = true)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.addToBackStack(Constants.DP)
        transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
        transaction.replace(R.id.profile_photo_container, imageViewFragment, Constants.DP)
        transaction.commit()
    }

    override fun onInfoClicked(position: Int) {
        CircleInfoActivity.model = commonList[position]
        startActivity(Intent(this, CircleInfoActivity::class.java))
    }
}