package com.pqaar.app.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pqaar.app.ui.BidListFragment
import com.pqaar.app.ui.RoutesFragment

class BottomSheetViewPagerAdapter(fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    //val fragmentList = ArrayList<Fragment>()

    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> RoutesFragment()
            1 -> BidListFragment()
            else -> RoutesFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Routes"
            1 -> "List"
            else -> ""
        }
    }
   /* override fun getItemCount(): Int {
        return fragmentList.size
    }*/

    /*override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RoutesFragment()
            1 -> BidListFragment()
            else -> RoutesFragment()
        }
    }*/
}