package com.example.ratemytoilet

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ratemytoilet.databinding.ActivityAddLocationMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Activity for inputting location for the new washroom.
 *
 */
class AddLocationToNewWashroomActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, GoogleMap.OnMapClickListener {
    // Map vars
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityAddLocationMapBinding
    private lateinit var locationManager: LocationManager
    private lateinit var markerOptions: MarkerOptions

    // Lat Lng var to save
    private lateinit var markerLatLng: LatLng

    // Save Previous Marker
    private var previousMarker: Marker? = null

    // Views
    private lateinit var finishButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddLocationMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (MainActivity.isAdmin) title = "ADMIN - " + getString(R.string.title_activity_add_location_map)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        finishButton = findViewById<Button>(R.id.bt_onFinishAddLocationClick)
        finishButton.setEnabled(false)
        finishButton.setAlpha(.5f)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        markerOptions = MarkerOptions()
        mMap.setOnMapClickListener(this)

        checkPermission()
    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 0)
        else {
            getUserLocation()
        }
    }

    fun getUserLocation() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            val provider : String? = locationManager.getBestProvider(criteria, true)
            if(provider != null) {
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null)
                    onLocationChanged(location)

            }
        } catch (e: SecurityException) {}
    }

    override fun onLocationChanged(location: Location) {
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)
        println("Debug: Init user location ")
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
        mMap.animateCamera(cameraUpdate)
    }

    override fun onMapClick(latLng: LatLng) {
        // Remove previous marker if it exists
        if(previousMarker != null){
            previousMarker?.remove()
        }

        // Add new marker to map, and save its lat and lng values
        markerOptions.position(latLng!!)
        previousMarker = mMap.addMarker(markerOptions)!!
        markerLatLng = latLng

        finishButton.setEnabled(true)
        finishButton.setAlpha(1f)
    }

    fun onFinishAddLocationClick(view: View){
        // Create bundle
        val bundle = Bundle()

        // Place location object into bundle
        bundle.putParcelable("LATLNG_KEY", markerLatLng)

        val intent = Intent().apply {
            putExtras(bundle)
            // Put your data here if you want.
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
