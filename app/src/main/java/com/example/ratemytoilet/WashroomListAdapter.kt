package com.example.ratemytoilet


import android.app.PendingIntent.getActivity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Criteria
import android.location.LocationListener
import android.location.LocationManager
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.ratemytoilet.database.Location
import com.example.ratemytoilet.database.Review
import com.example.ratemytoilet.database.ReviewViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat


// Based off class demo
class WashroomListAdapter(private val context: Context, private var locationList: List<Location>) : BaseAdapter(){


    // User location vars
    private lateinit var locationManager: LocationManager
    private var userLocation: android.location.Location? = null



    override fun getItem(position: Int): (Location) {
        return locationList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun getCount(): Int {
        return locationList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.layout_adapter, null)

        // Attempt to get User Location


        val washroomName = view.findViewById(R.id.title) as TextView
        val amenities = view.findViewById(R.id.amenities) as TextView
        val imageView = view.findViewById<View>(R.id.icon) as ImageView
        val ratingsText = view.findViewById<View>(R.id.tv_rating) as TextView

        val washroomInfo = locationList[position]
        washroomName.text = washroomInfo.name


        var reviewViewModel = ReviewViewModel()

        var totalRatingAmount = 0
        var averageCleanlinessRating = 0.0
        var paperTowelsValue = ""
        var soapValue = ""
        val decimalFormat = DecimalFormat("#.#")

        // Retrieve reviews for a location
        CoroutineScope(Dispatchers.IO).launch{

            var locationId = washroomInfo.id
            val genderInt = washroomInfo.gender

            var reviews = reviewViewModel.getReviewsForLocation(locationId)

            var arrayOfReviews = mutableListOf<Review>()


            // Get sum of cleanliness from each review
            for (review in reviews) {
                totalRatingAmount += review.cleanliness
                arrayOfReviews.add(review)
            }


            // Make sure there exists a review, otherwise divide by 0 error while calculating average cleanliness

            if(reviews.isNotEmpty()){


                // Calculate average rating
                averageCleanlinessRating = (totalRatingAmount.toDouble() / reviews.size.toDouble())


                // Sort reviews by timestamp in descending order
                arrayOfReviews.sortWith(compareByDescending{it.dateAdded})


                //TODO: Can I combine the two loops into one? Would it be faster?

                // Loop through reviews, and find most recent review with sufficientPaperTowels set to either 0 or 1 (No or Yes)
                var paperTowelIsUnknown = false
                for(latestReview in arrayOfReviews){
                    if(latestReview.sufficientPaperTowels == 0){
                        paperTowelsValue = " / No Paper Towels"
                        paperTowelIsUnknown = true

                        break
                    }
                    else if(latestReview.sufficientPaperTowels == 1){
                        paperTowelsValue = " / Paper Towels"
                        paperTowelIsUnknown = true

                        break
                    }
                }


                // If no review contained sufficientPaperTowels set to either 0 or 1 (No or Yes), then set to Unknown status
                if(!paperTowelIsUnknown){
                    paperTowelsValue = " / Unknown"
                }


                // Loop through reviews, and find most recent review with sufficientSoap set to either 0 or 1 (No or Yes)
                var soapIsUnknown = false
                for(latestReview in arrayOfReviews){
                    if(latestReview.sufficientSoap == 0){
                        soapValue = " / No Soap"
                        soapIsUnknown = true
                        break
                    }
                    else if(latestReview.sufficientSoap == 1){
                        soapValue = " / Soap"
                        soapIsUnknown = true
                        break
                    }
                }


                // If no review contained sufficientSoap set to either 0 or 1 (No or Yes), then set to Unknown status
                if(!soapIsUnknown){
                    soapValue = " / Unknown"
                }


            }
            else{
                // If no reviews exist, then set amenity values to Unknown
                paperTowelsValue = " / Unknown"
                soapValue = " / Unknown"
            }


            // Change the views. Cannot edit UI in coroutine
            GlobalScope.launch(Dispatchers.Main) {

                if(averageCleanlinessRating == 0.0){
                    ratingsText.text = averageCleanlinessRating.toInt().toString()
                }
                else{
                    var formattedCleanlinessRating = decimalFormat.format(averageCleanlinessRating).toString()
                    ratingsText.text = formattedCleanlinessRating
                }


                // Set gender and create amenities string
                if(genderInt == 0){
                    amenities.text = "Male" + paperTowelsValue + soapValue
                }
                else if(genderInt == 1){
                    amenities.text = "Female" + paperTowelsValue  + soapValue
                }
                else{
                    amenities.text = "Universal" + paperTowelsValue  + soapValue
                }
            }

        }


        // Get user location, and find distance relative to washroom location
        var distance = view.findViewById<TextView>(R.id.distance)
        val decimalFormatDistance = DecimalFormat("#.##")
        if(userLocation != null){
            var washroomLocation: android.location.Location? = android.location.Location("")
            washroomLocation?.latitude = washroomInfo.lat
            washroomLocation?.longitude = washroomInfo.lng

            // Get distance between user and washroom
            var distanceFromUserToWashroom = userLocation?.distanceTo(washroomLocation)
            if (distanceFromUserToWashroom != null) {

                // If less than a 1 km away, convert distance and show distance in Meters. Otherwise show in KM
                if (distanceFromUserToWashroom <= 1000){
                    distance.text = decimalFormatDistance.format(distanceFromUserToWashroom).toString() + " M"
                }
                else{
                    var distanceInKm = distanceFromUserToWashroom/1000
                    distance.text =  decimalFormatDistance.format(distanceInKm).toString() + " KM"
                }
            }

        }
        else{
            // Couldn't get user's location. Set distance to unknown.
            distance.text = "---"
        }


        // Set image for rating
        imageView.setImageResource(R.drawable.ellipse3)

        return view
    }


    fun replace(newCommentList: List<Location>) {
        locationList = newCommentList
    }


    fun replaceUserLocation(passedInUserLocation: android.location.Location?) {
        userLocation = passedInUserLocation
    }



}

