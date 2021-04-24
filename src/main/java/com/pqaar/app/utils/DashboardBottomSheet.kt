package com.pqaar.app.utils

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
