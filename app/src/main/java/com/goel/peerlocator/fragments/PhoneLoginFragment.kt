package com.goel.peerlocator.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.goel.peerlocator.databinding.FragmentPhoneLoginBinding

class PhoneLoginFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = PhoneLoginFragment()
    }

    private var binding: FragmentPhoneLoginBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        binding = FragmentPhoneLoginBinding.inflate(inflater, container, false)
        binding?.root?.setOnClickListener { return@setOnClickListener }
        binding?.closeButton?.setOnClickListener { activity!!.onBackPressed() }
        return binding?.root
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}