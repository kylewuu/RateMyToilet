package com.example.ratemytoilet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * refs:
 * https://www.howtocreate.co.uk/xor.html
 * https://stackoverflow.com/questions/21352571/android-how-do-i-check-if-dialogfragment-is-showing
 */
class MainActivity :  AppCompatActivity(), FilterDialogFragment.FilterListener {
    companion object {
        var notRunFirstTime = false
        var maleCheck = false
        var femaleCheck = false
        var paperCheck = false
        var soapCheck = false
        var accessCheck = false
        var cleanlinessStart = 1f
        var cleanlinessEnd = 5f
        var previousLocationsSize = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = WashroomMapFragment()
        val listFragment = WashroomListFragment()

//        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
//        val adapter = ViewPagerAdapter(this)
//        viewPager.adapter = adapter

        supportFragmentManager.beginTransaction().add(R.id.container, mapFragment, "Map").commit()

        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavView.setOnItemSelectedListener {
            val fragment = when(it.itemId) {
                R.id.washroom_map -> mapFragment
                R.id.washroom_list -> listFragment
                else -> mapFragment
            }
//            viewPager.currentItem = position
            supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
            true
        }
    }

    override fun onFilterConditionPassed(paperCheck: Boolean, soapCheck: Boolean, accessCheck: Boolean, maleCheck: Boolean, femaleCheck: Boolean, startValue: Float, endValue: Float) {
        val fragment = supportFragmentManager.findFragmentByTag("Map") as WashroomMapFragment

        fragment.filterConditionPassed(paperCheck, soapCheck, accessCheck, maleCheck, femaleCheck, startValue, endValue)

    }

//    private inner class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
//        override fun getItemCount(): Int {
//            return 2
//        }
//
//        override fun createFragment(position: Int): Fragment {
//            return when(position) {
//                0 -> WashroomMapFragment()
//                1 -> WashroomListFragment()
//                else -> WashroomMapFragment()
//            }
//        }
//
//    }
}

