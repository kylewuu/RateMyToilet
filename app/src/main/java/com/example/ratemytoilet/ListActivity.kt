package com.example.ratemytoilet

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity


/*
References:
https://stackoverflow.com/questions/21422294/how-to-add-button-in-header
https://www.android--code.com/2020/03/android-kotlin-listview-add-item.html
https://stackoverflow.com/questions/14666106/inserting-a-textview-in-the-middle-of-a-imageview-android
https://www.javatpoint.com/android-custom-listview
 */

class ListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        // Remove app name from toolbar
        supportActionBar?.setDisplayShowTitleEnabled(false);

        // TODO: Get reviews from firebase. Currently reviews in list are hardcoded.
        val washroomNames = arrayOf(
            "AQ Washroom", "AQ Washroom"
        )

        val washroomAmenities = arrayOf(
            "Male / Hand Dryer / Paper Towels",
            "Female / Hand Dryer / Paper Towels"
        )

        val ratings = arrayOf(
            "4.7", "0"
        )

        val distance = arrayOf(
            "50m","200m"
        )

        val images = arrayOf(R.drawable.ellipse3, R.drawable.ellipse3)

        val adapter = MyListAdapter(this, washroomNames, washroomAmenities, images, ratings, distance)
        var list = findViewById<View>(R.id.lv_reviews) as ListView
        list.setAdapter(adapter)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main1, menu) // Menu Resource, Menu
        return true
    }

    // Show filter fragment if filter button is clicked
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            R.id.action_maintain -> {
                val filterDialog = FilterDialogFragment()
                filterDialog.show(supportFragmentManager, "Filter")
                // TODO: Return filter settings and filter reviews
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}