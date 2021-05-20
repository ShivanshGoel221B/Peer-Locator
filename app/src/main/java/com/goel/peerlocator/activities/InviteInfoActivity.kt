package com.goel.peerlocator.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.goel.peerlocator.databinding.ActivityInviteInfoBinding
import com.goel.peerlocator.models.InviteModel

class InviteInfoActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        lateinit var model: InviteModel
    }

    private lateinit var binding: ActivityInviteInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInviteInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}