package com.goel.peerlocator.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.goel.peerlocator.R
import com.goel.peerlocator.databinding.ActivityUserInfoBinding
import com.goel.peerlocator.dialogs.InvitationLoadingDialog
import com.goel.peerlocator.dialogs.LoadingBasicDialog
import com.goel.peerlocator.listeners.AddFriendListener
import com.goel.peerlocator.listeners.EditFriendListener
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.repositories.FriendsRepository
import com.goel.peerlocator.repositories.InvitesRepository

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
        Glide.with(this).load(photoUrl)
            .placeholder(R.drawable.ic_placeholder_user_big)
            .into(binding.profileImageHolder)
    }

    private fun setClickListeners () {
        binding.sendInvitationButton.setOnClickListener {
            sendInvitation()
        }
        binding.blockButton.setOnClickListener {
            showBlockWarning()
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

    private fun showBlockWarning () {
        AlertDialog.Builder(this)
            .setTitle(R.string.block)
            .setMessage("Are you sure you want to block ${model.name}?")
            .setNegativeButton(R.string.no) {dialog, _ -> dialog.dismiss()}
            .setPositiveButton(R.string.yes) { dialog, _ ->
                dialog.dismiss()
                blockUser()
            }.show()
    }

    private fun blockUser() {
        val loading = LoadingBasicDialog("Blocking User")
        loading.show(supportFragmentManager, "Remove User")

        FriendsRepository.instance.blockUser(model.documentReference, object :
            EditFriendListener {
            override fun onFriendRemoved() {}

            override fun onFriendBlocked() {
                loading.dismiss()
                Toast.makeText(this@UserInfoActivity, "${model.name} blocked", Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onError() {
                loading.dismiss()
                Toast.makeText(this@UserInfoActivity, R.string.error_message, Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }
}