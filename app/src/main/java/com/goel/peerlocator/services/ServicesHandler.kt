package com.goel.peerlocator.services

import android.app.Activity
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context

abstract class ServicesHandler {

    companion object {
        fun startBackgroundLocation(activity: Activity) {
            toggleBackgroundLocation(activity, true)
        }

        fun stopBackgroundLocation(activity: Activity) {
            toggleBackgroundLocation(activity, false)
        }

        private fun toggleBackgroundLocation (activity: Activity, flag: Boolean) {
            val componentName = ComponentName(activity.applicationContext, BackgroundLocationServices::class.java)
            val info = JobInfo.Builder(100, componentName)
                .setPersisted(true)
                .build()
            val locationSchedule = activity.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            if (flag)
                locationSchedule.schedule(info)
            else
                locationSchedule.cancelAll()
        }
    }
}
