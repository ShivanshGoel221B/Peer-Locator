package com.goel.peerlocator.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.goel.peerlocator.R
import com.goel.peerlocator.databinding.ActivityFriendInfoBinding
import com.goel.peerlocator.databinding.ActivityUserInfoBinding
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.models.UserModel
import com.goel.peerlocator.utils.firebase.Database
import com.squareup.picasso.Picasso

class UserInfoActivity : AppCompatActivity() {

    companion object {
        lateinit var model : UnknownUserModel
    }

    private lateinit var binding: ActivityUserInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
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
        val photoUrl = model.photoUrl
        val name = model.displayName
        binding.infoToolbar.profileName.text = name
        Picasso.with(this).load(photoUrl).placeholder(R.drawable.ic_placeholder_user_big).into(binding.profileImageHolder)
    }
}