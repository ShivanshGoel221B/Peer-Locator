package com.goel.peerlocator.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goel.peerlocator.R
import com.goel.peerlocator.activities.CircleInfoActivity
import com.goel.peerlocator.activities.NewCircleActivity
import com.goel.peerlocator.adapters.CirclesAdapter
import com.goel.peerlocator.databinding.CirclesFragmentBinding
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.models.InviteModel
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.viewmodels.CirclesViewModel

class CirclesFragment : Fragment(), CirclesAdapter.CircleClickListener {

    companion object {
        fun newInstance() = CirclesFragment()
    }

    private lateinit var viewModel: CirclesViewModel
    private var binding : CirclesFragmentBinding? = null
    private lateinit var circlesAdapter: CirclesAdapter
    private lateinit var nothingFound : LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = CirclesFragmentBinding.inflate(inflater, container, false)
        nothingFound = binding?.nothingFound!!
        nothingFound.visibility = View.GONE
        binding?.createNewCircleButton?.setOnClickListener {
            startActivity(Intent(activity, NewCircleActivity::class.java))
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
            .get(CirclesViewModel::class.java)
        //initialize adapter
        circlesAdapter = CirclesAdapter(viewModel.circleList, context!!, this)
        setAdapter()

        viewModel.getAllCircles(object : GetListListener {
            override fun onFriendRetrieved(friend: FriendModel) {}
            override fun onCircleRetrieved(circle: CircleModel) {
                stopShimmer()
                nothingFound.visibility = View.GONE
                viewModel.circleList.add(circle)
                circlesAdapter.notifyDataSetChanged()
            }
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
        binding?.circleRecyclerView?.adapter = circlesAdapter
        val lm = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding?.circleRecyclerView?.layoutManager = lm
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


    //Click Listeners
    override fun onCircleClicked(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onCirclePhotoClicked(position: Int) {
        val model = viewModel.circleList[position]
        val imageViewFragment = ImageViewFragment.newInstance(url = model.imageUrl, isCircle = true)
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.addToBackStack(Constants.DP)
        transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
        transaction.replace(R.id.profile_photo_container, imageViewFragment, Constants.DP)
        transaction.commit()
    }

    override fun onInfoClicked(position: Int) {
        CircleInfoActivity.model = viewModel.circleList[position]
        startActivity(Intent(activity, CircleInfoActivity::class.java))
    }
}