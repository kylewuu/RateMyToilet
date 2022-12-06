package com.example.ratemytoilet

import android.os.Bundle
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ratemytoilet.MainActivity.Companion.updateList
import com.example.ratemytoilet.MainActivity.Companion.updateMap
import com.example.ratemytoilet.MainActivity.Companion.updateReviews
import com.example.ratemytoilet.database.Review
import com.example.ratemytoilet.database.ReviewViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

/**
 * Activity for writing a new review
 */
class NewReviewActivity : AppCompatActivity() {
    private var rating = 0.0f
    private var paperResult = 1
    private var soapResult = 1
    private var accResult = 1
    private var comment = ""
    private lateinit var review : Review
    private var washroomId : String ?= null

    /**
     * Creates a new review activity. Sets the save and cancel button listeners
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_review_normal)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        washroomId = intent.getStringExtra("location").toString()
        review = Review()
        val save = findViewById<FloatingActionButton>(R.id.saveNormalReview)
        val cancel = findViewById<FloatingActionButton>(R.id.cancelNormalReview)
        updateMap = false
        save.setOnClickListener {
            getData()
            if (rating <= 0.0) {
                Toast.makeText(this, "The minimum rating is 1 star.", Toast.LENGTH_SHORT).show()
            } else {
                setData()
                updateMap = true
                updateList = true
                updateReviews = true
                finish()
            }
        }
        cancel.setOnClickListener {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /**
     * Retrieves all of user inputted data
     */
    private fun getData() {
        val ratingBar = findViewById<RatingBar>(R.id.normalUserRating)
        rating = ratingBar.rating

        val paperGroup = findViewById<ChipGroup>(R.id.toiletGroup)
        val paperChipT = paperGroup.getChildAt(0) as Chip
        val paperChipF = paperGroup.getChildAt(1) as Chip
        if (paperChipF.isChecked) {
            paperResult = 0
        }
        if (!paperChipT.isChecked && !paperChipF.isChecked) {
            paperResult = 2
        }

        val soapGroup = findViewById<ChipGroup>(R.id.soapGroup)
        val soapChipT = soapGroup.getChildAt(0) as Chip
        val soapChipF = soapGroup.getChildAt(1) as Chip
        if (soapChipF.isChecked) {
            soapResult = 0
        }
        if (!soapChipT.isChecked && !soapChipF.isChecked) {
            soapResult = 2
        }

        val accGroup = findViewById<ChipGroup>(R.id.accessGroup)
        val accChipT = accGroup.getChildAt(0) as Chip
        val accChipF = accGroup.getChildAt(1) as Chip
        if (accChipF.isChecked) {
            accResult = 0
        }
        if (!accChipT.isChecked && !accChipF.isChecked) {
            accResult = 2
        }

        val commentText = findViewById<EditText>(R.id.normalReviewText)
        comment = commentText.text.toString()
    }

    /**
     * When data is saved, write it to the firestore db
     */
    private fun setData() {
        review.locationId = washroomId.toString()
        review.leftByAdmin = false
        review.dateAdded = Calendar.getInstance().timeInMillis
        review.cleanliness = rating.toInt()
        review.sufficientSoap = soapResult
        review.sufficientPaperTowels = paperResult
        review.accessibility = accResult
        review.comment = comment
        var reviewViewModel = ReviewViewModel()
        reviewViewModel.addReviewForLocation(review)
    }
}
