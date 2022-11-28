package com.example.ratemytoilet

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.ratemytoilet.database.Location
import com.example.ratemytoilet.database.LocationViewModel


/*
References:
https://stackoverflow.com/questions/21422294/how-to-add-button-in-header
https://www.android--code.com/2020/03/android-kotlin-listview-add-item.html
https://stackoverflow.com/questions/14666106/inserting-a-textview-in-the-middle-of-a-imageview-android
https://www.javatpoint.com/android-custom-listview
 */

class WashroomListActivity : AppCompatActivity() {

    private lateinit var myListView: ListView
    private lateinit var arrayList: ArrayList<Location>
    private lateinit var arrayAdapter: WashroomListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)


        // Remove app name from toolbar
        supportActionBar?.setDisplayShowTitleEnabled(false)


        // List of locations
        myListView = findViewById<ListView>(R.id.lv_locations)


        // Arraylist for displaying entries
        arrayList = ArrayList<Location>()
        arrayAdapter = WashroomListAdapter(this, arrayList)
        myListView.adapter = arrayAdapter
        var locationViewModel = LocationViewModel()


        // Reload the array with database values
        locationViewModel.locations.observe(this, Observer { it ->
            arrayAdapter.replace(it)
            arrayAdapter.notifyDataSetChanged()
        })


        // TODO: When a location is clicked, show its reviews
        myListView.isClickable = true
        myListView.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
               //println("Position in listView clicked: " + p2)
            }
        }
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