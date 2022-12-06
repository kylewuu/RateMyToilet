package com.example.ratemytoilet

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ratemytoilet.dialogs.FilterDialogFragment
import com.example.ratemytoilet.launch.LaunchActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener

/**
 * refs:
 * https://www.howtocreate.co.uk/xor.html
 * https://stackoverflow.com/questions/21352571/android-how-do-i-check-if-dialogfragment-is-showing
 */
class MainActivity :  AppCompatActivity(), FilterDialogFragment.FilterListener {
    var currentFragment = "Map"
    var FRAGMENT_KEY = "fragment_key"

    private var authListener = AuthStateListener {
        if (it.currentUser == null) {
            loadLaunchScreen()
        }
    }

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

        var updateMap = true
        var updateList = true
        var isAdmin = false
        var updateReviews = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = WashroomMapFragment()
        val listFragment = WashroomListFragment()
        val profileFragment = ProfileFragment()

        supportFragmentManager.beginTransaction().add(R.id.container, mapFragment, "Map").commit()

        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavView.setOnItemSelectedListener {
            var fragment = when(it.itemId) {
                R.id.washroomListFragment -> {
                    currentFragment = "List"
                    listFragment
                }
                R.id.profileFragment -> {
                    currentFragment = "Profile"
                    profileFragment
                }
                else -> {
                    currentFragment = "Map"
                    mapFragment
                }
            }
            val foundFragment = supportFragmentManager.findFragmentByTag(currentFragment)
            if (foundFragment != null) {
                fragment = foundFragment
            }
            supportFragmentManager.beginTransaction().replace(R.id.container, fragment, currentFragment).commit()

            true
        }
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(authListener)

    }

    private fun loadLaunchScreen() {
        val intent = Intent(this, LaunchActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStop() {
        super.onStop()

        FirebaseAuth.getInstance().removeAuthStateListener(authListener)
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

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString(FRAGMENT_KEY, currentFragment)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentFragment = savedInstanceState.getString(FRAGMENT_KEY).toString()
        updateMap = true
        updateList = true

        var fragment = when(currentFragment) {
            "Profile" -> {
                ProfileFragment()
            }
            "List" -> {
                WashroomListFragment()
            }
            else -> {
                WashroomMapFragment()
            }
        }
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment, currentFragment).commit()
    }

}
