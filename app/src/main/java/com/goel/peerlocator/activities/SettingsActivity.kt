package com.goel.peerlocator.activities

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.goel.peerlocator.R
import com.goel.peerlocator.databinding.ActivitySettingsBinding
import com.goel.peerlocator.services.ServicesHandler
import com.goel.peerlocator.utils.Constants

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setTheme(R.style.Theme_AppCompat_DayNight_DarkActionBar)

        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.settings)
        setSwitches()
        setClickListeners()
    }

    private fun setSwitches() {
        preferences = getSharedPreferences(Constants.PREFS, MODE_PRIVATE)
        val backgroundLocation = preferences.getBoolean(Constants.BACK_LOC, true)

        binding.backgroundLocationSwitch.isChecked = backgroundLocation
    }

    private fun setClickListeners () {
        val prefEditor = preferences.edit()

        binding.backgroundLocationSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefEditor.putBoolean(Constants.BACK_LOC, isChecked)
            prefEditor.apply()
            if (isChecked)
                ServicesHandler.startBackgroundLocation(this)
            else
                ServicesHandler.stopBackgroundLocation(this)
        }
    }

}