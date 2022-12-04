package com.example.ratemytoilet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

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
        setContentView(R.layout.fragment)
        val myFragment = MainFragment()
        supportFragmentManager.beginTransaction().add(R.id.container, myFragment, "Map").commit()
    }

    override fun onFilterConditionPassed(paperCheck: Boolean, soapCheck: Boolean, accessCheck: Boolean, maleCheck: Boolean, femaleCheck: Boolean, startValue: Float, endValue: Float) {
        val fragment = supportFragmentManager.findFragmentByTag("Map") as MainFragment

        fragment.filterConditionPassed(paperCheck, soapCheck, accessCheck, maleCheck, femaleCheck, startValue, endValue)

    }
}

