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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import androidx.core.app.ActivityCompat

import com.google.firebase.ktx.Firebase
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
import com.google.firebase.firestore.auth.User
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.ui.IconGenerator
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity :  AppCompatActivity(), OnMapReadyCallback, LocationListener{
    private var myLocationMarker : Marker ?= null
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding

    private val PERMISSION_REQUEST_CODE = 0
    private lateinit var locationManager: LocationManager

    private var mapCentered = false
    private var washroomId : String ?= null
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
            val listActivityIntent = Intent(this, ListActivity::class.java)
            this.startActivity(listActivityIntent)
        }

        DatabaseUsageExamples.initializeLocationViewModel(this)
    }

    /*override fun onStart() {
        super.onStart()
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            loadLaunchScreen()
        }
    }

    private fun loadLaunchScreen() {
        val intent = Intent(this, LaunchActivity::class.java)
        startActivity(intent)
    }*/

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
                    viewIntent.putExtra("ID", washroomId)
                    viewIntent.putExtra("ID", washroomId)
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
                var soap = true
                var paper = true
                var access = true
                val allReviews = reviewViewModel.getReviewsForLocation(location.id)
                val latLng = LatLng(location.lat, location.lng)
                if (allReviews.size != 0) {
                    for (review in allReviews) {
                        rating += review.cleanliness
                    }
                    rating /= allReviews.size

                }
                val snippet = location.name + ", " + location.roomNumber + ";" + rating
                val title = soap.toString() + "," + paper.toString() + "," + access.toString()
                val item = MyItem(latLng, title, snippet, BitmapDescriptorFactory.fromBitmap(bubble.makeIcon(rating.toString())), location.id)
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


        //Log.d("TAi", arr.size.toString())
       /* val latLng = LatLng(49.278762746674886, -122.9172651303747)
        val latlng2 = LatLng(49.27883401764781, -122.9172336167242)
        val latlng3 = LatLng(49.279164723163824, -122.91720144989004)
        val latlng4 = LatLng(49.279182669314714, -122.9171085521842)
        val latlng5 = LatLng(49.27950003509996, -122.91687906260248)


        val item = MyItem(latLng, "true,false,true", "AQ, 2008;4.7", BitmapDescriptorFactory.fromBitmap(bubble.makeIcon("4.7")))
        val item2 = MyItem(latlng2, "true,true,true", "AQ, 2007;4.5", BitmapDescriptorFactory.fromBitmap(bubble.makeIcon("4.5")))
        val item3 = MyItem(latlng3, "false,true,false", "AQ, 2006;4.3", BitmapDescriptorFactory.fromBitmap(bubble.makeIcon("4.3")))
        val item4 = MyItem(latlng4, "false,false,false", "AQ, 2005;3.1", BitmapDescriptorFactory.fromBitmap(bubble.makeIcon("3.1")))
        val item5 = MyItem(latlng5, "false,false,false", "AQ, 2004;3.1", BitmapDescriptorFactory.fromBitmap(bubble.makeIcon("3.1")))
        arr.add(item)
        arr.add(item2)
        arr.add(item3)
        arr.add(item4)
        arr.add(item5)*/



        //val toiletMarkOptions = MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.warning)).anchor(bubble.getAnchorU(), bubble.getAnchorV()).title("true, false, true").snippet("AQ, 2008;4.7")
        //myMarker = mMap.addMarker(toiletMarkOptions)!!
        //myMarker.showInfoWindow()

    /**
     * Adds a new location to the firestore. Currently does not add any real data.
     */
    /*fun addNewLocation(view: View) {
        count++
        Locations(count)
    }*/

    fun addLocation() {
        var locationViewModel = LocationViewModel()
        var newLocation = com.example.ratemytoilet.database.Location()
        newLocation.roomNumber = 2008
        newLocation.gender = 1
        newLocation.lat = 49.278762746674886
        newLocation.lng = -122.9172651303747
        newLocation.date = Calendar.getInstance().timeInMillis
        newLocation.name = "AQ, 2008"

        var newLocation1 = com.example.ratemytoilet.database.Location()
        newLocation1.roomNumber = 2007
        newLocation1.gender = 1
        newLocation1.lat = 49.27883401764781
        newLocation1.lng = -122.9172336167242
        newLocation1.date = Calendar.getInstance().timeInMillis
        newLocation1.name = "AQ, 2007"

        var newLocation2 = com.example.ratemytoilet.database.Location()
        newLocation2.roomNumber = 2006
        newLocation2.gender = 1
        newLocation2.lat = 49.279164723163824
        newLocation2.lng = -122.91720144989004
        newLocation2.date = Calendar.getInstance().timeInMillis
        newLocation2.name = "AQ, 2006"

        var newLocation3 = com.example.ratemytoilet.database.Location()
        newLocation3.roomNumber = 2005
        newLocation3.gender = 1
        newLocation3.lat = 49.279182669314714
        newLocation3.lng = -122.9171085521842
        newLocation3.date = Calendar.getInstance().timeInMillis
        newLocation3.name = "AQ, 2005"

        var newLocation4 = com.example.ratemytoilet.database.Location()
        newLocation4.roomNumber = 2004
        newLocation4.gender = 1
        newLocation4.lat = 49.27950003509996
        newLocation4.lng = -122.91687906260248
        newLocation4.date = Calendar.getInstance().timeInMillis
        newLocation4.name = "AQ, 2004"

        locationViewModel.addLocation(newLocation)
        locationViewModel.addLocation(newLocation1)
        locationViewModel.addLocation(newLocation2)
        locationViewModel.addLocation(newLocation3)
        locationViewModel.addLocation(newLocation4)
    }

}

