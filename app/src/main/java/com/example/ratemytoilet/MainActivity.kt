package com.example.ratemytoilet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * refs:
 * https://www.howtocreate.co.uk/xor.html
 * https://stackoverflow.com/questions/21352571/android-how-do-i-check-if-dialogfragment-is-showing
 */
class MainActivity :  AppCompatActivity(), FilterDialogFragment.FilterListener {
    var currentFragment = "Map"

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
        var isAdmin = false
        var updateReviews = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNavView.setupWithNavController(navController)
    }

    override fun onFilterConditionPassed(paperCheck: Boolean, soapCheck: Boolean, accessCheck: Boolean, maleCheck: Boolean, femaleCheck: Boolean, startValue: Float, endValue: Float) {
        if (currentFragment == "Map") {
            val fragment = supportFragmentManager.findFragmentByTag(currentFragment) as WashroomMapFragment
            fragment.filterConditionPassed(paperCheck, soapCheck, accessCheck, maleCheck, femaleCheck, startValue, endValue)
        } else if (currentFragment == "List"){
            val fragment = supportFragmentManager.findFragmentByTag(currentFragment) as WashroomListFragment
            fragment.filterConditionPassed(paperCheck, soapCheck, accessCheck, maleCheck, femaleCheck, startValue, endValue)
        }

    }

}
