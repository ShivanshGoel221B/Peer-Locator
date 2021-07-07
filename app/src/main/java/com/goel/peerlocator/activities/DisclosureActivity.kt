package com.goel.peerlocator.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.goel.peerlocator.R
import com.goel.peerlocator.databinding.ActivityDisclosureBinding
import com.goel.peerlocator.services.ServicesHandler
import com.goel.peerlocator.utils.Constants

class DisclosureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDisclosureBinding

    private val permissions = if(Build.VERSION.SDK_INT >= 29)
        arrayOf(Constants.FINE, Constants.COARSE, Constants.BACKGROUND)
    else
        arrayOf(Constants.FINE, Constants.COARSE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisclosureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.cancelButton.setOnClickListener {
            ServicesHandler.stopBackgroundLocation(this)
            finishAffinity()
        }
        binding.proceedButton.setOnClickListener {
            ActivityCompat.requestPermissions(this, permissions, Constants.LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && PackageManager.PERMISSION_DENIED !in grantResults) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    return
                }
            }
        }
        showDeniedDialog()
    }

    private fun showDeniedDialog() {
        AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle("Permission Denied")
            .setMessage(R.string.permission_denied)
            .setPositiveButton(R.string.ok) {_, _ -> finishAffinity()}
            .show()
    }
}