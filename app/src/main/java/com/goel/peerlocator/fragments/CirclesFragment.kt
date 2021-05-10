package com.goel.peerlocator.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goel.peerlocator.R
import com.goel.peerlocator.activities.CircleInfoActivity
import com.goel.peerlocator.adapters.CirclesAdapter
import com.goel.peerlocator.databinding.CirclesFragmentBinding
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

    private fun setAdapter() {
        binding?.circleRecyclerView?.adapter = circlesAdapter
        val lm = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding?.circleRecyclerView?.layoutManager = lm
    }

    override fun onResume() {
        super.onResume()

        val shimmer = binding?.shimmerLayout!!
        shimmer.startShimmerAnimation()
        shimmer.visibility = View.VISIBLE
        nothingFound.visibility = View.GONE

        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(activity!!.application))
            .get(CirclesViewModel::class.java)

        circlesAdapter = CirclesAdapter(viewModel.circleList.value!!, context!!, this)

        setAdapter()

        viewModel.circleList.observe(this) {
            circlesAdapter.notifyDataSetChanged()
        }
        
        viewModel.getAllCircles(circlesAdapter, shimmer, nothingFound)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


    //Click Listeners
    override fun onCircleClicked(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onCircleLongClicked(position: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun onCirclePhotoClicked(position: Int) {
        val model = viewModel.circleList.value!![position]
        val imageViewFragment = ImageViewFragment.newInstance(url = model.imageUrl, isCircle = true)
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.addToBackStack(Constants.DP)
        transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
        transaction.replace(R.id.profile_photo_container, imageViewFragment, Constants.DP)
        transaction.commit()
    }

    override fun onInfoClicked(position: Int) {
        CircleInfoActivity.model = viewModel.circleList.value!![position]
        startActivity(Intent(activity, CircleInfoActivity::class.java))
    }
}