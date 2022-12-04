package com.example.ratemytoilet

import android.Manifest
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.ratemytoilet.database.LocationViewModel
import com.example.ratemytoilet.database.ReviewViewModel
import com.example.ratemytoilet.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.ui.IconGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat

class MainFragment : Fragment(), OnMapReadyCallback, LocationListener{
    private var myLocationMarker : Marker?= null
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding

    private val PERMISSION_REQUEST_CODE = 0
    private lateinit var locationManager: LocationManager
    private lateinit var locationViewModel: LocationViewModel

    private var mapCentered = false
    private var washroomId : String ?= null
    private var washroomName : String ?= null
    private var gender : String ?= null
    private var date : String ?= null
    private lateinit var  markerOptions: MarkerOptions
    private lateinit var  polylineOptions: PolylineOptions
    private lateinit var  polylines: ArrayList<Polyline>
    private lateinit var myClusterManager: ClusterManager<MyItem>
    private lateinit var loadingDialogFragment: LoadingDialogFragment
    private lateinit var swipeToRefresh : SwipeRefreshLayout
    private lateinit var updateMap : String


    private var notRunFirstTime = false
    private var maleCheck = false
    private var femaleCheck = false
    private var paperCheck = false
    private var soapCheck = false
    private var accessCheck = false
    private var cleanlinessStart = 1f
    private var cleanlinessEnd = 5f
    private var previousLocationsSize = -1
    private val locationPermissionResultReceiver = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            initLocationManager()
            getToiletLocation()
            //Log.d("TAp", "true")
        } else {
            Toast.makeText(activity,"Permission Denied",Toast.LENGTH_SHORT).show()
        }
    }


    companion object {
        var MALE_CHECK_KEY = "male_check_key"
        var FEMALE_CHECK_KEY = "female_check_key"
        var PAPER_CHECK_KEY = "paper_check_key"
        var SOAP_CHECK_KEY = "soap_check_key"
        var ACCESS_CHECK_KEY = "access_check_key"
        var CLEANLINESS_START_KEY = "cleanliness_start_key"
        var CLEANLINESS_END_KEY = "cleanliness_end_key"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_main, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        locationViewModel = LocationViewModel()
        loadingDialogFragment = LoadingDialogFragment()

        if ((activity as AppCompatActivity).getSupportActionBar() != null) {
            (activity as AppCompatActivity).getSupportActionBar()?.hide();
        }

        val filterButton = view.findViewById<Button>(R.id.filterButton)
        filterButton.setOnClickListener {
            val filterDialogFragment = FilterDialogFragment()
            var bundle = Bundle()
            bundle.putBoolean(MALE_CHECK_KEY, maleCheck)
            bundle.putBoolean(FEMALE_CHECK_KEY, femaleCheck)
            bundle.putBoolean(PAPER_CHECK_KEY, paperCheck)
            bundle.putBoolean(SOAP_CHECK_KEY, soapCheck)
            bundle.putBoolean(ACCESS_CHECK_KEY, accessCheck)
            bundle.putFloat(CLEANLINESS_START_KEY, cleanlinessStart)
            bundle.putFloat(CLEANLINESS_END_KEY, cleanlinessEnd)
            filterDialogFragment.arguments = bundle
            filterDialogFragment.show(childFragmentManager, "Filter")
        }

        val listButton = view.findViewById<Button>(R.id.listButton)
        listButton.setOnClickListener {
            val washroomListActivityIntent = Intent(activity, WashroomListActivity::class.java)
            this.startActivity(washroomListActivityIntent)
        }

        val addButton = view.findViewById<Button>(R.id.addNewLocation)
        addButton.setOnClickListener {
            onAddNewLocationClick()
        }

        locationViewModel = LocationViewModel()
        locationViewModel.locations.observe(viewLifecycleOwner) {
            if (previousLocationsSize == -1) {
                previousLocationsSize = it.size
            }
            if (previousLocationsSize != it.size) {
                previousLocationsSize = it.size
                Toast.makeText(activity, "New Location added", Toast.LENGTH_SHORT).show()
            }
            Log.d("TAp", previousLocationsSize.toString())
        }

        return view
    }

    override fun onResume() {
        if (notRunFirstTime) {
            val sharedPref = activity?.getSharedPreferences("update", MODE_PRIVATE)
            updateMap = sharedPref?.getString("updateReview", "NULL").toString()
            //Log.d("TAp", updateMap)
            if (updateMap != "NULL") {
                if (updateMap == "Yes") {
                    if (mMap != null && myClusterManager != null) {
                        mMap.clear()
                        myClusterManager.clearItems()
                        getToiletLocation()
                        mMap.setOnMarkerClickListener(myClusterManager)
                        mMap.setOnCameraIdleListener(myClusterManager)
                        mMap.setInfoWindowAdapter(myClusterManager.markerManager)
                        myClusterManager.setOnClusterItemInfoWindowClickListener {
                            lifecycleScope.launch(Dispatchers.IO) {
                                if (washroomId != null) {
                                    val viewIntent = Intent(activity, DisplayActivity::class.java)
                                    viewIntent.putExtra("ID", washroomId)
                                    viewIntent.putExtra("name", washroomName)
                                    viewIntent.putExtra("date", date)
                                    viewIntent.putExtra("gender", gender)
                                    startActivity(viewIntent)
                                }
                            }
                        }
                        mMap.setOnInfoWindowClickListener(myClusterManager)

                    }
                }
            }

        } else {
            notRunFirstTime = true
        }

        super.onResume()
    }

   /* override fun onStart() {
        super.onStart()
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            loadLaunchScreen()
        }
    }

    private fun loadLaunchScreen() {
        val intent = Intent(activity, LaunchActivity::class.java)
        startActivity(intent)
    }*/

    fun initLocationManager() {
        try {
            locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            val provider : String? = locationManager.getBestProvider(criteria, true)
            if(provider != null) {
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null)
                    onLocationChanged(location)
                locationManager.requestLocationUpdates(provider, 0, 0f, this)
            }
        } catch (e: SecurityException) {
        }
    }

    override fun onLocationChanged(location: Location) {
        println("debug: onlocationchanged()")
        if (myLocationMarker != null) {
            myLocationMarker!!.remove()
        }
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)
        if (mapCentered == false) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
            mMap.animateCamera(cameraUpdate)
            mapCentered = true
        }

        markerOptions.position(latLng).title("ME").icon(
            BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_BLUE))
        myLocationMarker = mMap.addMarker(markerOptions)!!
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        polylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLACK)
        polylines = ArrayList()
        markerOptions = MarkerOptions()
        myClusterManager = ClusterManager<MyItem>(activity?.applicationContext , mMap)
        myClusterManager.renderer = context?.let { MarkerClusterRenderer(it, mMap, myClusterManager) }
        myClusterManager.markerCollection.setInfoWindowAdapter(context?.let { MyInfoWindowAdapter(it) })
        myClusterManager.setOnClusterItemClickListener {
            washroomId = it.getId()
            washroomName = it.snippet?.split(",")?.get(0)

            val dateTimeFormat : DateFormat = SimpleDateFormat ("MMM dd, yyyy")
            date = dateTimeFormat.format(it.getDate())
            if (it.getGender() == 0) {
                gender = "Male"
            } else if (it.getGender() == 1) {
                gender = "Female"
            } else {
                gender = "Universal"
            }
            return@setOnClusterItemClickListener false
        }
        mMap.setOnMarkerClickListener(myClusterManager)
        mMap.setOnCameraIdleListener(myClusterManager)
        mMap.setInfoWindowAdapter(myClusterManager.markerManager)
        myClusterManager.setOnClusterItemInfoWindowClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                if (washroomId != null) {
                    val viewIntent = Intent(activity, DisplayActivity::class.java)
                    viewIntent.putExtra("ID", washroomId)
                    viewIntent.putExtra("name", washroomName)
                    viewIntent.putExtra("date", date)
                    viewIntent.putExtra("gender", gender)
                    startActivity(viewIntent)
                }
            }
        }
        mMap.setOnInfoWindowClickListener(myClusterManager)

        locationPermissionResultReceiver.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null)
            locationManager.removeUpdates(this)
    }

    fun getToiletLocation() {
        val bubble = IconGenerator(context)
        val arr = ArrayList<MyItem>()
        bubble.setStyle(IconGenerator.STYLE_PURPLE)
        if (loadingDialogFragment.dialog == null || !loadingDialogFragment.dialog?.isShowing!!) loadingDialogFragment.show(childFragmentManager, "Load")
        lifecycleScope.launch(Dispatchers.IO) {
            var allLocations = locationViewModel.getAllLocations()
            val reviewViewModel = ReviewViewModel()
            for (location in allLocations) {
                var rating = 0.0
                var soap = "true"
                var paper = "true"
                var access = "true"
                var allReviews = reviewViewModel.getReviewsForLocation(location.id)
                allReviews = allReviews.sortedByDescending { it.dateAdded }
                val latLng = LatLng(location.lat, location.lng)
                if (allReviews.isNotEmpty()) {
                    for (review in allReviews) {
                        rating += review.cleanliness
                    }
                    rating /= allReviews.size
                    if (allReviews[0].sufficientSoap == 0) {
                        soap = "false"
                    } else if (allReviews[0].sufficientSoap == 2) {
                        soap = "unknown"
                    }


                    if (allReviews[0].sufficientPaperTowels == 0) {
                        paper = "false"
                    } else if (allReviews[0].sufficientPaperTowels == 2) {
                        paper = "unknown"
                    }

                    if (allReviews[0].accessibility == 0) {
                        access = "false"
                    } else if (allReviews[0].accessibility == 2) {
                        access = "unknown"
                    }
                }

                val snippet = location.name + ", " + location.roomNumber + ";" + rating.toInt()
                val title = soap + "," + paper + "," + access
                val item = MyItem(latLng, title, snippet, BitmapDescriptorFactory.fromBitmap(bubble.makeIcon(rating.toInt().toString())), location.id, location.date, location.gender)
                arr.add(item)
            }
            setClusterOnMainThread(arr)
        }
    }

    private suspend fun setClusterOnMainThread(locationList : ArrayList<MyItem>) {
        withContext(Dispatchers.Main){
            var loadFragment = childFragmentManager.findFragmentByTag("Load")
            if (loadFragment != null) {
                val fragment = loadFragment as DialogFragment
                fragment.dismiss()
            }
            myClusterManager.addItems(locationList)
            myClusterManager.cluster()
        }
    }

    private fun updateToilet() {
        val bubble = IconGenerator(context)
        val arr = ArrayList<MyItem>()
        var newLocations = ArrayList<com.example.ratemytoilet.database.Location>()
        bubble.setStyle(IconGenerator.STYLE_PURPLE)
        if (loadingDialogFragment.dialog == null || !loadingDialogFragment.dialog?.isShowing!!) loadingDialogFragment.show(childFragmentManager, "Load")
        lifecycleScope.launch(Dispatchers.IO) {
            var allLocations = locationViewModel.getAllLocations()
            val reviewViewModel = ReviewViewModel()
            if (allLocations != null) {
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

            val updates = newLocations.distinctBy { it.id }
            for(update in updates) {
                val updateLatLng = LatLng(update.lat, update.lng)
                var updateRating = 0.0
                var updateSoap = "true"
                var updatePaper = "true"
                var updateAccess = "true"
                var updateAllReviews = reviewViewModel.getReviewsForLocation(update.id)
                updateAllReviews = updateAllReviews.sortedByDescending { it.dateAdded }
                if (updateAllReviews.size != 0) {
                    for (review in updateAllReviews) {
                        updateRating += review.cleanliness
                    }
                    updateRating /= updateAllReviews.size
                    if (updateAllReviews[0].sufficientSoap == 0) {
                        updateSoap = "false"
                    } else if (updateAllReviews[0].sufficientSoap == 2) {
                        updateSoap = "unknown"
                    }

                    if (updateAllReviews[0].sufficientPaperTowels == 0) {
                        updatePaper = "false"
                    } else if (updateAllReviews[0].sufficientPaperTowels == 2) {
                        updatePaper = "unknown"
                    }

                    if (updateAllReviews[0].accessibility == 0) {
                        updateAccess = "false"
                    } else if (updateAllReviews[0].accessibility == 2) {
                        updateAccess = "unknown"
                    }
                }
                val snippet = update.name + ", " + update.roomNumber + ";" + updateRating.toInt()
                val title = updateSoap + "," + updatePaper + "," + updateAccess
                val item = MyItem(
                    updateLatLng,
                    title,
                    snippet,
                    BitmapDescriptorFactory.fromBitmap(
                        bubble.makeIcon(
                            updateRating.toInt().toString()
                        )
                    ),
                    update.id,
                    update.date,
                    update.gender
                )
                arr.add(item)
            }
            setClusterOnMainThread(arr)
        }
    }

    fun filterConditionPassed(paperCheck: Boolean, soapCheck: Boolean, accessCheck: Boolean, maleCheck: Boolean, femaleCheck: Boolean, startValue: Float, endValue: Float) {
        mMap.clear()
        myClusterManager.clearItems()
        //Log.d("TAy", paperCheck.toString())
        saveFilterConditions(paperCheck, soapCheck, accessCheck, maleCheck, femaleCheck, startValue, endValue)
        updateToilet()
        myClusterManager = ClusterManager<MyItem>(activity?.applicationContext , mMap)
        myClusterManager.renderer = context?.let { MarkerClusterRenderer(it, mMap, myClusterManager) }
        myClusterManager.markerCollection.setInfoWindowAdapter(context?.let { MyInfoWindowAdapter(it) })
        myClusterManager.setOnClusterItemClickListener {
            washroomId = it.getId()
            washroomName = it.snippet?.split(",")?.get(0)

            val dateTimeFormat : DateFormat = SimpleDateFormat ("MMM dd, yyyy")
            date = dateTimeFormat.format(it.getDate())
            if (it.getGender() == 0) {
                gender = "Male"
            } else if (it.getGender() == 1) {
                gender = "Female"
            } else {
                gender = "Universal"
            }
            return@setOnClusterItemClickListener false
        }


        mMap.setOnMarkerClickListener(myClusterManager)
        mMap.setOnCameraIdleListener(myClusterManager)
        mMap.setInfoWindowAdapter(myClusterManager.markerManager)
        myClusterManager.setOnClusterItemInfoWindowClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                if (washroomId != null) {
                    val viewIntent = Intent(activity, DisplayActivity::class.java)
                    viewIntent.putExtra("ID", washroomId)
                    viewIntent.putExtra("name", washroomName)
                    viewIntent.putExtra("date", date)
                    viewIntent.putExtra("gender", gender)
                    startActivity(viewIntent)
                }
            }
        }
        mMap.setOnInfoWindowClickListener(myClusterManager)
    }

    fun onAddNewLocationClick() {
        val viewIntent = Intent(activity, AddNewLocationFragment::class.java)
        startActivity(viewIntent)
    }

    private fun saveFilterConditions(paperCheck: Boolean, soapCheck: Boolean, accessCheck: Boolean, maleCheck: Boolean, femaleCheck: Boolean, startValue: Float, endValue: Float) {
        this.paperCheck = paperCheck
        this.soapCheck = soapCheck
        this.accessCheck = accessCheck
        this.maleCheck = maleCheck
        this.femaleCheck = femaleCheck
        this.cleanlinessStart = startValue
        this.cleanlinessEnd = endValue
    }

    override fun onLocationChanged(locations: MutableList<Location>) {
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String) {
    }

    override fun onProviderDisabled(provider: String) {
    }

}