package com.goel.peerlocator.services

import android.app.Activity
import android.content.Intent

abstract class ServicesHandler {

    companion object {

        fun startBackgroundLocation(activity: Activity) {
            toggleBackgroundLocation(activity, true)
        }

        fun stopBackgroundLocation(activity: Activity) {
            toggleBackgroundLocation(activity, false)
        }

        private fun toggleBackgroundLocation (activity: Activity, flag: Boolean) {
            val notificationIntent = Intent(activity, BackgroundLocationServices::class.java)
            if (flag)
                activity.startService(notificationIntent)
            else
                activity.stopService(notificationIntent)
        }
    }
}
