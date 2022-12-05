package com.example.ratemytoilet


import android.content.Context
import android.content.*
import android.content.Context.MODE_PRIVATE
import android.location.Criteria
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.ratemytoilet.MainActivity.Companion.accessCheck
import com.example.ratemytoilet.MainActivity.Companion.cleanlinessEnd
import com.example.ratemytoilet.MainActivity.Companion.cleanlinessStart
import com.example.ratemytoilet.MainActivity.Companion.femaleCheck
import com.example.ratemytoilet.MainActivity.Companion.maleCheck
import com.example.ratemytoilet.MainActivity.Companion.paperCheck
import com.example.ratemytoilet.MainActivity.Companion.soapCheck
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

class WashroomListFragment : Fragment(), FilterDialogFragment.FilterListener, LocationListener{

    private lateinit var myListView: ListView
    private lateinit var toolbar: Toolbar
    private lateinit var arrayList: ArrayList<Location>
    private lateinit var arrayAdapter: WashroomListAdapter
    private val monthArray = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    private lateinit var locationViewModel: LocationViewModel

    private lateinit var updatePreference : SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    // User location vars
    private lateinit var locationManager: LocationManager
    private var userLocation: android.location.Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)
        toolbar = view.findViewById(R.id.toolbar)


        updatePreference = requireContext().getSharedPreferences("update", MODE_PRIVATE)
        editor = updatePreference.edit()



        // Register for Receiver
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        filter.addAction(Intent.ACTION_PROVIDER_CHANGED)
        context?.registerReceiver(locationSwitchStateReceiver, filter)


        // List of locations
        myListView = view.findViewById(R.id.lv_locations)

        // Arraylist for displaying entries
        arrayList = ArrayList<Location>()
        arrayAdapter = WashroomListAdapter(requireContext(), arrayList)
        myListView.adapter = arrayAdapter
        locationViewModel = LocationViewModel()

        // Attempt to get users location
        getUserLocation()

        // Load washrooms
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

                val viewIntent = Intent(requireActivity(), DisplayActivity::class.java)
                viewIntent.putExtra("ID", location.id)
                viewIntent.putExtra("name", location.name)
                viewIntent.putExtra("date", monthName + ". " + date.toString() + ", " +  year.toString())
                viewIntent.putExtra("gender", gender)
                startActivity(viewIntent)
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.inflateMenu(R.menu.main1)

    }

    private fun loadWashrooms() {
        lifecycleScope.launch(Dispatchers.IO) {
            var newLocations = ArrayList<Location>()
            var allLocations = locationViewModel.getAllLocations()
            var distanceArray = ArrayList<Float?>()
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
            } else {
                newLocations = allLocations as ArrayList<Location>
            }


            if(userLocation != null) {
                newLocations =
                    newLocations.sortedBy{ calculateDistanceToUser(it.lat, it.lng) }
                        .toCollection(ArrayList())
            }

            withContext(Main) {
                arrayAdapter.replace(newLocations)
                arrayAdapter.notifyDataSetChanged()
            }
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
        MainActivity.paperCheck = paperCheck
        MainActivity.soapCheck = soapCheck
        MainActivity.accessCheck = accessCheck
        MainActivity.maleCheck = maleCheck
        MainActivity.femaleCheck = femaleCheck
        MainActivity.cleanlinessStart = startValue
        MainActivity.cleanlinessEnd = endValue

        editor.putString("updateReview", "Yes")
        editor.apply()

        loadWashrooms()
    }


    // Get the current users location
    private fun getUserLocation() {
        try {

            locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            val provider : String? = locationManager.getBestProvider(criteria, true)


            if(provider != null) {
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null){
                    // Location found. Set the user's location in the WashroomListAdapter and notify
                    userLocation = location
                    loadWashrooms()

                    arrayAdapter.replaceUserLocation(userLocation)
                    arrayAdapter.notifyDataSetChanged()
                }
                else{
                    // Otherwise, wait for provider to give us a new location
                    locationManager.requestLocationUpdates(provider, 0, 0f, this)
                }
            }
        }
        catch (e: SecurityException) {
        }

    }

    override fun onLocationChanged(location: android.location.Location) {
        // Location found. Set the user's location in the WashroomListAdapter and notify
        userLocation = location
        loadWashrooms()
        arrayAdapter.replaceUserLocation(userLocation)
        arrayAdapter.notifyDataSetChanged()


        // Only needs to be updated once. Remove the request
        locationManager.removeUpdates(this)

    }


    private val locationSwitchStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (LocationManager.PROVIDERS_CHANGED_ACTION == intent.action) {
                val locationManager =
                    context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                if (isGpsEnabled || isNetworkEnabled) {
                    // If location is enabled, try to get users location
                    getUserLocation()
                }
                else{
                    // Location is disabled. Set distances in Washroom list to --- by setting `passedInUserLocation` to null
                    arrayAdapter.replaceUserLocation(null)
                    arrayAdapter.notifyDataSetChanged()
                }
            }
        }

    }

    fun calculateDistanceToUser(lat: Double, lng: Double): Float? {

        // Calculate distances
        var washroomLocation: android.location.Location? = android.location.Location("")
        washroomLocation?.latitude = lat
        washroomLocation?.longitude = lng
        var distanceFromUserToWashroom = userLocation?.distanceTo(washroomLocation)


        return distanceFromUserToWashroom


    }








}