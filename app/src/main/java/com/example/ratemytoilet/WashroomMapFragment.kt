package com.example.ratemytoilet

import android.Manifest
import android.content.*
import android.content.Context.MODE_PRIVATE
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.ratemytoilet.MainActivity.Companion.accessCheck
import com.example.ratemytoilet.MainActivity.Companion.cleanlinessEnd
import com.example.ratemytoilet.MainActivity.Companion.cleanlinessStart
import com.example.ratemytoilet.MainActivity.Companion.femaleCheck
import com.example.ratemytoilet.MainActivity.Companion.maleCheck
import com.example.ratemytoilet.MainActivity.Companion.notRunFirstTime
import com.example.ratemytoilet.MainActivity.Companion.paperCheck
import com.example.ratemytoilet.MainActivity.Companion.previousLocationsSize
import com.example.ratemytoilet.MainActivity.Companion.soapCheck
import com.example.ratemytoilet.database.LocationViewModel
import com.example.ratemytoilet.database.ReviewViewModel
import com.example.ratemytoilet.launch.LaunchActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.ui.IconGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat

private const val TAG = "WashroomMapFragment"

class WashroomMapFragment : Fragment(), OnMapReadyCallback, LocationListener{
    private var myLocationMarker : Marker?= null
    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView

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
    private lateinit var updateMap : String
    private lateinit var updatePreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var locationPermissionResultReceiver: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // We must register the ActivityResult in onCreate, to ensure it gets registered each time
        // this fragment is created.
        locationPermissionResultReceiver = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                initLocationManager()
                loadWashrooms()
            } else {
                Toast.makeText(activity,"Permission Denied",Toast.LENGTH_SHORT).show()
            }
        }

        // Register for Receiver
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        filter.addAction(Intent.ACTION_PROVIDER_CHANGED)
        getActivity()?.registerReceiver(locationSwitchStateReceiver, filter)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_washroom_map, container, false)

//        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapView = view.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        locationPermissionResultReceiver.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        locationViewModel = LocationViewModel()
        loadingDialogFragment = LoadingDialogFragment()
        updatePreference = activity?.getSharedPreferences("update", MODE_PRIVATE)!!
        editor = updatePreference.edit()

        if ((activity as AppCompatActivity).getSupportActionBar() != null) {
            (activity as AppCompatActivity).getSupportActionBar()?.hide();
        }

        val filterButton = view.findViewById<Button>(R.id.filterButton)
        filterButton.setOnClickListener {
            val filterDialogFragment = FilterDialogFragment()
            filterDialogFragment.show(childFragmentManager, "Filter")
        }

        val addButton = view.findViewById<FloatingActionButton>(R.id.addNewLocation)
        addButton.setOnClickListener {
            onAddNewLocationClick()
        }

        locationViewModel = LocationViewModel()
        locationViewModel.locations.observe(viewLifecycleOwner) {
            if (previousLocationsSize == -1) {
                previousLocationsSize = it.size
            }
            if (previousLocationsSize != it.size) {
                Toast.makeText(activity, "New Location added", Toast.LENGTH_SHORT).show()
            }
            Log.d("TAp", previousLocationsSize.toString())
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        if (notRunFirstTime) {
            val sharedPref = activity?.getSharedPreferences("update", MODE_PRIVATE)
            updateMap = sharedPref?.getString("updateReview", "NULL").toString()
            //Log.d("TAp", updateMap)
            if (updateMap != "NULL") {
                if (updateMap == "Yes") {
                    editor.putString("updateReview", "No")
                    editor.apply()
                    if (mMap != null && myClusterManager != null) {
                        mMap.clear()
                        myClusterManager.clearItems()
                        loadWashrooms()
                        setClusterManager()
                    }
                }
            }

        } else {
            notRunFirstTime = true
        }
    }

   override fun onStart() {
        super.onStart()
       mapView.onStart()

        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            loadLaunchScreen()
        }
    }

    private fun loadLaunchScreen() {
        val intent = Intent(activity, LaunchActivity::class.java)
        //startActivity(intent)
    }

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
        Log.d(TAG, "Map is ready!")
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        polylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLACK)
        polylines = ArrayList()
        markerOptions = MarkerOptions()

        setClusterManager()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()

        if (locationManager != null)
            locationManager.removeUpdates(this)

        // Unregister receiver
        getActivity()?.unregisterReceiver(locationSwitchStateReceiver)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
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

    private fun loadWashrooms() {
        val bubble = IconGenerator(context)
        val arr = ArrayList<MyItem>()
        var newLocations = ArrayList<com.example.ratemytoilet.database.Location>()
        bubble.setStyle(IconGenerator.STYLE_PURPLE)
        if (loadingDialogFragment.dialog == null || !loadingDialogFragment.dialog?.isShowing!!) loadingDialogFragment.show(childFragmentManager, "Load")
        lifecycleScope.launch(Dispatchers.IO) {
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
            } else if (allLocations != null){
                newLocations = allLocations as java.util.ArrayList<com.example.ratemytoilet.database.Location>
            }

            if (newLocations.isNotEmpty()) {
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
    }

    fun filterConditionPassed(paperCheck: Boolean, soapCheck: Boolean, accessCheck: Boolean, maleCheck: Boolean, femaleCheck: Boolean, startValue: Float, endValue: Float) {
        mMap.clear()
        myClusterManager.clearItems()
        saveFilterConditions(paperCheck, soapCheck, accessCheck, maleCheck, femaleCheck, startValue, endValue)
        loadWashrooms()
        setClusterManager()
    }

    fun onAddNewLocationClick() {
        val viewIntent = Intent(activity, AddNewLocationActivity::class.java)
        startActivity(viewIntent)
    }

    private fun saveFilterConditions(paperCheck: Boolean, soapCheck: Boolean, accessCheck: Boolean, maleCheck: Boolean, femaleCheck: Boolean, startValue: Float, endValue: Float) {
        MainActivity.paperCheck = paperCheck
        MainActivity.soapCheck = soapCheck
        MainActivity.accessCheck = accessCheck
        MainActivity.maleCheck = maleCheck
        MainActivity.femaleCheck = femaleCheck
        MainActivity.cleanlinessStart = startValue
        MainActivity.cleanlinessEnd = endValue
    }

    override fun onLocationChanged(locations: MutableList<Location>) {
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String) {
        println("DEBUG: Provider Enabled" )
    }

    override fun onProviderDisabled(provider: String) {
        println("DEBUG: Provider Disabled" )
    }



    private fun setClusterManager() {
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



    // Detects if the location is turned on in the map fragment. If detected, it will start the location manager again with function initLocationManager().

    // Helps to solve the issue where the location is turned off in another fragment (ex. Washroom List Fragment), and the map view is re-opened with location still turned off.
    // This will detect if the location is turned back on in the map view and start up location manager to detect new locations. Otherwise, location will not be updated.
    private val locationSwitchStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (LocationManager.PROVIDERS_CHANGED_ACTION == intent.action) {
                val locationManager =
                    context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                if (isGpsEnabled || isNetworkEnabled) {
                    //  is enabled
                    initLocationManager()
                }
            }
        }
    }



}