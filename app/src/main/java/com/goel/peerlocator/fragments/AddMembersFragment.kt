package com.goel.peerlocator.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.goel.peerlocator.R
import com.goel.peerlocator.activities.CircleInfoActivity
import com.goel.peerlocator.activities.NewCircleActivity
import com.goel.peerlocator.adapters.AddMembersAdapter
import com.goel.peerlocator.databinding.AddMembersFragmentBinding
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.models.InviteModel
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.viewmodels.AddMembersViewModel
import java.util.*

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

        binding?.nothingFound?.visibility = View.GONE
        initializeViewModel()
        updateCounter()
        binding?.cancelButton?.setOnClickListener { requireActivity().onBackPressed() }
        binding?.addMembersButton?.setOnClickListener {
            viewModel.selectedList.forEach {
                alreadyAdded.add(it)
            }
            try {
                (activity as NewCircleActivity).membersAdded()
            } catch (e: ClassCastException) {
                (activity as CircleInfoActivity).membersAdded()
            }
            requireActivity().onBackPressed()
        }
        binding?.searchBar?.setOnQueryTextListener (object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                searchInFriends(newText)
                return true
            }
        })
        return binding?.root
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))
            .get(AddMembersViewModel::class.java)
        createRecyclerView()

        viewModel.getFriendsList(alreadyAdded, object : GetListListener {
            override fun onFriendRetrieved(friend: FriendModel) {
                viewModel.friendList.add(friend)
                adapter.notifyDataSetChanged()
            }
            override fun onCircleRetrieved(circle: CircleModel) {}
            override fun onUserRetrieved(user: UnknownUserModel) {}
            override fun onInvitationRetrieved(invitation: InviteModel) {}

            override fun foundEmptyList() {
                binding?.nothingFound?.visibility = View.VISIBLE
            }
            override fun onError() {
                Toast.makeText(context, R.string.error_message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createRecyclerView () {
        adapter = AddMembersAdapter(requireContext(), viewModel.friendList, viewModel.selectedList, this)
        binding?.friendsRecyclerView?.adapter = adapter
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding?.friendsRecyclerView?.layoutManager = layoutManager
    }

    private fun searchInFriends (query: String?) {
        query?.let {
            if (it.isNotEmpty()) {
                val newList = viewModel.friendList.filter { friend ->
                    query.toLowerCase(Locale.ROOT) in friend.name.toLowerCase(Locale.ROOT)
                } as ArrayList<FriendModel>
                binding?.friendsRecyclerView?.adapter = AddMembersAdapter(requireContext(), newList,
                                                        viewModel.selectedList, this)
                if (newList.isEmpty())
                    binding?.nothingFound?.visibility = View.VISIBLE
                else
                    binding?.nothingFound?.visibility = View.GONE
            }
            else {
                binding?.friendsRecyclerView?.adapter = adapter
                if (viewModel.friendList.isNotEmpty())
                    binding?.nothingFound?.visibility = View.GONE
            }
        }
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