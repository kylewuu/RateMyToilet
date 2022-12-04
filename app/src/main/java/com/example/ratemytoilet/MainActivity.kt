package com.example.ratemytoilet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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

        var updateMap = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = WashroomMapFragment()
        val listFragment = WashroomListFragment()

        supportFragmentManager.beginTransaction().add(R.id.container, mapFragment, "Map").commit()

        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavView.setOnItemSelectedListener {
            val fragment = when(it.itemId) {
                R.id.washroom_map -> mapFragment
                R.id.washroom_list -> listFragment
                else -> mapFragment
            }
            supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
            true
        }
    }

    override fun onFilterConditionPassed(paperCheck: Boolean, soapCheck: Boolean, accessCheck: Boolean, maleCheck: Boolean, femaleCheck: Boolean, startValue: Float, endValue: Float) {
        val fragment = supportFragmentManager.findFragmentByTag("Map") as WashroomMapFragment

        fragment.filterConditionPassed(paperCheck, soapCheck, accessCheck, maleCheck, femaleCheck, startValue, endValue)
    }
}
