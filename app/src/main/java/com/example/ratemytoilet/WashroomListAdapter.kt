package com.example.ratemytoilet


import android.content.Context
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



// Based off class demo
class WashroomListAdapter(private val context: Context, private var locationList: List<Location>) : BaseAdapter() {


    // Returns the values associated with a key in the database
    override fun getItem(position: Int): (Array<String>) {
        return arrayOf(
            locationList.get(position).id

        )

    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun getCount(): Int {
        return locationList.size
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.layout_adapter, null)

        val washroomName = view.findViewById(R.id.title) as TextView
        val amenities = view.findViewById(R.id.amenities) as TextView
        val imageView = view.findViewById<View>(R.id.icon) as ImageView
        val ratingsText = view.findViewById<View>(R.id.tv_rating) as TextView


        washroomName.text = locationList[position].name


        var reviewViewModel = ReviewViewModel()

        var totalRatingAmount = 0
        var averageCleanlinessRating = 0.0
        var paperTowelsValue = ""
        var soapValue = ""


        // Get average cleanliness rating
        CoroutineScope(Dispatchers.IO).launch{
            var locationId = locationList[position].id
            val genderInt = locationList[position].gender
            var reviews = reviewViewModel.getReviewsForLocation(locationId)


            var arrayOfReviews = mutableListOf<Review>()


            // Get sum of cleanliness from each review
            for (review in reviews) {
                totalRatingAmount += review.cleanliness
                arrayOfReviews.add(review)
            }


            // Make sure there exists a review, otherwise divide by 0 error
            if(reviews.isNotEmpty()){


                // Calculate average rating
                averageCleanlinessRating = (totalRatingAmount.toDouble() / reviews.size.toDouble())


                arrayOfReviews.sortWith(compareByDescending{it.dateAdded})
                for(latestReview in arrayOfReviews){
                    //println("Review: " + latestReview +" : " + latestReview.dateAdded)
                    if(latestReview.sufficientPaperTowels == 0){
                        paperTowelsValue = " / No Paper Towels"
                        break
                    }
                    else if(latestReview.sufficientPaperTowels == 1){
                        paperTowelsValue = " / Paper Towels"
                        break
                    }
                }

                for(latestReview in arrayOfReviews){
                    //println("Review: " + latestReview +" : " + latestReview.dateAdded)
                    if(latestReview.sufficientSoap == 0){
                        soapValue = " / No Soap"
                        break
                    }
                    else if(latestReview.sufficientSoap == 1){
                        soapValue = " / Soap"
                        break
                    }
                }


            }


            // Change the views
            GlobalScope.launch(Dispatchers.Main) {
                if(averageCleanlinessRating == 0.0){
                    ratingsText.text = averageCleanlinessRating.toInt().toString()
                }
                else{
                    ratingsText.text = averageCleanlinessRating.toString()
                }



                // Set gender
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


        // Set image for rating
        imageView.setImageResource(R.drawable.ellipse3)


        return view
    }


    fun replace(newCommentList: List<Location>) {
        locationList = newCommentList
    }
}

