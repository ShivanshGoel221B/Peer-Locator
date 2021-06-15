package com.goel.peerlocator.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goel.peerlocator.adapters.BlockListAdapter
import com.goel.peerlocator.databinding.BlockListFragmentBinding
import com.goel.peerlocator.dialogs.LoadingBasicDialog
import com.goel.peerlocator.listeners.BlockListener
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.viewmodels.BlockListViewModel

class BlockListFragment : Fragment(), BlockListAdapter.BlockListClickListener, BlockListener {

    companion object {
        fun newInstance() = BlockListFragment()
    }

    private var binding : BlockListFragmentBinding? = null
    private lateinit var viewModel: BlockListViewModel
    private lateinit var adapter : BlockListAdapter
    private lateinit var nothingFound : LinearLayout
    private lateinit var loading: LoadingBasicDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = BlockListFragmentBinding.inflate(inflater, container, false)
        nothingFound = binding?.nothingFound!!
        nothingFound.visibility = View.GONE
        createRecyclerView()
        binding?.root?.setOnClickListener { return@setOnClickListener }
        binding?.closeButton?.setOnClickListener { requireActivity().onBackPressed() }
        binding?.unblockButton?.setOnClickListener {
            loading = LoadingBasicDialog("Unblocking")
            loading.show(requireActivity().supportFragmentManager, "loading")
            viewModel.unblockSelected(this)
        }
        return binding?.root
    }

    private fun createRecyclerView () {
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))
            .get(BlockListViewModel::class.java)
        adapter = BlockListAdapter(viewModel.blockList, this, requireContext())

        binding?.blockListRecyclerView?.adapter = adapter
        val lm = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding?.blockListRecyclerView?.layoutManager = lm

        viewModel.getMyBlockList(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onChecked(position: Int) {
        viewModel.checkedList.add(viewModel.blockList[position])
    }

    override fun noBlockFound() {
        nothingFound.visibility = View.VISIBLE
        binding?.unblockButton?.visibility = View.GONE
    }

    override fun onBlockListUpdated(model: UnknownUserModel) {
        nothingFound.visibility = View.GONE
        binding?.unblockButton?.visibility = View.VISIBLE
        viewModel.blockList.add(model)
        adapter.notifyDataSetChanged()
    }

    override fun onBlocked(name: String) { return }

    override fun onUnblocked() {
        if (viewModel.checkedList.isNotEmpty()) {
            for (model in viewModel.checkedList) {
                val index = viewModel.blockList.indexOf(model)
                viewModel.blockList.remove(model)
                adapter.notifyItemRemoved(index)
                loading.dismiss()
            }
            viewModel.checkedList.clear()
            Toast.makeText(context, "Unblocked Selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNetworkError() {
        Toast.makeText(context, "Failed to Connect", Toast.LENGTH_SHORT).show()
    }

}