package com.goel.peerlocator.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.goel.peerlocator.R
import com.goel.peerlocator.databinding.ActivitySplashBinding
import com.goel.peerlocator.dialogs.CreateProfileDialog
import com.goel.peerlocator.fragments.SignInFragment
import com.goel.peerlocator.listeners.UserDataListener
import com.goel.peerlocator.repositories.UserRepository
import com.goel.peerlocator.utils.Constants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SplashActivity : AppCompatActivity(), UserDataListener, CreateProfileDialog.ClickListener {

    lateinit var auth: FirebaseAuth
    private lateinit var binding : ActivitySplashBinding
    private lateinit var nameDialog: CreateProfileDialog

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
        runOnUiThread {
            startLogoAnimation()
            loadLoginFragment()
        }
    }

    fun signInUser(user: FirebaseUser) {
        UserRepository.instance.signIn(user, this)
    }

    private fun startLogoAnimation () {
        val logo : ImageView = binding.splashLogo
        val logoAnimation = logo.animate().translationYBy(-300f)
        logoAnimation.duration = 500
        logoAnimation.start()
    }

    private fun loadLoginFragment() {

        val container = binding.fragmentContainer

        val signInFragment = SignInFragment(this)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.login_frame_container, signInFragment)
        container.translationY = 250f
        transaction.commitAllowingStateLoss()

        container.animate().translationYBy(-250f).setDuration(500).start()
    }

    private fun userLogout () {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        GoogleSignIn.getClient(this, gso).signOut()
            .addOnSuccessListener {
                auth.signOut()
                recreate()
            }
    }


    fun showProgress () {
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgress () {
       binding.progressBar.visibility = View.GONE
    }

    override fun newPhoneFound() {
        nameDialog = CreateProfileDialog(this)
        nameDialog.show(supportFragmentManager, Constants.NAME)
    }

    override fun onUserCreated() {
        val mainIntent = Intent(this, MainActivity::class.java)
        val disclosureIntent = Intent(this, DisclosureActivity::class.java)

        if (
            ContextCompat.checkSelfPermission(applicationContext, Constants.FINE) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(applicationContext, Constants.COARSE) == PackageManager.PERMISSION_GRANTED) {
            if(Build.VERSION.SDK_INT >= 29) {
                if (ContextCompat.checkSelfPermission(applicationContext, Constants.BACKGROUND)
                    == PackageManager.PERMISSION_GRANTED) {
                    startActivity(mainIntent)
                }
                else {
                    startActivity(disclosureIntent)
                }

            } else {
                startActivity(mainIntent)
            }
        }
        else {
            startActivity(disclosureIntent)
        }
        hideProgress()
        finish()
    }

    override fun onError() {
        userLogout()
        hideProgress()
        Toast.makeText(this, R.string.error_message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateClicked(editTextName: EditText) {
        showProgress()
        val editText = editTextName.text
        if (editText == null)
            editTextName.error = getString(R.string.invalid_name_warning)
        editText?.let {
            val name = it.toString()
            val validityResult = Constants.isNameValid(name)
            if (false in validityResult.keys)
                editTextName.error = validityResult[false]
            else {
                UserRepository.instance.createProfile(name, this)
                nameDialog.dismiss()
                showProgress()
            }
        }
    }

    override fun onCancelClicked() {
        nameDialog.dismiss()
        userLogout()
    }

}