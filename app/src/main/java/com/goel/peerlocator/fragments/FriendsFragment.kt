package com.goel.peerlocator.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goel.peerlocator.R
import com.goel.peerlocator.activities.AddFriendActivity
import com.goel.peerlocator.activities.FriendActivity
import com.goel.peerlocator.activities.FriendInfoActivity
import com.goel.peerlocator.adapters.FriendsAdapter
import com.goel.peerlocator.databinding.FriendsFragmentBinding
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.models.InviteModel
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.viewmodels.FriendsViewModel

class FriendsFragment : Fragment(), FriendsAdapter.FriendClickListener {

    companion object {
        fun newInstance() = FriendsFragment()
    }

    private var binding : FriendsFragmentBinding? = null
    private lateinit var viewModel: FriendsViewModel
    private lateinit var friendsAdapter: FriendsAdapter
    private lateinit var nothingFound : LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FriendsFragmentBinding.inflate(inflater, container, false)
        nothingFound = binding?.nothingFound!!
        nothingFound.visibility = View.GONE
        binding?.addNewFriend?.setOnClickListener {
            startActivity(Intent(context, AddFriendActivity::class.java))
        }
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

        startShimmer()
        nothingFound.visibility = View.GONE
        createRecyclerView()
    }

    private fun createRecyclerView () {
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(activity!!.application))
            .get(FriendsViewModel::class.java)
        //initialize adapter
        friendsAdapter = FriendsAdapter(viewModel.friendsList, context!!, this)
        setAdapter()

        viewModel.getAllFriends(object : GetListListener {
            override fun onFriendRetrieved(friend: FriendModel) {
                stopShimmer()
                nothingFound.visibility = View.GONE
                viewModel.friendsList.add(friend)
                friendsAdapter.notifyDataSetChanged()
            }
            override fun onCircleRetrieved(circle: CircleModel) {}
            override fun onUserRetrieved(user: UnknownUserModel) {}
            override fun onInvitationRetrieved(invitation: InviteModel) {}

            override fun foundEmptyList() {
                stopShimmer()
                nothingFound.visibility = View.VISIBLE
            }

            override fun onError() {
                stopShimmer()
                Toast.makeText(context, R.string.error_message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun startShimmer () {
        binding?.shimmerLayout?.visibility = View.VISIBLE
        binding?.shimmerLayout?.startShimmerAnimation()
    }

    private fun stopShimmer () {
        binding?.shimmerLayout?.visibility = View.GONE
        binding?.shimmerLayout?.stopShimmerAnimation()
    }

    private fun setAdapter() {
        binding?.friendsRecyclerView?.adapter = friendsAdapter
        val lm = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding?.friendsRecyclerView?.layoutManager = lm
    }

    override fun onPause() {
        super.onPause()
        viewModel.friendsList.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


    // Click Listeners
    override fun onFriendClicked(position: Int) {
        val friend = viewModel.friendsList[position]
        FriendActivity.friend = friend
        startActivity(Intent(context, FriendActivity::class.java))
    }

    override fun onFriendLongClicked(position: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun onFriendPhotoClicked(position: Int) {
        val model = viewModel.friendsList[position]
        val imageViewFragment = ImageViewFragment.newInstance(url = model.imageUrl, isCircle = false)
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.addToBackStack(Constants.DP)
        transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
        transaction.replace(R.id.profile_photo_container, imageViewFragment, Constants.DP)
        transaction.commit()
    }

    override fun onFriendInfoClicked(position: Int) {
        FriendInfoActivity.model = viewModel.friendsList[position]
        startActivity(Intent(activity, FriendInfoActivity::class.java))
    }
}