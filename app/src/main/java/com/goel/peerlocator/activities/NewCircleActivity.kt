package com.goel.peerlocator.activities

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.goel.peerlocator.R
import com.goel.peerlocator.adapters.NewCircleAdapter
import com.goel.peerlocator.databinding.ActivityNewCircleBinding
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.Database
import com.goel.peerlocator.viewmodels.NewCircleViewModel
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class NewCircleActivity : AppCompatActivity(), NewCircleAdapter.NewCircleClickListener {

    private lateinit var binding : ActivityNewCircleBinding
    private lateinit var viewModel: NewCircleViewModel
    private lateinit var adapter : NewCircleAdapter
    private lateinit var membersCounter : TextView
    private var membersCount : Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCircleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setViews()
        setRecyclerView()
        setClickListeners()
    }

    private fun setViews () {
        membersCounter = binding.circleMembersCounter
        Picasso.with(this)
            .load(Database.currentUser?.photoUrl)
            .placeholder(R.drawable.ic_placeholder_user)
            .transform(CropCircleTransformation())
            .into(binding.myProfilePicture)
        membersCount = 1
        updateCounter(membersCount)
    }

    private fun updateCounter (number : Int) {
        membersCounter.text = resources.getQuantityString(R.plurals.members_count, number, number)
    }

    private fun setRecyclerView() {
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))
            .get(NewCircleViewModel::class.java)
        adapter = NewCircleAdapter(this, viewModel.membersList, this)
        binding.membersRecyclerView.adapter = adapter

        val manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.membersRecyclerView.layoutManager = manager
    }

    private fun setClickListeners() {
        binding.addMembersButton.setOnClickListener {
            if (membersCount < Constants.MAX_CIRCLE_SIZE) {

            }
            else {
                Toast.makeText(this, R.string.circle_size_warning, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRemoveClick(position: Int) {
        adapter.notifyItemRemoved(position)
        viewModel.membersList.removeAt(position)
    }
}