package com.goel.peerlocator.activities

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.goel.peerlocator.R
import com.goel.peerlocator.adapters.MainFragmentsAdapter
import com.goel.peerlocator.databinding.ActivityMainBinding
import com.goel.peerlocator.dialogs.LocationDialog
import com.goel.peerlocator.models.UserModel
import com.goel.peerlocator.services.ServicesHandler
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.database.Database
import com.goel.peerlocator.utils.location.Location
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {
    private lateinit var user : UserModel
    private lateinit var binding: ActivityMainBinding

    private var locationPermissionGranted = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createToolBar()
        binding.customToolbar.profilePicture.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val fragmentsAdapter = MainFragmentsAdapter(supportFragmentManager, 0)
        setFragmentsAdapter(fragmentsAdapter)
        setUserData()
        getLocationPermission()
    }

    private fun checkBackgroundLocation () {
        val preferences = getSharedPreferences(Constants.PREFS, MODE_PRIVATE)
        val per = preferences.getBoolean(Constants.BACK_LOC, true)
        ServicesHandler.stopBackgroundLocation(this)
        if (per) {
            ServicesHandler.startBackgroundLocation(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionGranted = false

        when (requestCode) {
            Constants.LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && PackageManager.PERMISSION_DENIED !in grantResults) {
                    locationPermissionGranted = true
                    checkGPS()
                    getMyLocation()
                    checkBackgroundLocation()
                    return
                }
            }
        }
        showLocationWarning ()
    }

    private fun getLocationPermission () {
        if (
            ContextCompat.checkSelfPermission(applicationContext, Constants.FINE) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(applicationContext, Constants.COARSE) == PackageManager.PERMISSION_GRANTED) {
                if(Build.VERSION.SDK_INT >= 29) {
                    if (ContextCompat.checkSelfPermission(applicationContext, Constants.BACKGROUND)
                        == PackageManager.PERMISSION_GRANTED) {
                        locationPermissionGranted = true
                        checkGPS()
                        getMyLocation()
                        checkBackgroundLocation()
                    }

                } else {
                    locationPermissionGranted = true
                    checkGPS()
                    getMyLocation()
                    checkBackgroundLocation()
                }
        }
        else {
            locationPermissionGranted = false
            showLocationWarning ()
        }
    }

    private fun showLocationWarning () {
        val permissions = if(Build.VERSION.SDK_INT >= 29)
            arrayOf(Constants.FINE, Constants.COARSE, Constants.BACKGROUND)
        else
            arrayOf(Constants.FINE, Constants.COARSE)

        LocationDialog(object : LocationDialog.ClickListeners {
            override fun proceed() {
                ActivityCompat.requestPermissions(this@MainActivity, permissions, Constants.LOCATION_PERMISSION_REQUEST_CODE)
            }
            override fun cancel() {
                finishAffinity()
                ServicesHandler.stopBackgroundLocation(this@MainActivity)
            }
        }).show(supportFragmentManager, "location")
    }

    private fun checkGPS() {
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager
        val isEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if(!isEnabled)
            alertForGPS()
    }

    private fun alertForGPS () {
        AlertDialog.Builder(this)
            .setTitle("Turn On GPS")
            .setMessage("We recommend you to turn on GPS for better accuracy")
            .setPositiveButton("Settings") {dialog, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                dialog.dismiss()
            }.show()
    }

    private fun getMyLocation () {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            if (ContextCompat.checkSelfPermission(applicationContext, Constants.FINE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(applicationContext, Constants.COARSE) == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                            Location.updateMyLocation (it)
                        }
            }
            else
                getLocationPermission()
        } catch (e : SecurityException) {
        }
    }

    private fun createToolBar() {
        val toolbar : androidx.appcompat.widget.Toolbar = binding.customToolbar.root
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_context_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.add_new_circle -> startActivity(Intent(this, NewCircleActivity::class.java))
            R.id.add_new_friend -> startActivity(Intent(this, AddFriendActivity::class.java))
            R.id.sent_invitations -> {
                SentInvitationActivity.documentReference = Database.currentUserRef
                startActivity(Intent(this, SentInvitationActivity::class.java))
            }
            R.id.app_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.more_apps -> loadUrl(Constants.DEV_PAGE)
            R.id.about -> loadUrl(Constants.ABOUT)
            R.id.terms -> loadUrl(Constants.TERMS)
            R.id.privacy -> loadUrl(Constants.PRIVACY_POLICY)
        }
        return true
    }

    private fun loadUrl(key: String) {
        FirebaseDatabase.getInstance().reference.get()
            .addOnFailureListener {
                Toast.makeText(this, R.string.error_message, Toast.LENGTH_SHORT).show()
            }
            .addOnSuccessListener {
                val url = it.child(key).getValue(String::class.java)
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            }
    }


    private fun setUserData() {
        Database.currentUser.let {
            user = it
            user = Database.currentUser
            val dpView = binding.customToolbar.profilePicture
            val nameView = binding.customToolbar.profileName
            val displayName = user.name
            val photoUrl = user.imageUrl
            nameView.text = displayName
            if (photoUrl.isNotEmpty())
                Glide.with(this).load(photoUrl).placeholder(R.drawable.ic_placeholder_user)
                    .circleCrop().into(dpView)
        }
    }

    private fun setFragmentsAdapter(fragmentsAdapter: MainFragmentsAdapter) {
        binding.mainViewPager.adapter = fragmentsAdapter
        binding.mainTabs.setupWithViewPager(binding.mainViewPager)
    }

}