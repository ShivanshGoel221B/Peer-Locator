package com.goel.peerlocator.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.goel.peerlocator.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class InvitationNotificationServices : JobService() {

    private val inviteRef = FirebaseDatabase.getInstance().reference.child("invites")
            .child(FirebaseAuth.getInstance().uid.toString())

    private val inviteListener = object : ChildEventListener {
        var currentCount : Long = 0
        var inviteCount : Long = 0
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            inviteRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    inviteCount = dataSnapshot.childrenCount
                    if (++currentCount > inviteCount) {
                        val path = snapshot.key?.replace('!', '/')
                        Firebase.firestore.document(path!!).get().addOnSuccessListener {
                            val name = it["name"].toString()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val channel = NotificationChannel("inviteChannel", "New Request", NotificationManager.IMPORTANCE_DEFAULT)
                                val manager = getSystemService(NotificationManager::class.java)
                                manager.createNotificationChannel(channel)
                            }
                            val builder = NotificationCompat.Builder(applicationContext, "inviteChannel")
                                    .setContentTitle(name)
                                    .setAutoCancel(true)
                                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                                    .setContentText("You have a new invitation on Peer Locator")
                            val manager = NotificationManagerCompat.from(applicationContext)
                            manager.notify(69, builder.build())
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    inviteCount--
                    currentCount = inviteCount
                }
            })
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onChildRemoved(snapshot: DataSnapshot) {
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onCancelled(error: DatabaseError) {}

    }

    override fun onStartJob(params: JobParameters?): Boolean {
        inviteRef.addChildEventListener(inviteListener)
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        inviteRef.removeEventListener(inviteListener)
        return true
    }
}