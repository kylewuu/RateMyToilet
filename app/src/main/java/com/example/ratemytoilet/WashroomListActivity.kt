package com.example.ratemytoilet

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ratemytoilet.database.Location
import com.example.ratemytoilet.database.LocationViewModel
import com.example.ratemytoilet.database.ReviewViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


/*
References:
https://stackoverflow.com/questions/21422294/how-to-add-button-in-header
https://www.android--code.com/2020/03/android-kotlin-listview-add-item.html
https://stackoverflow.com/questions/14666106/inserting-a-textview-in-the-middle-of-a-imageview-android
https://www.javatpoint.com/android-custom-listview
 */

class WashroomListActivity : AppCompatActivity(), FilterDialogFragment.FilterListener {

    private lateinit var myListView: ListView
    private lateinit var arrayList: ArrayList<Location>
    private lateinit var arrayAdapter: WashroomListAdapter
    private val monthArray = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    private lateinit var locationViewModel: LocationViewModel
    private var maleCheck = false
    private var femaleCheck = false
    private var paperCheck = false
    private var soapCheck = false
    private var accessCheck = false
    private var cleanlinessStart = 1f
    private var cleanlinessEnd = 5f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        var bundle = intent.extras
        maleCheck = bundle?.getBoolean(MainActivity.MALE_CHECK_KEY) ?: false
        femaleCheck = bundle?.getBoolean(MainActivity.FEMALE_CHECK_KEY) ?: false
        paperCheck = bundle?.getBoolean(MainActivity.PAPER_CHECK_KEY) ?: false
        soapCheck = bundle?.getBoolean(MainActivity.SOAP_CHECK_KEY) ?: false
        accessCheck = bundle?.getBoolean(MainActivity.ACCESS_CHECK_KEY) ?: false
        cleanlinessStart = bundle?.getFloat(MainActivity.CLEANLINESS_START_KEY) ?: 1f
        cleanlinessEnd = bundle?.getFloat(MainActivity.CLEANLINESS_END_KEY) ?: 5f

        // Remove app name from toolbar
        supportActionBar?.setDisplayShowTitleEnabled(false)


        // List of locations
        myListView = findViewById(R.id.lv_locations)

        // Arraylist for displaying entries
        arrayList = ArrayList<Location>()
        arrayAdapter = WashroomListAdapter(this, arrayList)
        myListView.adapter = arrayAdapter
        locationViewModel = LocationViewModel()
        loadWashrooms()

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

    private fun loadWashrooms() {
        lifecycleScope.launch(Dispatchers.IO) {
            var newLocations = ArrayList<Location>()
            var allLocations = locationViewModel.getAllLocations()
            val reviewViewModel = ReviewViewModel()
            if (allLocations != null &&
                    (paperCheck ||
                    soapCheck ||
                    accessCheck ||
                    maleCheck ||
                    femaleCheck ||
                    cleanlinessStart != 1f ||
                    cleanlinessEnd != 5f)
            ) {
                for (location in allLocations) {
                    var shouldAdd = true
                    var rating = 0.0
                    var allReviews = reviewViewModel.getReviewsForLocation(location.id)
                    allReviews = allReviews.sortedByDescending { it.dateAdded }
                    if (allReviews.isNotEmpty()) {
                        for (review in allReviews) {
                            rating += review.cleanliness
                        }
                        rating /= allReviews.size

                        if (paperCheck) {
                            if (allReviews[0].sufficientPaperTowels != 1) {
                                shouldAdd = false
                            }
                        }
                        if (soapCheck) {
                            if (allReviews[0].sufficientSoap != 1) {
                                shouldAdd = false
                            }
                        }
                        if (accessCheck) {
                            if (allReviews[0].accessibility != 1) {
                                shouldAdd = false
                            }
                        }
                    }
                    if ((femaleCheck && !maleCheck) || (!femaleCheck && maleCheck)) {
                        if (maleCheck) {
                            if (location.gender != 0 && location.gender != 2) {
                                shouldAdd = false
                            }
                        }
                        if (femaleCheck) {
                            if (location.gender != 1 && location.gender != 2) {
                                shouldAdd = false
                            }
                        }
                    }

                    if (rating !in cleanlinessStart..cleanlinessEnd) {
                        shouldAdd = false
                    }

                    if (shouldAdd) newLocations.add(location)
                }
            }

            withContext(Main) {
                arrayAdapter.replace(newLocations)
                arrayAdapter.notifyDataSetChanged()
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
                val filterDialogFragment = FilterDialogFragment()
                var bundle = Bundle()
                bundle.putBoolean(MainActivity.MALE_CHECK_KEY, maleCheck)
                bundle.putBoolean(MainActivity.FEMALE_CHECK_KEY, femaleCheck)
                bundle.putBoolean(MainActivity.PAPER_CHECK_KEY, paperCheck)
                bundle.putBoolean(MainActivity.SOAP_CHECK_KEY, soapCheck)
                bundle.putBoolean(MainActivity.ACCESS_CHECK_KEY, accessCheck)
                bundle.putFloat(MainActivity.CLEANLINESS_START_KEY, cleanlinessStart)
                bundle.putFloat(MainActivity.CLEANLINESS_END_KEY, cleanlinessEnd)
                filterDialogFragment.arguments = bundle
                filterDialogFragment.show(supportFragmentManager, "Filter")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onFilterConditionPassed(
        paperCheck: Boolean,
        soapCheck: Boolean,
        accessCheck: Boolean,
        maleCheck: Boolean,
        femaleCheck: Boolean,
        startValue: Float,
        endValue: Float
    ) {
        this.paperCheck = paperCheck
        this.soapCheck = soapCheck
        this.accessCheck = accessCheck
        this.maleCheck = maleCheck
        this.femaleCheck = femaleCheck
        this.cleanlinessStart = startValue
        this.cleanlinessEnd = endValue
        loadWashrooms()
    }
}