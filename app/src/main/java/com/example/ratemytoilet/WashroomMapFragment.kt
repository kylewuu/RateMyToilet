package com.example.ratemytoilet

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.ratemytoilet.MainActivity.Companion.cleanlinessEnd
import com.example.ratemytoilet.MainActivity.Companion.cleanlinessStart
import com.example.ratemytoilet.MainActivity.Companion.isAdmin
import com.example.ratemytoilet.MainActivity.Companion.notRunFirstTime
import com.example.ratemytoilet.MainActivity.Companion.previousLocationsSize
import com.example.ratemytoilet.MainActivity.Companion.updateMap
import com.example.ratemytoilet.database.LocationViewModel
import com.example.ratemytoilet.database.ReviewCard
import com.example.ratemytoilet.database.ToiletUser.Companion.toToiletUser
import com.example.ratemytoilet.dialogs.FilterDialogFragment
import com.example.ratemytoilet.dialogs.LoadingDialogFragment
import com.example.ratemytoilet.listadapters.MapMarkerTooltipAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.ui.IconGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat

private const val TAG = "WashroomMapFragment"

/**
 * Fragment to show the map and all washrooms on the map
 *
 */
class WashroomMapFragment : Fragment(), OnMapReadyCallback, LocationListener {
    private var myLocationMarker : Marker?= null
    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView
    private var markerColor = IconGenerator.STYLE_PURPLE

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
    private lateinit var myClusterManager: ClusterManager<ReviewCard>
    private lateinit var loadingDialogFragment: LoadingDialogFragment
    private lateinit var locationPermissionResultReceiver: ActivityResultLauncher<String>


    /**
     * Override onCreate. Call initializer functions
     */
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

        locationViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]

        // Register for Receiver
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        filter.addAction(Intent.ACTION_PROVIDER_CHANGED)
        getActivity()?.registerReceiver(locationSwitchStateReceiver, filter)
    }

    /**
     * Override onCreateView. Set up map view, buttons and toolbar. Also observe for new locations added to map.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_washroom_map, container, false)

        setUpAdmin(view)
        mapView = view.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        locationPermissionResultReceiver.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        loadingDialogFragment = LoadingDialogFragment()

        if ((activity as AppCompatActivity).supportActionBar != null) {
            (activity as AppCompatActivity).supportActionBar?.hide();
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

        locationViewModel.locations.observe(viewLifecycleOwner) {
            if (previousLocationsSize == -1) {
                previousLocationsSize = it.size
            }
            if (previousLocationsSize != it.size) {
                Toast.makeText(activity, "New Location added", Toast.LENGTH_SHORT).show()
                previousLocationsSize = it.size
            }
        }
        return view
    }

    /**
     * Override onResume. If not first time loading map, reload map and washrooms.
     */
    override fun onResume() {
        super.onResume()
        mapView.onResume()

        if (notRunFirstTime) {
            if (updateMap) {
                Log.d("TAp", "true")
                if (this::mMap.isInitialized && mMap != null && myClusterManager != null) {
                    mMap.clear()
                    myClusterManager.clearItems()
                    loadWashrooms()
                    setClusterManager()
                }
            }
        } else {
            notRunFirstTime = true
        }
    }

    /**
     * Override onStart
     */
    override fun onStart() {
       super.onStart()
       mapView.onStart()
    }

    /**
     * Initialize Location Manager. Attempt to get user last known location and request for user location updates.
     */
    fun initLocationManager() {
        try {
            mapCentered = false
            Log.d("TAp", "running")
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
        } catch (e: SecurityException) {}
    }

    /**
     * Override onLocationChanged. Get user lat and lng, and set marker to user's current location. Center camera if first time loading map or location setting is turned on.
     */
    override fun onLocationChanged(location: Location) {
        if (myLocationMarker != null) {
            myLocationMarker!!.remove()
        }
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)
        if (!mapCentered) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
            mMap.animateCamera(cameraUpdate)
            mapCentered = true
        }

        markerOptions.position(latLng).title("ME").icon(
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
        )
        myLocationMarker = mMap.addMarker(markerOptions)!!
    }

    /**
     * Override onMapReady. Start clusterManger and google map
     */
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

    /**
     * Override onPause
     */
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    /**
     * Override onStop
     */
    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    /**
     * Override onDestroy. Unregister the locationSwitchStateReceiver and stop receiving user location updates from locationManager
     */
    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()

        if (this::locationManager.isInitialized && locationManager != null)
            locationManager.removeUpdates(this)

        // Unregister receiver
        getActivity()?.unregisterReceiver(locationSwitchStateReceiver)
    }

    /**
     * Override onSaveInstanceState
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    /**
     * Override onLowMemory
     */
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    /**
     * Cluster the washrooms on the main thread
     */
    private suspend fun setClusterOnMainThread(locationList : ArrayList<ReviewCard>) {
        withContext(Dispatchers.Main){
            var loadFragment = childFragmentManager.findFragmentByTag("Load")
            if (loadFragment != null) {
                loadingDialogFragment.dismiss()
            }
            myClusterManager.addItems(locationList)
            myClusterManager.cluster()
        }
    }

    /**
     * Load the washrooms into the map
     */
    private fun loadWashrooms() {
        val bubble = IconGenerator(context)
        bubble.setStyle(markerColor)
        if (loadingDialogFragment.dialog == null || !loadingDialogFragment.dialog?.isShowing!!) loadingDialogFragment.show(childFragmentManager, "Load")
        lifecycleScope.launch(Dispatchers.IO) {

            if (updateMap || locationViewModel.tempMarkers.value == null) {
                locationViewModel.processMapLocations(bubble)
            }
            var arr = locationViewModel.tempMarkers.value
            if (arr != null) {
                setClusterOnMainThread(arr)
            } else {
                loadingDialogFragment.dismiss()
            }
            updateMap = false
        }
    }

    /**
     * Save filter settings
     */
    fun filterConditionPassed(paperCheck: Boolean, soapCheck: Boolean, accessCheck: Boolean, maleCheck: Boolean, femaleCheck: Boolean, startValue: Float, endValue: Float) {
        mMap.clear()
        myClusterManager.clearItems()
        saveFilterConditions(paperCheck, soapCheck, accessCheck, maleCheck, femaleCheck, startValue, endValue)
        loadWashrooms()
        setClusterManager()
    }

    /**
     * Start the AddNewWashroomActivity when the add new location button is pressed by the user
     */
    private fun onAddNewLocationClick() {
        val viewIntent = Intent(activity, AddNewWashroomActivity::class.java)
        startActivity(viewIntent)
    }

    /**
     * Save filter conditions for switching between Map fragment and Washroom list fragment
     */
    private fun saveFilterConditions(paperCheck: Boolean, soapCheck: Boolean, accessCheck: Boolean, maleCheck: Boolean, femaleCheck: Boolean, startValue: Float, endValue: Float) {
        MainActivity.paperCheck = paperCheck
        MainActivity.soapCheck = soapCheck
        MainActivity.accessCheck = accessCheck
        MainActivity.maleCheck = maleCheck
        MainActivity.femaleCheck = femaleCheck
        cleanlinessStart = startValue
        cleanlinessEnd = endValue
    }


    override fun onLocationChanged(locations: MutableList<Location>) {
    }


    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }


    override fun onProviderEnabled(provider: String) {

    }


    override fun onProviderDisabled(provider: String) {

    }


    /**
     * Start the cluster manager in order to show washroom clusters on the map
     */
    private fun setClusterManager() {
        myClusterManager = ClusterManager<ReviewCard>(activity?.applicationContext , mMap)
        myClusterManager.renderer = context?.let { MarkerClusterRenderer(it, mMap, myClusterManager) }
        myClusterManager.markerCollection.setInfoWindowAdapter(context?.let { MapMarkerTooltipAdapter(it) })
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
                    val viewIntent = Intent(activity, WashroomDetailsActivity::class.java)
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

    /**
     * Detects if the location is turned on in the map fragment. If detected, it will start the location manager again with function initLocationManager().
     * Helps to solve the issue where the location is turned off in another fragment (ex. Washroom List Fragment), and the map view is re-opened with location still turned off.
     * This will detect if the location is turned back on in the map view and start up location manager to detect new locations. Otherwise, location will not be updated.
     */
    private val locationSwitchStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (LocationManager.PROVIDERS_CHANGED_ACTION == intent.action) {
                val locationManager =
                    context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                if (isGpsEnabled || isNetworkEnabled) {
                    // Location is enabled. Recenter map
                    mapCentered = false
                    initLocationManager()
                }
            }
        }
    }

    /**
     * Check if the user is an admin. If so, display the appropriate Admin title in map
     */
    private fun setUpAdmin(view: View) {
        var db = FirebaseFirestore.getInstance()
        var auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.document("users/${currentUser!!.uid}").addSnapshotListener { value, error ->
                if (value != null) {
                    val userDocument = value!!.toToiletUser()
                    Log.i(TAG, "Got user document: $userDocument")
                    if (userDocument != null) {
                        isAdmin = userDocument.isAdmin ?: false
                        println("debugk: $isAdmin")

                        var adminTitle = view.findViewById<TextView>(R.id.adminTitle)
                        if (isAdmin) {
                            adminTitle.visibility = View.VISIBLE
                            markerColor = IconGenerator.STYLE_RED
                        }
                    }
                }
            }
        }
    }
}
