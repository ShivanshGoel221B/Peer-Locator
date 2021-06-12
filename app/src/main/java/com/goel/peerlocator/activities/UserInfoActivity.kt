package com.goel.peerlocator.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.goel.peerlocator.R
import com.goel.peerlocator.databinding.ActivityUserInfoBinding
import com.goel.peerlocator.dialogs.InvitationLoadingDialog
import com.goel.peerlocator.listeners.AddFriendListener
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.repositories.InvitesRepository
import com.squareup.picasso.Picasso

class UserInfoActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        lateinit var model : UnknownUserModel
    }

    private lateinit var binding: ActivityUserInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
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
    }

    private fun setClickListeners () {
        binding.sendInvitationButton.setOnClickListener {
            sendInvitation()
        }
    }

    private fun sendInvitation() {
        val loading = InvitationLoadingDialog("Sending Invitation")
        loading.show(supportFragmentManager, "sending invitation")
        InvitesRepository.instance.sendInvitation(model, object : AddFriendListener {
            override fun onInvitationSent(model: UnknownUserModel) {
                loading.dismiss()
                Toast.makeText(this@UserInfoActivity, R.string.invitation_sent, Toast.LENGTH_SHORT)
                    .show()
                finish()
            }

            override fun onInvitationUnsent(model: UnknownUserModel) {}

            override fun onError() {
                loading.dismiss()
                Toast.makeText(this@UserInfoActivity, R.string.error_message, Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}