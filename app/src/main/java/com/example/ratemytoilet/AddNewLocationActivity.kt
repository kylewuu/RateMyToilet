package com.example.ratemytoilet

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ratemytoilet.MainActivity.Companion.isAdmin
import com.example.ratemytoilet.MainActivity.Companion.updateMap
import com.example.ratemytoilet.database.Location
import com.example.ratemytoilet.database.LocationViewModel
import com.example.ratemytoilet.database.Review
import com.example.ratemytoilet.database.ReviewViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AddNewLocationActivity : AppCompatActivity() {

    // Views
    private lateinit var ratingBar: RatingBar
    private lateinit var roomNumber: EditText
    private lateinit var washroomName: EditText
    private lateinit var paperTowelButtonYes: Button
    private lateinit var paperTowelButtonNo: Button
    private lateinit var soapButtonYes: Button
    private lateinit var soapButtonNo: Button
    private lateinit var accessButtonYes: Button
    private lateinit var accessButtonNo: Button
    private lateinit var commentBox :EditText

    // Vars to save
    private var gender: Int? = 0
    private var addLocationLatLng : LatLng? = null
    private var paperTowelValue: Int = 2
    private var soapValue: Int = 2
    private var accessValue: Int = 2

    // Array of genders
    val genderArray = arrayOf("Male", "Female", "Universal")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_location)

        if (isAdmin) title = "ADMIN - " + getString(R.string.title_activity_add_location_map)

        // Set views
        paperTowelButtonYes = findViewById<Button>(R.id.bt_paperTowelsYes)
        paperTowelButtonNo = findViewById<Button>(R.id.bt_paperTowelsNo)
        soapButtonYes = findViewById<Button>(R.id.bt_soapYes)
        soapButtonNo = findViewById<Button>(R.id.bt_soapNo)
        accessButtonYes = findViewById<Button>(R.id.bt_accessYes)
        accessButtonNo = findViewById<Button>(R.id.bt_accessNo)
        commentBox = findViewById<EditText>(R.id.et_comment)

        // Setup spinner for Gender
        val spinner = findViewById<Spinner>(R.id.sp_gender)
        spinner?.adapter = ArrayAdapter(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, genderArray ) as SpinnerAdapter

        // Update textView with selected gender
        spinner?.onItemSelectedListener = object:AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                gender = p2
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                //Do nothing
            }
        }
        updateMap = false
    }



    // Start the AddLocationMapActivity. User chooses new washroom's location
    fun onAddLocationClick(view: View){
        val viewIntent = Intent(this, AddLocationMapActivity::class.java)
        startActivityForResult(viewIntent, 0)
    }



    // Retrieve lat and lng coordinates from the user selected location, from AddLocationMapActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                val message = data!!.getParcelableExtra<LatLng>("LATLNG_KEY")
                if (message != null) {
                    addLocationLatLng = message
                    //println("debug" + addLocationLatLng)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }



    // On finish button clicked
    fun onAddNewWashroomClick(view: View){

        // Get views
        ratingBar = findViewById(R.id.rb_ratingBar)
        roomNumber = findViewById(R.id.et_roomNumber)
        washroomName = findViewById(R.id.et_washroomName)


        // Get values in views
        val roomNum = roomNumber.text.toString()
        val roomName = washroomName.text.toString()
        val rating = ratingBar.rating.toDouble().toInt()
        //println("Rating" + rating)


        // Room Number, Room Name, and Location cannot be empty
        if(addLocationLatLng == null){
            // Show toast indicating location parameter is missing
            val toast = Toast.makeText(applicationContext, "Missing Location", Toast.LENGTH_SHORT)
            toast.show()
        }
        else if(roomNum == ""){
            // Show toast indicating room number parameter is missing
            val toast = Toast.makeText(applicationContext, "Missing Room Number", Toast.LENGTH_SHORT)
            toast.show()
        }
        else if(roomName == ""){
            // Show toast indicating room name parameter is missing
            val toast = Toast.makeText(applicationContext, "Missing Washroom Name", Toast.LENGTH_SHORT)
            toast.show()
        } else if (rating <= 0.0) {
            Toast.makeText(this, "The minimum rating is 1 star.", Toast.LENGTH_SHORT).show()
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                var locationViewModel = LocationViewModel()
                var newLocation = Location()

                // Add new location to database
                newLocation.roomNumber = roomNum.toInt()
                newLocation.gender = gender!!
                newLocation.lat = addLocationLatLng!!.latitude
                newLocation.lng = addLocationLatLng!!.longitude
                newLocation.date = Calendar.getInstance().timeInMillis
                newLocation.name = roomName
                locationViewModel.addLocation(newLocation).collect {

                    // Add new review to newly created location
                    var newReview = Review()
                    newReview.locationId = it
                    newReview.leftByAdmin = isAdmin
                    newReview.cleanliness = rating
                    Log.d("TAo", rating.toString())
                    newReview.dateAdded = Calendar.getInstance().timeInMillis
                    newReview.sufficientPaperTowels = paperTowelValue
                    newReview.sufficientSoap = soapValue
                    newReview.accessibility = accessValue
                    newReview.comment = commentBox.text.toString()

                    var reviewViewModel = ReviewViewModel()
                    reviewViewModel.addReviewForLocation(newReview)

                }

            }
            updateMap = true
            finish()
        }
    }

    fun onPaperTowelYesClick(view:View){
        paperTowelValue = 1

        paperTowelButtonYes.setBackgroundColor(Color.parseColor("#9754CB"))
        paperTowelButtonYes.setTextColor(Color.WHITE)


        paperTowelButtonNo.setBackgroundColor(Color.WHITE)
        paperTowelButtonNo.setTextColor(Color.parseColor("#B6B6B6"))

    }

    fun onPaperTowelNoClick(view:View){
        paperTowelValue = 0

        paperTowelButtonYes.setBackgroundColor(Color.WHITE)
        paperTowelButtonYes.setTextColor(Color.parseColor("#B6B6B6"))

        paperTowelButtonNo.setBackgroundColor(Color.parseColor("#9754CB"))
        paperTowelButtonNo.setTextColor(Color.WHITE)

    }



    fun onSoapYesClick(view:View){
        soapValue = 1

        soapButtonYes.setBackgroundColor(Color.parseColor("#9754CB"))
        soapButtonYes.setTextColor(Color.WHITE)


        soapButtonNo.setBackgroundColor(Color.WHITE)
        soapButtonNo.setTextColor(Color.parseColor("#B6B6B6"))
    }



    fun onSoapNoClick(view:View){
        soapValue = 0

        soapButtonYes.setBackgroundColor(Color.WHITE)
        soapButtonYes.setTextColor(Color.parseColor("#B6B6B6"))

        soapButtonNo.setBackgroundColor(Color.parseColor("#9754CB"))
        soapButtonNo.setTextColor(Color.WHITE)
    }


    fun onAccessYesClick(view:View){
        accessValue = 1

        accessButtonYes.setBackgroundColor(Color.parseColor("#9754CB"))
        accessButtonYes.setTextColor(Color.WHITE)


        accessButtonNo.setBackgroundColor(Color.WHITE)
        accessButtonNo.setTextColor(Color.parseColor("#B6B6B6"))
    }



    fun onAccessNoClick(view:View){
        accessValue = 0

        accessButtonYes.setBackgroundColor(Color.WHITE)
        accessButtonYes.setTextColor(Color.parseColor("#B6B6B6"))

        accessButtonNo.setBackgroundColor(Color.parseColor("#9754CB"))
        accessButtonNo.setTextColor(Color.WHITE)
    }

}