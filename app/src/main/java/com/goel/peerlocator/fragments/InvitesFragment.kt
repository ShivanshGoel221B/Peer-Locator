package com.goel.peerlocator.fragments

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
import com.goel.peerlocator.adapters.InvitesAdapter
import com.goel.peerlocator.databinding.InvitesFragmentBinding
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.models.InviteModel
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.services.ServicesHandler
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.viewmodels.InvitesViewModel

class InvitesFragment : Fragment(), InvitesAdapter.InviteClickListener {

    companion object {
        fun newInstance() = InvitesFragment()
    }

    private var binding : InvitesFragmentBinding? = null
    private lateinit var viewModel: InvitesViewModel
    private lateinit var invitesAdapter: InvitesAdapter
    private lateinit var nothingFound : LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = InvitesFragmentBinding.inflate(inflater, container, false)
        nothingFound = binding?.nothingFound!!
        nothingFound.visibility = View.GONE
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

        ServicesHandler.stopInviteNotification(activity!!)
    }

    private fun createRecyclerView () {
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(activity!!.application))
            .get(InvitesViewModel::class.java)
        //initialize adapter
        invitesAdapter = InvitesAdapter(viewModel.invitesList, context!!, this)
        setAdapter()

        viewModel.getAllInvites(object : GetListListener {
            override fun onFriendRetrieved(friend: FriendModel) {}
            override fun onCircleRetrieved(circle: CircleModel) {}
            override fun onUserRetrieved(user: UnknownUserModel) {}

            override fun onInvitationRetrieved(invitation: InviteModel) {
                stopShimmer()
                nothingFound.visibility = View.GONE
                viewModel.invitesList.add(invitation)
                invitesAdapter.notifyDataSetChanged()
            }

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

    override fun onPause() {
        super.onPause()
        ServicesHandler.startInviteNotification(activity!!)
        viewModel.invitesList.clear()
        invitesAdapter.notifyDataSetChanged()
    }

    private fun setAdapter() {
        binding?.invitesRecyclerView?.adapter = invitesAdapter
        val lm = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding?.invitesRecyclerView?.layoutManager = lm
    }

    private fun startShimmer () {
        binding?.shimmerLayout?.visibility = View.VISIBLE
        binding?.shimmerLayout?.startShimmerAnimation()
    }

    private fun stopShimmer () {
        binding?.shimmerLayout?.visibility = View.GONE
        binding?.shimmerLayout?.stopShimmerAnimation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


    //Click Listeners
    override fun onInviteClicked(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onInvitePhotoClicked(position: Int) {
        val model = viewModel.invitesList[position]
        val isCircle = Constants.CIRCLES in model.documentReference.path
        val imageViewFragment = ImageViewFragment.newInstance(url = model.imageUrl, isCircle = isCircle)
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.addToBackStack(Constants.DP)
        transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
        transaction.replace(R.id.profile_photo_container, imageViewFragment, Constants.DP)
        transaction.commit()
    }

    override fun onAcceptClicked(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onRejectClicked(position: Int) {
        TODO("Not yet implemented")
    }
}