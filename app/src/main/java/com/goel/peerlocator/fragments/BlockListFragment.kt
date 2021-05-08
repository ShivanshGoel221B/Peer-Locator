package com.goel.peerlocator.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goel.peerlocator.adapters.BlockListAdapter
import com.goel.peerlocator.databinding.BlockListFragmentBinding
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = BlockListFragmentBinding.inflate(inflater, container, false)
        createRecyclerView()
        binding?.root?.setOnClickListener { return@setOnClickListener }
        binding?.closeButton?.setOnClickListener { activity!!.onBackPressed() }
        binding?.unblockButton?.setOnClickListener { viewModel.unblockSelected(this) }
        return binding?.root
    }

    private fun createRecyclerView () {
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(activity!!.application))
            .get(BlockListViewModel::class.java)
        adapter = BlockListAdapter(viewModel.blockList, this, context!!)

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

    override fun onBlockListUpdated(model: UnknownUserModel) {
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
            }
            viewModel.checkedList.clear()
            Toast.makeText(context, "Unblocked Selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNetworkError() {
        Toast.makeText(context, "Failed to Connect", Toast.LENGTH_SHORT).show()
    }

}