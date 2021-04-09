package com.pqaar.app.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.pqaar.app.R
import com.pqaar.app.ui.adapters.BottomSheetViewPagerAdapter

/*
class DashboardBottomSheet : FragmentActivity() {



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val adapter = BottomSheetViewPagerAdapter(supportFragmentManager)
        val fragmentsList = listOf(RoutesFragment(), BidListFragment())
        adapter.fragmentList.addAll(fragmentsList)

        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager2)
        viewPager.adapter = adapter
        viewPager.currentItem = 0

        val tabLayout = view?.findViewById<TabLayout>(
            R.id.tabLayout)
        TabLayoutMediator(tabLayout!!, viewPager!!) {
                tab, position -> when (position) {
            0 -> tab.text = "ROUTES"
            1 -> tab.text = "BID LIST"
        }
        }.attach()

        return view
    }
}*/
