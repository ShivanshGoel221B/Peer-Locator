package com.goel.peerlocator.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.goel.peerlocator.R
import com.goel.peerlocator.databinding.ActivitySplashBinding
import com.goel.peerlocator.fragments.SignInFragment
import com.goel.peerlocator.repositories.UserRepository
import com.goel.peerlocator.services.ServicesHandler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.lang.Thread.sleep

class SplashActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    private lateinit var binding : ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Thread {
            sleep(2000)
            auth = FirebaseAuth.getInstance()
            checkLogIn()
        }.start()
    }

    private fun checkLogIn () {
        val currentUser = auth.currentUser
        currentUser?.let {
            startMainActivity(it)
        }
        runOnUiThread { loadLoginFragment() }
    }

    private fun loadLoginFragment() {
        val logo : ImageView = binding.splashLogo
        val logoAnimation = logo.animate().translationYBy(-300f)
        logoAnimation.duration = 500
        logoAnimation.start()

        val container = binding.fragmentContainer

        val signInFragment = SignInFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.login_frame_container, signInFragment)
        container.translationY = 250f
        transaction.commitAllowingStateLoss()

        container.animate().translationYBy(-250f).setDuration(500).start()
    }


    fun startMainActivity(user : FirebaseUser?) {
        user?.let {
            UserRepository.instance.signIn(user)
            ServicesHandler.stopInviteNotification(this)
            ServicesHandler.startInviteNotification(this)
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            hideProgress()
            finish()
        }
    }

    fun showProgress () {
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgress () {
       binding.progressBar.visibility = View.GONE
    }

}