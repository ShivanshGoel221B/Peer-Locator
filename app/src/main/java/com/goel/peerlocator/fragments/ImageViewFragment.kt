package com.goel.peerlocator.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.goel.peerlocator.R
import com.goel.peerlocator.databinding.FragmentImageViewBinding

class ImageViewFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = ImageViewFragment()
    }

    private var binding : FragmentImageViewBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding  = FragmentImageViewBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}