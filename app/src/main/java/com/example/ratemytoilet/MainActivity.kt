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
        var MALE_CHECK_KEY = "male_check_key"
        var FEMALE_CHECK_KEY = "female_check_key"
        var PAPER_CHECK_KEY = "paper_check_key"
        var SOAP_CHECK_KEY = "soap_check_key"
        var ACCESS_CHECK_KEY = "access_check_key"
        var CLEANLINESS_START_KEY = "cleanliness_start_key"
        var CLEANLINESS_END_KEY = "cleanliness_end_key"
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

