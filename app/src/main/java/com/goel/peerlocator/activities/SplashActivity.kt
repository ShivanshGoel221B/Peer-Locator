package com.goel.peerlocator.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.goel.peerlocator.R
import com.goel.peerlocator.databinding.ActivitySplashBinding
import com.goel.peerlocator.fragments.SignInFragment
import com.goel.peerlocator.listeners.UserDataListener
import com.goel.peerlocator.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SplashActivity : AppCompatActivity(), UserDataListener {

    lateinit var auth: FirebaseAuth
    private lateinit var binding : ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        checkLogIn()
    }

    private fun checkLogIn () {
        val currentUser = auth.currentUser
        currentUser?.let {
            signInUser(it)
            return
        }
        runOnUiThread { loadLoginFragment() }
    }

    fun signInUser(user: FirebaseUser) {
        UserRepository.instance.signIn(user, this)
    }

    private fun loadLoginFragment() {
        val logo : ImageView = binding.splashLogo
        val logoAnimation = logo.animate().translationYBy(-300f)
        logoAnimation.duration = 500
        logoAnimation.start()

        val container = binding.fragmentContainer

        val signInFragment = SignInFragment.getInstance(this)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.login_frame_container, signInFragment)
        container.translationY = 250f
        transaction.commitAllowingStateLoss()

        container.animate().translationYBy(-250f).setDuration(500).start()
    }


    fun showProgress () {
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgress () {
       binding.progressBar.visibility = View.GONE
    }

    override fun newPhoneFound() {

    }

    override fun onUserCreated() {
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
        hideProgress()
        finish()
    }

    override fun onError() {
        recreate()
        Toast.makeText(this, R.string.error_message, Toast.LENGTH_SHORT).show()
    }

}