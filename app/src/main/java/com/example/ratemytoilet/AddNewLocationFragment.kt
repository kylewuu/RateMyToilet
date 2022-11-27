package com.example.ratemytoilet

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.ratemytoilet.database.Location
import com.example.ratemytoilet.database.LocationViewModel
import com.example.ratemytoilet.database.Review
import com.example.ratemytoilet.database.ReviewViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AddNewLocationFragment : AppCompatActivity() {

    private lateinit var ratingBar: RatingBar
    private lateinit var roomNumber: EditText
    private lateinit var washroomName: EditText
    private var gender: Int? = 3
    private var addLocationLatLng : LatLng? = null


    val genderArray = arrayOf("Male", "Female", "Universal")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_add_new_location)


        val genderSelected = findViewById<TextView>(R.id.tv_genderSelected)
        val spinner = findViewById<Spinner>(R.id.sp_gender)
        spinner?.adapter = ArrayAdapter(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, genderArray ) as SpinnerAdapter
        //spinner.prompt = "Select Gender"
        spinner?.onItemSelectedListener = object:AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                gender = p2
                genderSelected.text = genderArray[p2]
                /*
                if (inputTypeResult != null) {
                    inputTypeResult.text = inputTypeArray[p2]   // Set the textView text with the chosen element

                }

                 */
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //Do nothing, compiler complains if not implemented
            }

        }
    }


    fun onAddLocationClick(view: View){
        val viewIntent = Intent(this, AddLocationMapActivity::class.java)
        startActivityForResult(viewIntent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                val message = data!!.getParcelableExtra<LatLng>("LATLNG_KEY")
                if (message != null) {
                    addLocationLatLng = message
                    println("debug" + addLocationLatLng)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun onAddNewWashroomClick(view: View){
        ratingBar = findViewById(R.id.rb_ratingBar)
        roomNumber = findViewById(R.id.et_roomNumber)
        washroomName = findViewById(R.id.et_washroomName)


        val roomNum = roomNumber.text.toString()
        val roomName = washroomName.text.toString()
        val rating = ratingBar.rating.toDouble().toInt()
        println("Rating" + rating)



        if(roomNum != "" && roomName != ""  && addLocationLatLng != null)
        {
            /*
            var locationViewModel = LocationViewModel()

            var newLocation = Location()
            newLocation.roomNumber = roomNum.toInt()
            newLocation.gender = gender!!
            newLocation.lat = addLocationLatLng!!.latitude
            newLocation.lng = addLocationLatLng!!.longitude
            newLocation.date = Calendar.getInstance().timeInMillis
            newLocation.name = roomName
            //locationViewModel.addLocation(newLocation)

             */

            CoroutineScope(Dispatchers.IO).launch {
                var locationViewModel = LocationViewModel()
                var newLocation = Location()

                newLocation.roomNumber = roomNum.toInt()
                newLocation.gender = gender!!
                newLocation.lat = addLocationLatLng!!.latitude
                newLocation.lng = addLocationLatLng!!.longitude
                newLocation.date = Calendar.getInstance().timeInMillis
                newLocation.name = roomName
                locationViewModel.addLocation(newLocation).collect {
                    /**
                     * "it" here is the documentId of the newly added location. It is the same
                     * as the locatoinId found in ReviewDb. This can be used to attach a new
                     * review or to fetch the new location as soon as it is added.
                     */
                    var newReview = Review()
                    newReview.locationId = it
                    newReview.leftByAdmin = false
                    newReview.cleanliness = rating
                    newReview.dateAdded = Calendar.getInstance().timeInMillis
                    newReview.sufficientPaperTowels = 0
                    newReview.sufficientSoap = 0
                    newReview.accessibility = 0
                    newReview.comment = ""

                    var reviewViewModel = ReviewViewModel()
                    reviewViewModel.addReviewForLocation(newReview)

                }
            }


            //TODO: Add Review. Don't know how to get last added location.
            /*
            CoroutineScope(Dispatchers.IO).launch {
                var allLocations = locationViewModel.getAllLocations()


                var lastAddedLocationID = allLocations[allLocations.size-1]
                println("Location:"+lastAddedLocationID.toString())
                var newReview = Review()
                newReview.locationId = lastAddedLocationID.toString()
                newReview.leftByAdmin = false
                newReview.cleanliness = 5
                newReview.dateAdded = Calendar.getInstance().timeInMillis
                newReview.sufficientPaperTowels = 1
                newReview.sufficientSoap = 2
                newReview.accessibility = 0
                newReview.comment = ""

                var reviewViewModel = ReviewViewModel()
                //reviewViewModel.addReviewForLocation(newReview)

            }

             */
            finish()
        }
        else if(addLocationLatLng == null){
            // Show toast indicating a parameter is missing
            val toast = Toast.makeText(applicationContext, "Missing Location", Toast.LENGTH_SHORT)
            toast.show()
        }
        else if(roomNum == ""){
            // Show toast indicating a parameter is missing
            val toast = Toast.makeText(applicationContext, "Missing Room Number", Toast.LENGTH_SHORT)
            toast.show()
        }
        else if(roomName == ""){
            // Show toast indicating a parameter is missing
            val toast = Toast.makeText(applicationContext, "Missing Washroom Name", Toast.LENGTH_SHORT)
            toast.show()
        }








    }



}