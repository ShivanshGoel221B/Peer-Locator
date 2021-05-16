package com.goel.peerlocator.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.goel.peerlocator.R
import com.goel.peerlocator.adapters.AddMembersAdapter
import com.goel.peerlocator.databinding.AddMembersFragmentBinding
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.viewmodels.AddMembersViewModel

class AddMembersFragment : Fragment(), AddMembersAdapter.AddMembersClickListeners {

    companion object {
        fun newInstance(alreadyAdded : ArrayList<FriendModel>) = AddMembersFragment().apply {
            this.alreadyAdded = alreadyAdded
            this.initialCount = alreadyAdded.size + 1
        }
    }

    private var binding: AddMembersFragmentBinding? = null
    private lateinit var viewModel: AddMembersViewModel
    private lateinit var adapter : AddMembersAdapter
    private lateinit var alreadyAdded: ArrayList<FriendModel>
    private var initialCount : Int = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = AddMembersFragmentBinding.inflate(inflater, container, false)
        binding?.root?.setOnClickListener { return@setOnClickListener }

        initializeViewModel()
        updateCounter()
        binding?.cancelButton?.setOnClickListener { activity!!.onBackPressed() }
        binding?.addMembersButton?.setOnClickListener {
            viewModel.selectedList.forEach {
                alreadyAdded.add(it)
            }
            activity!!.onBackPressed()
        }
        return binding?.root
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(activity!!.application))
            .get(AddMembersViewModel::class.java)
        createRecyclerView()

        viewModel.getFriendsList(alreadyAdded, object : GetListListener {
            override fun onFriendRetrieved(friend: FriendModel) {
                viewModel.friendList.add(friend)
                adapter.notifyDataSetChanged()
            }
            override fun onCircleRetrieved(circle: CircleModel) {}
            override fun onUserRetrieved(user: UnknownUserModel) {}
            override fun foundEmptyList() {}
            override fun onError() {
                Toast.makeText(context, R.string.error_message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createRecyclerView () {
        adapter = AddMembersAdapter(context!!, viewModel.friendList, viewModel.selectedList, this)
        binding?.friendsRecyclerView?.adapter = adapter
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding?.friendsRecyclerView?.layoutManager = layoutManager
    }

    private fun updateCounter () {
        val count = initialCount + viewModel.addedCount
        binding?.membersCounter?.text = resources.getQuantityString(R.plurals.members_count, count, count)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onAddClicked(position: Int) {
        val count = initialCount + ++viewModel.addedCount
        if (count > Constants.MAX_CIRCLE_SIZE){
            Toast.makeText(context, R.string.circle_size_warning, Toast.LENGTH_LONG).show()
            return
        }
        viewModel.selectedList.add(viewModel.friendList[position])
        updateCounter()
        adapter.notifyItemChanged(position)
    }

    override fun onRemoveClicked(position: Int) {
        viewModel.selectedList.remove(viewModel.friendList[position])
        viewModel.addedCount--
        updateCounter()
        adapter.notifyItemChanged(position)
    }

}