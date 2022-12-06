package com.example.ratemytoilet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ratemytoilet.MainActivity.Companion.isAdmin
import com.example.ratemytoilet.MainActivity.Companion.updateReviews
import com.example.ratemytoilet.NewReviewAdminFragment.Companion.ACCESSIBILITY_KEY
import com.example.ratemytoilet.NewReviewAdminFragment.Companion.LOCATION_ID_KEY
import com.example.ratemytoilet.database.ReviewViewModel
import com.example.ratemytoilet.database.UserComment
import com.example.ratemytoilet.listadapters.WashroomDetailsReviewAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * Activity to show the washroom details when a washroom is selected.
 */
class WashroomDetailsActivity : AppCompatActivity(), NewReviewAdminFragment.AdminReviewListener {
    private lateinit var userCommentList: ArrayList<UserComment>
    private lateinit var washroomId : String
    private lateinit var washroom : String
    private lateinit var gender : String
    private lateinit var date : String
    private lateinit var access : String
    private lateinit var commentList: ListView
    private lateinit var listAdapter: WashroomDetailsReviewAdapter
    private var accessibility = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        commentList = findViewById(R.id.commentListView)
        userCommentList = ArrayList()
        listAdapter = WashroomDetailsReviewAdapter(this, userCommentList)
        commentList.adapter = listAdapter
        washroomId = intent.getStringExtra("ID").toString()
        washroom = intent.getStringExtra("name").toString()
        gender = intent.getStringExtra("gender").toString()
        date = intent.getStringExtra("date").toString()
        access = intent.getStringExtra("access").toString()
        setData()
        if (isAdmin) title = "ADMIN - Washroom Reviews"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onResume() {
        if (updateReviews) {
            userCommentList.clear()
            setData()
            Log.d("TAb", "resume")
        }
        super.onResume()
    }

    private fun setData() {
        updateReviews = false

        val dataText = findViewById<TextView>(R.id.dateText)
        val rate = findViewById<RatingBar>(R.id.overallRate)
        val genderText = findViewById<TextView>(R.id.genderText)
        val paperText = findViewById<TextView>(R.id.paperCheckText)
        val soapText = findViewById<TextView>(R.id.soapCheckText)

        val addButton = findViewById<FloatingActionButton>(R.id.addButton)
        val washroomName = findViewById<TextView>(R.id.washroomNameText)
        val rateNumber = findViewById<TextView>(R.id.reviewNumberText)
        val mostRecentComment = findViewById<TextView>(R.id.mostRecentComment)
        washroomName.setText(washroom)
        genderText.setText(gender)
        dataText.setText(date)

        lifecycleScope.launch(Dispatchers.IO) {
            val reviewViewModel = ReviewViewModel()
            var allReviews = reviewViewModel.getReviewsForLocation(washroomId)
            allReviews = allReviews.sortedByDescending { it.dateAdded }

            if (allReviews.size != 0 && allReviews.size != userCommentList.size) {
                var rating = 0.0
                accessibility = allReviews[0].accessibility
                withContext(Main) {
                    launch {
                        rateNumber.setText("(" + allReviews.size + ")")

                        // Display most recent comment
                        if(allReviews[0].comment != ""){
                            mostRecentComment.text  = "Most Recent Comment: " + allReviews[0].comment
                        }
                    }
                }
                if (allReviews[0].sufficientPaperTowels == 0) {
                    lifecycleScope.launch(Main) {
                        paperText.setText("No")
                    }
                } else if (allReviews[0].sufficientPaperTowels == 1) {
                    lifecycleScope.launch(Main) {
                        paperText.setText("Yes")
                    }
                }
                else{
                    lifecycleScope.launch(Main) {
                        paperText.setText("Unknown")
                    }
                }

                if (allReviews[0].sufficientSoap == 0) {
                    lifecycleScope.launch(Main) {
                        soapText.setText("No")
                    }

                } else if (allReviews[0].sufficientSoap == 1) {
                    lifecycleScope.launch(Main) {
                        soapText.setText("Yes")
                    }
                }
                else{
                    lifecycleScope.launch(Main) {
                        soapText.setText("Unknown")
                    }
                }

                for (review in allReviews) {
                    rating += review.cleanliness

                    val dateTimeFormat : DateFormat = SimpleDateFormat ("MMM dd, yyyy")
                    date = dateTimeFormat.format(review.dateAdded)

                    val user = UserComment(review.id,date, review.cleanliness.toFloat(), review.comment, review.sufficientSoap, review.sufficientPaperTowels, review.accessibility, review.leftByAdmin)
                    withContext(Main) {
                        launch {
                            userCommentList.add(user)
                        }
                    }
                }
                withContext(Main) {
                    launch {
                        listAdapter.update(userCommentList)
                        listAdapter.notifyDataSetChanged()
                    }
                }
                rating /= allReviews.size
                withContext(Main) {
                    rate.setRating(rating.toFloat())
                }
            }
        }

        addButton.setOnClickListener {
            if (!isAdmin) {
                val intent = Intent(this, NewReviewActivity::class.java)
                intent.putExtra("location", washroomId)
                startActivity(intent)
            } else {
                val bundle = Bundle()
                bundle.putString(LOCATION_ID_KEY,washroomId)
                bundle.putInt(ACCESSIBILITY_KEY, accessibility)
                val adminDialogFragment = NewReviewAdminFragment()
                adminDialogFragment.arguments = bundle
                adminDialogFragment.show(supportFragmentManager, "Admin")
            }
        }
    }

    override fun loadReviews() {
        onResume()
    }
}
