package com.goel.peerlocator.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.goel.peerlocator.activities.SplashActivity
import com.goel.peerlocator.databinding.FragmentPhoneLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class PhoneLoginFragment(private val splash: SplashActivity) : Fragment() {

    private var binding: FragmentPhoneLoginBinding? = null
    private lateinit var auth : FirebaseAuth
    private lateinit var storedVerificationId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        auth = FirebaseAuth.getInstance()
        auth.useAppLanguage()
        binding = FragmentPhoneLoginBinding.inflate(inflater, container, false)
        binding?.root?.setOnClickListener { return@setOnClickListener }

        binding?.phoneDetailsContainer?.visibility = View.VISIBLE
        binding?.loadingLayout?.visibility = View.GONE
        binding?.verificationContainer?.visibility = View.GONE

        binding?.closeButton?.setOnClickListener { requireActivity().onBackPressed() }
        binding?.getOtpButton?.setOnClickListener { sendOtp() }
        return binding?.root
    }

    private fun sendOtp () {
        val phoneNumber = binding?.editTextPhone?.text?.toString()!!
        if (phoneNumber.isEmpty()) {
            binding?.editTextPhone?.error = getString(com.goel.peerlocator.R.string.empty_phone_error)
            return
        }
        else {
            createCallBack(phoneNumber)
            showLoading(getString(com.goel.peerlocator.R.string.sending_otp))
        }
    }

    private fun createCallBack(phoneNumber: String) {
        // Callback for OTP
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d("PhoneAuthSuccess", "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }
            override fun onVerificationFailed(e: FirebaseException) {
                Log.w("PhoneAuthFails", "onVerificationFailed", e)
                if (e is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(context, "Invalid Phone Number, Please Try Again", Toast.LENGTH_LONG).show()
                    hideLoading()
                    binding?.phoneDetailsContainer?.visibility = View.VISIBLE
                    binding?.editTextPhone?.error = getString(com.goel.peerlocator.R.string.empty_phone_error)
                } else if (e is FirebaseTooManyRequestsException) {
                    Toast.makeText(context, "Sorry, too many requests to the servers", Toast.LENGTH_LONG).show()
                    activity!!.onBackPressed()
                }
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d("PhoneAuthCodeSent", "onCodeSent:$verificationId")
                storedVerificationId = verificationId
                askOtp()
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity as Activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun askOtp () {
        hideLoading()
        binding?.verificationContainer?.visibility = View.VISIBLE
        binding?.verifyOtpButton?.setOnClickListener {
            val code = binding?.otp?.text?.toString()
            if (code == null || code.length < 6) {
                binding?.otp?.error = "Invalid OTP"
            }
            else {
                showLoading(getString(com.goel.peerlocator.R.string.verifying_otp))
                verifyOtp(code)
            }
        }
    }

    private fun verifyOtp(code: String) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnSuccessListener {
            val user = it.user!!
            splash.signInUser(user)
            requireActivity().onBackPressed()
            splash.showProgress()
        }.addOnFailureListener {
            Toast.makeText(context, "Couldn't Verify, Try Again", Toast.LENGTH_SHORT).show()
            hideLoading()
            binding?.phoneDetailsContainer?.visibility = View.VISIBLE
        }
    }

    private fun showLoading (message: String) {
        binding?.root?.hideKeyboard()
        binding?.loadingLayout?.visibility = View.VISIBLE
        binding?.loadingMessage?.text = message
        binding?.phoneDetailsContainer?.visibility = View.GONE
        binding?.verificationContainer?.visibility = View.GONE
    }

    private fun hideLoading () {
        binding?.loadingLayout?.visibility = View.GONE
        binding?.phoneDetailsContainer?.visibility = View.GONE
        binding?.verificationContainer?.visibility = View.GONE
    }

    private fun View.hideKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}