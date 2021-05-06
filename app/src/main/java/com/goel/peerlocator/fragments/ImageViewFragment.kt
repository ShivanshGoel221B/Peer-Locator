package com.goel.peerlocator.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.goel.peerlocator.R
import com.goel.peerlocator.databinding.FragmentImageViewBinding
import com.google.firebase.firestore.DocumentReference
import com.squareup.picasso.Picasso

class ImageViewFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(url : String, isCircle : Boolean, editable : Boolean = false, reference: DocumentReference? = null) = ImageViewFragment().apply {
            this.url = url
            this.isCircle = isCircle
            this.editable = editable
            this.reference = reference
        }
    }

    private var binding : FragmentImageViewBinding? = null
    private var reference : DocumentReference? = null
    private lateinit var url : String
    private var editable = false
    private var isCircle = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding  = FragmentImageViewBinding.inflate(inflater, container, false)
        if (editable)
            binding?.imageEditButton?.visibility = View.VISIBLE
        else
            binding?.imageEditButton?.visibility = View.GONE

        val placeHolder = if (isCircle) R.drawable.ic_placeholder_circle_big
                          else R.drawable.ic_placeholder_user_big

        Picasso.with(context).load(url).placeholder(placeHolder).into(binding?.largeProfileImage)
        binding?.imageCloseButton?.setOnClickListener { activity!!.onBackPressed() }
        binding?.imageEditButton?.setOnClickListener {
            if (editable)
                editProfilePhoto()
        }

        binding?.root?.setOnClickListener {
            Log.d("FRAME: ", "Photo frame clicked")
        }

        return binding?.root
    }

    private fun editProfilePhoto () {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}