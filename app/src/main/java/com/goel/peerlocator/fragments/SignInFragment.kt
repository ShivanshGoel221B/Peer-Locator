package com.goel.peerlocator.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.goel.peerlocator.R
import com.goel.peerlocator.activities.SplashActivity
import com.goel.peerlocator.databinding.FragmentSignInBinding
import com.goel.peerlocator.utils.Constants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class SignInFragment(private val splash: SplashActivity) : Fragment() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val rcSignIn = 10
    private lateinit var googleSignInButton : Button
    private lateinit var auth : FirebaseAuth
    private var binding : FragmentSignInBinding? = null

    override fun onCreateView (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        val view = binding?.root

        googleSignInButton = binding!!.btnGoogleLogin

        initializeGoogleAuthObject()

        googleSignInButton.setOnClickListener {googleSignIn()}
        binding?.btnPhoneLogin?.setOnClickListener {
            val phoneFragment = PhoneLoginFragment(splash)
            val transaction = activity!!.supportFragmentManager.beginTransaction()
            transaction.addToBackStack(Constants.DP)
            transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
            transaction.replace(R.id.phone_fragment_container, phoneFragment, Constants.PHONE)
            transaction.commit()
        }
        auth = splash.auth

        return view
    }

    // Configure Google Sign In
    private fun initializeGoogleAuthObject() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity!!, gso)
    }

    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        splash.showProgress()
        startActivityForResult(signInIntent, rcSignIn)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == rcSignIn) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d("LogIn Success", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("Login Failed", "Google sign in failed", e)
                splash.hideProgress()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("LogIn Success", "signInWithCredential:success")
                    val user = auth.currentUser!!
                    splash.signInUser(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("LogIn Fail", "signInWithCredential:failure", task.exception)
                    Toast.makeText(context, "Google Sign in Failed", Toast.LENGTH_LONG).show()
                    splash.hideProgress()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}