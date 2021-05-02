package com.goel.peerlocator.services

import android.app.Activity
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context

object ServicesHandler {

    private fun toggleInviteNotification (activity : Activity, flag : Boolean) {
        val componentName = ComponentName(activity.applicationContext, InvitationNotificationServices::class.java)
        val info = JobInfo.Builder(69, componentName)
                .setPersisted(true)
                .build()

        val invitationSchedule = activity.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        if (flag)
            invitationSchedule.schedule(info)
        else
            invitationSchedule.cancelAll()
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

    fun startInviteNotification (activity: Activity) {
        toggleInviteNotification(activity, true)
    }

    fun stopInviteNotification (activity: Activity) {
        toggleInviteNotification(activity, false)
    }

    fun startBackgroundLocation ( activity: Activity) {
        toggleBackgroundLocation(activity, true)
    }

    fun stopBackgroundLocation ( activity: Activity) {
        toggleBackgroundLocation(activity, false)
    }

}