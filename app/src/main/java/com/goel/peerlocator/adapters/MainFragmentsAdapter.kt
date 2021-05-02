package com.goel.peerlocator.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.goel.peerlocator.fragments.CirclesFragment
import com.goel.peerlocator.fragments.FriendsFragment
import com.goel.peerlocator.fragments.InvitesFragment

class MainFragmentsAdapter(fm : FragmentManager, behavior : Int) : FragmentPagerAdapter(fm, behavior) {

    override fun getCount(): Int = 3

    override fun getItem(position: Int): Fragment = when (position) {
            1-> FriendsFragment.newInstance()
            2 -> InvitesFragment.newInstance()
            else -> CirclesFragment.newInstance()
        }

    override fun getPageTitle(position: Int): CharSequence = when (position) {
        1-> "FRIENDS"
        2-> "INVITES"
        else -> "CIRCLES"
    }

}