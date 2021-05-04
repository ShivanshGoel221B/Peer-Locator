package com.goel.peerlocator.activities

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.goel.peerlocator.R
import com.goel.peerlocator.adapters.MainFragmentsAdapter
import com.goel.peerlocator.databinding.ActivityMainBinding
import com.goel.peerlocator.models.UserModel
import com.goel.peerlocator.services.ServicesHandler
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.Database
import com.goel.peerlocator.listeners.UserDataListener
import com.goel.peerlocator.utils.location.Location
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation


class MainActivity : AppCompatActivity(), UserDataListener {
    private lateinit var user : UserModel
    private lateinit var binding: ActivityMainBinding

    private var locationPermissionGranted = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Database.listener = this

        createToolBar()
        setBackgroundLocationService()
    }

    override fun onResume() {
        super.onResume()
        val fragmentsAdapter = MainFragmentsAdapter(supportFragmentManager, 0)
        setFragmentsAdapter(fragmentsAdapter)

        setUserData()
        val preferences = getSharedPreferences(Constants.PREFS, MODE_PRIVATE)
        val per = preferences.getBoolean(Constants.BACK_LOC, true)
        if (!per) {
            ServicesHandler.stopBackgroundLocation(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        locationPermissionGranted = false

        when (requestCode) {
            Constants.PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && PackageManager.PERMISSION_DENIED !in grantResults) {
                    locationPermissionGranted = true
                    alertForGPS()
                    getMyLocation()
                    setBackgroundLocationService ()
                    return
                }
            }
        }
        showLocationWarning ()
    }

    private fun getLocationPermission () {
        if (ContextCompat.checkSelfPermission(applicationContext, Constants.FINE) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(applicationContext, Constants.COARSE) == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true
                getMyLocation()
                setBackgroundLocationService ()
            }
        }
        else {
            showLocationWarning ()
        }
    }

    private fun showLocationWarning () {
        val permissions = arrayOf(Constants.FINE, Constants.COARSE)
        AlertDialog.Builder(this)
            .setTitle("Permission")
            .setMessage("You need to give Location Access Permission to continue")
            .setPositiveButton("Proceed") { dialog, _ -> Log.d("Error: ", "Could not Retrieve Data")
                ActivityCompat.requestPermissions(this, permissions, Constants.PERMISSION_REQUEST_CODE)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { _, _ -> finish()}
            .show()
    }

    private fun alertForGPS () {
        AlertDialog.Builder(this)
            .setTitle("Turn On GPS")
            .setMessage("Turn on the GPS in the settings for best accuracy")
            .setPositiveButton("Settings") {dialog, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                dialog.dismiss()
            }.show()
    }

    private fun getMyLocation () {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            if (ContextCompat.checkSelfPermission(applicationContext, Constants.FINE) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(applicationContext, Constants.COARSE) == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                            Location.updateMyLocation (it)
                        }
                }
            }
        } catch (e : SecurityException) {
        }
    }

    private fun createToolBar() {
        val toolbar : androidx.appcompat.widget.Toolbar = binding.customToolbar.root
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        binding.customToolbar.profilePicture.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_context_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setUserData() {

        if (Database.currentUser == null) {
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Your Profile could not be retrieved, Please try again later")
                .setPositiveButton("OK") { dialog, _ -> Log.d("Error: ", "Could not Retrieve Data")
                    dialog.dismiss()
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, SplashActivity::class.java))
                }.show()
        }
        Database.currentUser?.let {
            user = it
            user = Database.currentUser!!
            val dpView = binding.customToolbar.profilePicture
            val nameView = binding.customToolbar.profileName
            val displayName = user.displayName
            val photoUrl = user.photoUrl
            nameView.text = displayName
            if (photoUrl.isNotEmpty())
                Picasso.with(this).load(photoUrl).placeholder(R.drawable.ic_placeholder_user)
                    .transform(CropCircleTransformation()).into(dpView)
        }
    }

    private fun setFragmentsAdapter(fragmentsAdapter: MainFragmentsAdapter) {
        binding.mainViewPager.adapter = fragmentsAdapter
        binding.mainTabs.setupWithViewPager(binding.mainViewPager)
    }

    private fun setBackgroundLocationService () {
        val preferences = getSharedPreferences(Constants.PREFS, MODE_PRIVATE)
        val per = preferences.getBoolean(Constants.BACK_LOC, true)
        if (per) {
            ServicesHandler.startBackgroundLocation(this)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onUserCreated() {
        getLocationPermission()
        setUserData()
    }

}