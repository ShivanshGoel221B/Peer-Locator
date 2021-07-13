package com.goel.peerlocator.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.goel.peerlocator.fragments.CirclesFragment
import com.goel.peerlocator.fragments.FriendsFragment
import com.goel.peerlocator.fragments.InvitesFragment

class MainFragmentsAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment = when (position) {
            1-> FriendsFragment.newInstance()
            2 -> InvitesFragment.newInstance()
            else -> CirclesFragment.newInstance()
        }

    fun getPageTitle(position: Int): CharSequence = when (position) {
        1-> "FRIENDS"
        2-> "INVITES"
        else -> "CIRCLES"
    }

}