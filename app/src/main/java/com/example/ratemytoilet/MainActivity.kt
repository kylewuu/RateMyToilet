package com.example.ratemytoilet

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.ratemytoilet.database.DatabaseUsageExamples
import com.example.ratemytoilet.database.LocationViewModel
import com.example.ratemytoilet.database.ReviewViewModel
import com.example.ratemytoilet.databinding.ActivityMainBinding
import com.example.ratemytoilet.launch.LaunchActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.ui.IconGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat

class MainActivity :  AppCompatActivity(), OnMapReadyCallback, LocationListener, FilterDialogFragment.FilterListener {
    private var myLocationMarker : Marker ?= null
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding

    private val PERMISSION_REQUEST_CODE = 0
    private lateinit var locationManager: LocationManager

    private var mapCentered = false
    private var washroomId : String ?= null
    private var washroomName : String ?= null
    private var gender : String ?= null
    private var date : String ?= null
    private lateinit var  markerOptions: MarkerOptions
    private lateinit var  polylineOptions: PolylineOptions
    private lateinit var  polylines: ArrayList<Polyline>
    private lateinit var myClusterManager: ClusterManager<MyItem>

    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (getSupportActionBar() != null) {
            getSupportActionBar()?.hide();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val filterButton = findViewById<Button>(R.id.filterButton)
        filterButton.setOnClickListener {
            val filterDialogFragment = FilterDialogFragment()
            filterDialogFragment.show(supportFragmentManager, "Filter")
        }

        val listButton = findViewById<Button>(R.id.listButton)
        listButton.setOnClickListener {
            val washroomListActivityIntent = Intent(this, WashroomListActivity::class.java)
            this.startActivity(washroomListActivityIntent)
        }


        DatabaseUsageExamples.initializeLocationViewModel(this)
    }


