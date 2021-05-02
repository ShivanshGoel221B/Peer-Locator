package com.goel.peerlocator.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goel.peerlocator.activities.FriendActivity
import com.goel.peerlocator.adapters.FriendsAdapter
import com.goel.peerlocator.databinding.FriendsFragmentBinding
import com.goel.peerlocator.utils.firebase.Database
import com.goel.peerlocator.viewmodels.FriendsViewModel

class FriendsFragment : Fragment(), FriendsAdapter.FriendClickListener {

    companion object {
        fun newInstance() = FriendsFragment()
    }

    private var binding : FriendsFragmentBinding? = null
    private lateinit var viewModel: FriendsViewModel
    private lateinit var friendsAdapter: FriendsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FriendsFragmentBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val swipe = binding?.swipeLayout!!
        swipe.setOnRefreshListener {
            onResume()
            swipe.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()

        val shimmer = binding?.shimmerLayout!!
        shimmer.startShimmerAnimation()
        shimmer.visibility = View.VISIBLE

        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(activity!!.application))
            .get(FriendsViewModel::class.java)

        friendsAdapter = FriendsAdapter(viewModel.friendsList.value!!, context!!, this)

        setAdapter()

        viewModel.friendsList.observe(this) {
            friendsAdapter.notifyDataSetChanged()
        }

        viewModel.getAllFriends(friendsAdapter, shimmer)

    }

    override fun onPause() {
        super.onPause()
        viewModel.friendsList.value?.clear()
    }

    private fun setAdapter() {
        binding?.friendsRecyclerView?.adapter = friendsAdapter
        val lm = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding?.friendsRecyclerView?.layoutManager = lm
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


    // Click Listeners
    override fun onFriendClicked(position: Int) {
        val friend = viewModel.friendsList.value?.get(position)
        Database.currentFriend = friend
        startActivity(Intent(context, FriendActivity::class.java))
    }

    override fun onFriendPhotoClicked(position: Int) {

    }
}