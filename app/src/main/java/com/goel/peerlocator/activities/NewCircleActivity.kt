package com.goel.peerlocator.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.goel.peerlocator.databinding.ActivityNewCircleBinding

class NewCircleActivity : AppCompatActivity() {

    private lateinit var binding : ActivityNewCircleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCircleBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}