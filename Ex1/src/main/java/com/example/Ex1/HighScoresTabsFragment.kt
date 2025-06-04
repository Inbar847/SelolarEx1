package com.example.Ex1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.tabs.TabLayout

class HighScoresTabsFragment : Fragment(), TopScoresFragment.OnScoreSelectedListener {

    private var topScoresFragment: TopScoresFragment? = null
    private var mapFragment: MapFragment? = null
    private var pendingLatLng: LatLng? = null
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_high_scores_tabs, container, false)

        val viewPager = view.findViewById<ViewPager>(R.id.viewPager)
        tabLayout = view.findViewById(R.id.tabLayout)

        viewPager.adapter = object : FragmentPagerAdapter(childFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

            override fun getCount(): Int = 2

            override fun getItem(position: Int): Fragment {
                return if (position == 0) {
                    topScoresFragment = TopScoresFragment()
                    topScoresFragment!!.setListener(this@HighScoresTabsFragment)
                    topScoresFragment!!
                } else {
                    mapFragment = MapFragment()
                    pendingLatLng?.let {
                        mapFragment?.setInitialLatLng(it)
                        pendingLatLng = null
                    }
                    mapFragment!!
                }
            }

            override fun getPageTitle(position: Int): CharSequence {
                return if (position == 0) "Top Scores" else "Map"
            }
        }

        tabLayout.setupWithViewPager(viewPager)

        return view
    }

    override fun onScoreSelected(lat: Double, lon: Double) {
        val latLng = LatLng(lat, lon)
        if (mapFragment != null) {
            mapFragment?.updateMap(latLng)
            tabLayout.getTabAt(1)?.select() // מעבר אוטומטי לטאב של המפה
        } else {
            pendingLatLng = latLng
        }
    }
}
