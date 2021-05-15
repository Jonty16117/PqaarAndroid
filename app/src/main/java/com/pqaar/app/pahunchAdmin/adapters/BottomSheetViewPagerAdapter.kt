package com.pqaar.app.pahunchAdmin.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.pqaar.app.pahunchAdmin.view.TruckRequestsFragment
import com.pqaar.app.truckOwner.view.AuctionListFragment
import com.pqaar.app.truckOwner.view.RoutesFragment

class BottomSheetViewPagerAdapter(fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return 1
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> TruckRequestsFragment()
            else -> TruckRequestsFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Incoming Trucks"
            else -> ""
        }
    }
}
