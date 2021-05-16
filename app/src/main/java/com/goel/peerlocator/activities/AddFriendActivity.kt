package com.goel.peerlocator.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.goel.peerlocator.R
import com.goel.peerlocator.adapters.AddFriendAdapter
import com.goel.peerlocator.databinding.ActivityAddFriendBinding
import com.goel.peerlocator.fragments.ImageViewFragment
import com.goel.peerlocator.listeners.AddFriendListener
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.viewmodels.AddFriendViewModel

class AddFriendActivity : AppCompatActivity(), AddFriendAdapter.ClickListeners {

    private lateinit var binding : ActivityAddFriendBinding
    private lateinit var viewModel: AddFriendViewModel
    private lateinit var adapter : AddFriendAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolBar()
        binding.swipeLayout.setOnRefreshListener { onResume() }
    }

    override fun onResume() {
        super.onResume()
        createRecyclerView()
    }


    private fun setToolBar() {
        val toolbar : androidx.appcompat.widget.Toolbar = binding.toolbar.root
        setSupportActionBar(toolbar)
        binding.toolbar.backButton.setOnClickListener {onBackPressed()}
        binding.toolbar.backButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back))
        supportActionBar?.title = ""
        binding.toolbar.profileName.text = getString(R.string.add_new_friend)
    }

    private fun createRecyclerView() {
        startShimmer()
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))
            .get(AddFriendViewModel::class.java)
        adapter = AddFriendAdapter(this, viewModel.usersList, this)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.usersRecyclerView.layoutManager = layoutManager
        binding.usersRecyclerView.adapter = adapter
        viewModel.usersList.clear()
        adapter.notifyDataSetChanged()
        viewModel.getAllUsers(object : GetListListener {
            override fun onFriendRetrieved(friend: FriendModel) {}
            override fun onCircleRetrieved(circle: CircleModel) {}

            override fun onUserRetrieved(user: UnknownUserModel) {
                viewModel.usersList.add(user)
                binding.nothingFound.visibility = View.GONE
                stopShimmer()
                adapter.notifyDataSetChanged()
            }

            override fun foundEmptyList() {
                stopShimmer()
                binding.nothingFound.visibility = View.VISIBLE
            }

            override fun onError() {
                Toast.makeText(this@AddFriendActivity, R.string.error_message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun startShimmer () {
        binding.nothingFound.visibility = View.GONE
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.shimmerLayout.startShimmerAnimation()
    }
    private fun stopShimmer () {
        binding.shimmerLayout.visibility = View.GONE
        binding.shimmerLayout.stopShimmerAnimation()
        binding.swipeLayout.isRefreshing = false
    }

    override fun onInviteClicked(position: Int) {
        viewModel.sendInvitation(position, object :  AddFriendListener{
            override fun onInvitationSent(model: UnknownUserModel) {
                val index = viewModel.usersList.indexOf(model)
                adapter.notifyItemRemoved(index)
                viewModel.usersList.remove(model)
                Toast.makeText(this@AddFriendActivity, R.string.invitation_sent, Toast.LENGTH_SHORT).show()
                if (viewModel.usersList.isEmpty()) {
                    stopShimmer()
                    binding.nothingFound.visibility = View.VISIBLE
                }
            }

            override fun onError() {
                Toast.makeText(this@AddFriendActivity, R.string.error_message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onUserClicked(position: Int) {
        UserInfoActivity.model = viewModel.usersList[position]
        startActivity(Intent(this, UserInfoActivity::class.java))
    }

    override fun onPhotoClicked(position: Int) {
        val url = viewModel.usersList[position].imageUrl
        val imageViewFragment = ImageViewFragment.newInstance(url = url, isCircle = false)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.addToBackStack(Constants.DP)
        transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
        transaction.replace(R.id.image_fragment_container, imageViewFragment, Constants.DP)
        transaction.commit()
    }
}