    override fun onStart() {
        super.onStart()
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
//            loadLaunchScreen()
        }
    }

    private fun loadLaunchScreen() {
        val intent = Intent(this, LaunchActivity::class.java)
        startActivity(intent)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        polylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLACK)
        polylines = ArrayList()
        markerOptions = MarkerOptions()
        myClusterManager = ClusterManager<MyItem>(applicationContext , mMap)
        myClusterManager.renderer = MarkerClusterRenderer(this, mMap, myClusterManager)
        myClusterManager.markerCollection.setInfoWindowAdapter(MyInfoWindowAdapter(this))
        myClusterManager.setOnClusterItemClickListener {
            washroomId = it.getId()
            washroomName = it.snippet?.split(",")?.get(0)
            val dateTimeFormat : DateFormat = SimpleDateFormat ("MMM.dd.yyyy")
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
            CoroutineScope(Dispatchers.Main).launch {
                if (washroomId != null) {
                    val viewIntent = Intent(this@MainActivity, DisplayActivity::class.java)
                    viewIntent.putExtra("ID", washroomId)
                    viewIntent.putExtra("name", washroomName)
                    viewIntent.putExtra("date", date)
                    viewIntent.putExtra("gender", gender)
                    startActivity(viewIntent)
                }
            }
        }
        mMap.setOnInfoWindowClickListener(myClusterManager)
        Log.d("TAi", mMap.cameraPosition.zoom.toString())
        //addLocation()
        checkPermission()
    }

    fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
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

        markerOptions.position(latLng).title("ME").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        myLocationMarker = mMap.addMarker(markerOptions)!!
    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_CODE)
        else {
            initLocationManager()
            getToiletLocation()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initLocationManager()
                getToiletLocation()
            }
        }
    }

    fun getToiletLocation() {
        val bubble = IconGenerator(this)
        val arr = ArrayList<MyItem>()
        bubble.setStyle(IconGenerator.STYLE_PURPLE)
        val locationViewModel = LocationViewModel()
        val loadingDialogFragment = LoadingDialogFragment()
        CoroutineScope(Dispatchers.IO).launch {
            loadingDialogFragment.show(supportFragmentManager, "Load")
            var allLocations = locationViewModel.getAllLocations()
            val reviewViewModel = ReviewViewModel()
            for (location in allLocations) {
                var rating = 0.0
                var soap = "true"
                var paper = "true"
                var access = "true"
                val allReviews = reviewViewModel.getReviewsForLocation(location.id)
                allReviews.sortedByDescending { it.dateAdded }
                Log.d("TAb",allReviews.size.toString())
                val latLng = LatLng(location.lat, location.lng)
                if (allReviews.size != 0) {
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
                    } else if (allReviews[0].sufficientSoap == 2) {
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

    suspend fun setClusterOnMainThread(locationList : ArrayList<MyItem>) {
        withContext(Dispatchers.Main){
            val fragment = getSupportFragmentManager().findFragmentByTag("Load") as DialogFragment
            fragment.dismiss()
            myClusterManager.addItems(locationList)
            myClusterManager.cluster()
        }
    }

    fun updateToilet(paperCheck: Boolean, soapCheck: Boolean, accessCheck: Boolean, maleCheck: Boolean, femaleCheck: Boolean, startValue: Float, endValue: Float) {
        val bubble = IconGenerator(this)
        val arr = ArrayList<MyItem>()
        var newLocations = ArrayList<com.example.ratemytoilet.database.Location>()
        bubble.setStyle(IconGenerator.STYLE_PURPLE)
        val locationViewModel = LocationViewModel()
        val loadingDialogFragment = LoadingDialogFragment()
        CoroutineScope(Dispatchers.IO).launch {
            loadingDialogFragment.show(supportFragmentManager, "Load")
            var allLocations = locationViewModel.getAllLocations()
            val reviewViewModel = ReviewViewModel()
            for (location in allLocations) {
                var rating = 0.0
                val allReviews = reviewViewModel.getReviewsForLocation(location.id)
                allReviews.sortedByDescending { it.dateAdded }
                if (allReviews.size != 0) {
                    for (review in allReviews) {
                        rating += review.cleanliness
                    }
                    rating /= allReviews.size

                    if (paperCheck) {
                        if (allReviews[0].sufficientPaperTowels == 1) {
                            newLocations.add(location)
                        }
                    }
                    if (soapCheck) {
                        if (allReviews[0].sufficientSoap == 1) {
                            newLocations.add(location)
                        }
                    }
                    if (accessCheck) {
                        if (allReviews[0].accessibility == 1) {
                            newLocations.add(location)
                        }
                    }
                }
                if (maleCheck) {
                    if (location.gender == 0) {
                        newLocations.add(location)
                    }
                }
                if (femaleCheck) {
                    if (location.gender == 1) {
                        newLocations.add(location)
                    }
                }

                if (rating >= startValue && rating <= endValue) {
                    newLocations.add(location)
                }
            }

            val updates = newLocations.distinctBy { it.id }
            for(update in updates) {
                val updateLatLng = LatLng(update.lat, update.lng)
                var updateRating = 0.0
                var updateSoap = "true"
                var updatePaper = "true"
                var updateAccess = "true"
                val updateAllReviews = reviewViewModel.getReviewsForLocation(update.id)
                updateAllReviews.sortedByDescending { it.dateAdded }
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
                    } else if (updateAllReviews[0].sufficientSoap == 2) {
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

    override fun onFilterConditionPassed(paperCheck: Boolean, soapCheck: Boolean, accessCheck: Boolean, maleCheck: Boolean, femaleCheck: Boolean, startValue: Float, endValue: Float) {
        mMap.clear()
        myClusterManager.clearItems()
        updateToilet(paperCheck, soapCheck, accessCheck, maleCheck, femaleCheck, startValue, endValue)
        myClusterManager = ClusterManager<MyItem>(applicationContext , mMap)
        myClusterManager.renderer = MarkerClusterRenderer(this, mMap, myClusterManager)
        myClusterManager.markerCollection.setInfoWindowAdapter(MyInfoWindowAdapter(this))
        myClusterManager.setOnClusterItemClickListener {
            washroomId = it.getId()
            washroomName = it.snippet?.split(",")?.get(0)
            val dateTimeFormat : DateFormat = SimpleDateFormat ("MMM.dd.yyyy")
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
            CoroutineScope(Dispatchers.Main).launch {
                if (washroomId != null) {
                    val viewIntent = Intent(this@MainActivity, DisplayActivity::class.java)
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


    // Start AddNewLocationFragment in order to add a new washroom location
    fun onAddNewLocationClick(view: View) {
        val viewIntent = Intent(this, AddNewLocationFragment::class.java)
        startActivity(viewIntent)
    }

    /*fun addLocation() {
        CoroutineScope(Dispatchers.Main).launch {
            var locationViewModel = LocationViewModel()

            var newLocation1 = com.example.ratemytoilet.database.Location()
            newLocation1.roomNumber = 2001
            newLocation1.gender = 1
            newLocation1.lat = 49.27883170343454
            newLocation1.lng = -122.91723594551543
            newLocation1.date = Calendar.getInstance().timeInMillis
            newLocation1.name = "AQ women washroom"

            var newLocation2 = com.example.ratemytoilet.database.Location()
            newLocation2.roomNumber = 2002
            newLocation2.gender = 0
            newLocation2.lat = 49.278769584950695
            newLocation2.lng = -122.91726410870903
            newLocation2.date = Calendar.getInstance().timeInMillis
            newLocation2.name = "AQ man washroom"

            var newLocation3 = com.example.ratemytoilet.database.Location()
            newLocation3.roomNumber = 2003
            newLocation3.gender = 1
            newLocation3.lat = 49.27916667453397
            newLocation3.lng = -122.91719975380576
            newLocation3.date = Calendar.getInstance().timeInMillis
            newLocation3.name = "AQ women washroom 2"

            var newLocation4 = com.example.ratemytoilet.database.Location()
            newLocation4.roomNumber = 2004
            newLocation4.gender = 1
            newLocation4.lat = 49.27918065286712
            newLocation4.lng = -122.91710615222036
            newLocation4.date = Calendar.getInstance().timeInMillis
            newLocation4.name = "AQ women washroom 3"

            var newLocation5 = com.example.ratemytoilet.database.Location()
            newLocation5.roomNumber = 2005
            newLocation5.gender = 0
            newLocation5.lat = 49.2794941365365
            newLocation5.lng = -122.91683385059164
            newLocation5.date = Calendar.getInstance().timeInMillis
            newLocation5.name = "AQ men washroom 2"

            *//*locationViewModel.addLocation(newLocation1).collect{
                var newReview = Review()
                newReview.locationId = it
                Log.d("TAb", it)
                newReview.leftByAdmin = false
                newReview.cleanliness = 3
                newReview.dateAdded = Calendar.getInstance().timeInMillis
                newReview.sufficientPaperTowels = 1
                newReview.sufficientSoap = 2
                newReview.accessibility = 0
                newReview.comment = "New comment"

                var reviewViewModel = ReviewViewModel()
                reviewViewModel.addReviewForLocation(newReview)
            }*//*
           *//* locationViewModel.addLocation(newLocation2).collect{
                var newReview = Review()
                newReview.locationId = it
                Log.d("TAb", it)
                newReview.leftByAdmin = false
                newReview.cleanliness = 4
                newReview.dateAdded = Calendar.getInstance().timeInMillis
                newReview.sufficientPaperTowels = 0
                newReview.sufficientSoap = 1
                newReview.accessibility = 1
                newReview.comment = "New comment"

                var reviewViewModel = ReviewViewModel()
                reviewViewModel.addReviewForLocation(newReview)
            }*//*
           *//* locationViewModel.addLocation(newLocation3).collect{
                var newReview = Review()
                newReview.locationId = it
                Log.d("TAb", it)
                newReview.leftByAdmin = false
                newReview.cleanliness = 6
                newReview.dateAdded = Calendar.getInstance().timeInMillis
                newReview.sufficientPaperTowels = 3
                newReview.sufficientSoap = 1
                newReview.accessibility = 0
                newReview.comment = "New comment"

                var reviewViewModel = ReviewViewModel()
                reviewViewModel.addReviewForLocation(newReview)
            }*//*
            *//*locationViewModel.addLocation(newLocation4).collect{
                var newReview = Review()
                newReview.locationId = it
                Log.d("TAb", it)
                newReview.leftByAdmin = false
                newReview.cleanliness = 3
                newReview.dateAdded = Calendar.getInstance().timeInMillis
                newReview.sufficientPaperTowels = 1
                newReview.sufficientSoap = 2
                newReview.accessibility = 0
                newReview.comment = "New comment"

                var reviewViewModel = ReviewViewModel()
                reviewViewModel.addReviewForLocation(newReview)
            }*//*
            *//*locationViewModel.addLocation(newLocation5).collect{
                var newReview = Review()
                newReview.locationId = it
                Log.d("TAb", it)
                newReview.leftByAdmin = false
                newReview.cleanliness = 3
                newReview.dateAdded = Calendar.getInstance().timeInMillis
                newReview.sufficientPaperTowels = 1
                newReview.sufficientSoap = 2
                newReview.accessibility = 0
                newReview.comment = "New comment"

                var reviewViewModel = ReviewViewModel()
                reviewViewModel.addReviewForLocation(newReview)
            }*//*
        }*/
    //}
}