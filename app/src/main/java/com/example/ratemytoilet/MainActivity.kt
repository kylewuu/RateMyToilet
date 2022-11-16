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
import android.view.View
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ratemytoilet.database.Locations
import com.example.ratemytoilet.databinding.ActivityMainBinding
import com.example.ratemytoilet.launch.LaunchActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.maps.android.ui.IconGenerator

class MainActivity :  AppCompatActivity(), OnMapReadyCallback, LocationListener, OnMarkerClickListener, OnInfoWindowClickListener {
    private var myLocationMarker : Marker ?= null
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding

    private val PERMISSION_REQUEST_CODE = 0
    private lateinit var locationManager: LocationManager

    private var mapCentered = false
    private lateinit var myMarker : Marker
    private lateinit var  markerOptions: MarkerOptions
    private lateinit var  polylineOptions: PolylineOptions
    private lateinit var  polylines: ArrayList<Polyline>

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
            val filterDialog = FilterDialogFragment()
            filterDialog.show(supportFragmentManager, "Filter")
        }

        val listButton = findViewById<Button>(R.id.listButton)
        listButton.setOnClickListener {
            val listActivityIntent = Intent(this, ListActivity::class.java)
            this.startActivity(listActivityIntent)
        }

    }

    override fun onStart() {
        super.onStart()
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            loadLaunchScreen()
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
        mMap.setOnMarkerClickListener(this)
        mMap.setInfoWindowAdapter(MyInfoWindowAdapter(this))
        mMap.setOnInfoWindowClickListener(this)
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
        val latLng = LatLng(49.278762746674886, -122.9172651303747)
        val bubble = IconGenerator(this)
        bubble.setStyle(IconGenerator.STYLE_PURPLE)
        val toiletMarkOptions = MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(bubble.makeIcon("4.7"))).anchor(bubble.getAnchorU(), bubble.getAnchorV()).title("true, false, true").snippet("AQ, 2008;4.7")
        myMarker = mMap.addMarker(toiletMarkOptions)!!
        //myMarker.showInfoWindow()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        if (marker.equals(myMarker)) {
            marker.showInfoWindow()
            return false
        }
        return true
    }

    /**
     * Adds a new location to the firestore. Currently does not add any real data.
     */
    fun addNewLocation(view: View) {
        count++
        Locations(count)
    }

    override fun onInfoWindowClick(marker: Marker) {
        val viewIntent = Intent(this, DisplayActivity::class.java)
        startActivity(viewIntent)
    }

}