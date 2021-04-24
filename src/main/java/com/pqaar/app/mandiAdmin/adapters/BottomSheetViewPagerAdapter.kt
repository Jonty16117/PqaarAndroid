package com.pqaar.app.mandiAdmin.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.pqaar.app.mandiAdmin.view.AddMandiRoutesFragment

class BottomSheetViewPagerAdapter(fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return 1
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> AddMandiRoutesFragment()
            else -> AddMandiRoutesFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Create new routes list"
            else -> ""
        }
    }
}