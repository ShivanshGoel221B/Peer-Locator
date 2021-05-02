package com.goel.peerlocator.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.goel.peerlocator.databinding.ActivityInfoBinding
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.models.UserModel

class InfoActivity : AppCompatActivity() {

    private lateinit var model : Any
    private lateinit var binding: ActivityInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setViews()
    }

    private fun setViews () {
        when (model) {
            is UserModel -> {
                binding.profileInformation.visibility = View.GONE
                binding.unknownPrivacy.visibility = View.VISIBLE
                binding.sendInvitationButton.visibility = View.VISIBLE
                binding.removeButton.visibility = View.GONE
                binding.blockButton.visibility = View.VISIBLE
                binding.reportButton.visibility = View.VISIBLE

                setData (model as UserModel)
            }
            is FriendModel -> {
                binding.profileInformation.visibility = View.VISIBLE
                binding.unknownPrivacy.visibility = View.GONE
                binding.sendInvitationButton.visibility = View.GONE
                binding.removeButton.visibility = View.VISIBLE
                binding.blockButton.visibility = View.VISIBLE
                binding.reportButton.visibility = View.VISIBLE
            }
            is CircleModel -> {
                binding.profileInformation.visibility = View.VISIBLE
                binding.unknownPrivacy.visibility = View.GONE
                binding.sendInvitationButton.visibility = View.GONE
                binding.removeButton.visibility = View.VISIBLE
                binding.blockButton.visibility = View.VISIBLE
                binding.reportButton.visibility = View.VISIBLE
            }
        }
    }

    private fun setData(model: UserModel) {
        val photoUrl = model.photoUrl
        val name = model.displayName

    }

    private fun setData (model : FriendModel) {

    }

    private fun setData (model : CircleModel) {

    }

}