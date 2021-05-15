package com.goel.peerlocator.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.goel.peerlocator.databinding.ActivityAddFriendBinding

class AddFriendActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAddFriendBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}