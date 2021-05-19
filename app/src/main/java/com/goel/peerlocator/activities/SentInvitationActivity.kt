package com.goel.peerlocator.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.goel.peerlocator.R
import com.goel.peerlocator.adapters.SentInvitationAdapter
import com.goel.peerlocator.databinding.ActivitySentInvitationBinding
import com.goel.peerlocator.dialogs.InvitationLoadingDialog
import com.goel.peerlocator.listeners.AddFriendListener
import com.goel.peerlocator.listeners.GetListListener
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.models.InviteModel
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.viewmodels.SentInvitationViewModel

class SentInvitationActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySentInvitationBinding
    private lateinit var viewModel: SentInvitationViewModel
    private lateinit var adapter: SentInvitationAdapter
    private var invitationLoading: InvitationLoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySentInvitationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolbar()
        binding.swipeLayout.setOnRefreshListener {
            onResume()
            binding.swipeLayout.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        binding.nothingFound.visibility = View.GONE
        createRecyclerView()
    }

    private fun setToolbar () {
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar.root)
        toolbar.profileName.text = getString(R.string.sent_invitations)
        toolbar.backButton.setOnClickListener { finish() }
        supportActionBar?.title = ""
    }

    private fun createRecyclerView() {
        startShimmer()
        viewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(SentInvitationViewModel::class.java)
        adapter = SentInvitationAdapter(this, viewModel.sentInvitationList, object : SentInvitationAdapter.ClickListener {
            override fun onRemoveClicked(position: Int) {
                showRemoveMessage(viewModel.sentInvitationList[position])
            }
        })
        binding.sentRecyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.sentRecyclerView.layoutManager = layoutManager

        viewModel.getSentInvitations(object : GetListListener {
            override fun onFriendRetrieved(friend: FriendModel) {}
            override fun onCircleRetrieved(circle: CircleModel) {}
            override fun onInvitationRetrieved(invitation: InviteModel) {}

            override fun onUserRetrieved(user: UnknownUserModel) {
                viewModel.sentInvitationList.add(user)
                stopShimmer()
                adapter.notifyDataSetChanged()
            }

            override fun foundEmptyList() {
                stopShimmer()
                binding.nothingFound.visibility = View.VISIBLE
            }

            override fun onError() {
                Toast.makeText(this@SentInvitationActivity, R.string.error_message, Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun showRemoveMessage(model: UnknownUserModel) {
        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Are you sure you want to unsent invitation to ${model.name}")
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.yes) { dialog, _ ->
                dialog.dismiss()
                showLoader()
                unSendInvitation(model)
            }
            .show()
    }

    private fun showLoader () {
        invitationLoading = InvitationLoadingDialog("Removing Invitation")
        invitationLoading?.show(supportFragmentManager, "Loading")
    }

    private fun unSendInvitation(model: UnknownUserModel) {
        viewModel.removeInvitation(model, object : AddFriendListener {
            override fun onInvitationSent(model: UnknownUserModel) {}
            override fun onInvitationUnsent(model: UnknownUserModel) {
                val index = viewModel.sentInvitationList.indexOf(model)
                adapter.notifyItemRemoved(index)
                viewModel.sentInvitationList.remove(model)
                if (viewModel.sentInvitationList.isEmpty())
                    binding.nothingFound.visibility = View.VISIBLE
                invitationLoading?.dismiss()
                Toast.makeText(this@SentInvitationActivity, "Invitation Removed", Toast.LENGTH_SHORT).show()
            }

            override fun onError() {
                invitationLoading?.dismiss()
                Toast.makeText(this@SentInvitationActivity, R.string.error_message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun startShimmer () {
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.shimmerLayout.startShimmerAnimation()
    }

    private fun stopShimmer () {
        binding.shimmerLayout.visibility = View.GONE
        binding.shimmerLayout.stopShimmerAnimation()
    }
}