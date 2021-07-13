package com.goel.peerlocator.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.goel.peerlocator.R
import com.goel.peerlocator.databinding.FragmentImageViewBinding

class ImageViewFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(url : String, isCircle : Boolean) = ImageViewFragment().apply {
            this.url = url
            this.isCircle = isCircle
        }
    }

    private var binding : FragmentImageViewBinding? = null
    private lateinit var url : String
    private var isCircle = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding  = FragmentImageViewBinding.inflate(inflater, container, false)

        val placeHolder = if (isCircle) R.drawable.ic_placeholder_circle_big
                          else R.drawable.ic_placeholder_user_big

        Glide.with(requireContext()).load(url)
            .placeholder(placeHolder).into(binding?.largeProfileImage!!)
        binding?.imageCloseButton?.setOnClickListener { requireActivity().onBackPressed() }

        binding?.root?.setOnClickListener { return@setOnClickListener }

        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}