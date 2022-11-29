package com.example.ratemytoilet

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.ratemytoilet.database.Location
import com.example.ratemytoilet.database.LocationViewModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


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
    private val monthArray = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")


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
                val location = arrayAdapter.getItem(p2)

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = location.date
                val month = calendar.get(Calendar.MONTH)
                val monthName = monthArray[month]
                val date = calendar.get(Calendar.DAY_OF_MONTH)
                val year = calendar.get(Calendar.YEAR)

                var gender = ""
                if (location.gender == 0) {
                    gender = "Male"
                } else if (location.gender == 1) {
                    gender = "Female"
                } else {
                    gender = "Universal"
                }

                val viewIntent = Intent(this@WashroomListActivity, DisplayActivity::class.java)
                viewIntent.putExtra("ID", location.id)
                viewIntent.putExtra("name", location.name)
                viewIntent.putExtra("date", monthName + ". " + date.toString() + ", " +  year.toString())
                viewIntent.putExtra("gender", gender)
                startActivity(viewIntent)




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