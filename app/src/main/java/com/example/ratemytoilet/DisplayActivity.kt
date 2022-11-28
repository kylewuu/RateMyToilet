package com.example.ratemytoilet

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ratemytoilet.database.ReviewViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat

class DisplayActivity : AppCompatActivity() {
    private lateinit var userCommentList: ArrayList<UserComment>
    private lateinit var washroomId : String
    private lateinit var washroom : String
    private lateinit var gender : String
    private lateinit var date : String
    private lateinit var commentList: ListView
    private lateinit var listAdapter: UserCommentListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        commentList = findViewById(R.id.commentListView)
        userCommentList = ArrayList()
        listAdapter = UserCommentListAdapter(applicationContext, userCommentList)
        commentList.adapter = listAdapter
        washroomId = intent.getStringExtra("ID").toString()
        washroom = intent.getStringExtra("name").toString()
        gender = intent.getStringExtra("gender").toString()
        date = intent.getStringExtra("date").toString()

        setData()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun setData() {
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
        rateNumber.setText("(0)")
        paperText.setText("Yes")
        soapText.setText("Yes")
        rate.setRating(0.0f)

        CoroutineScope(Dispatchers.IO).launch {
            val reviewViewModel = ReviewViewModel()
            val allReviews = reviewViewModel.getReviewsForLocation(washroomId)
            allReviews.sortedByDescending { it.dateAdded }
            if (allReviews.size != 0) {
                var rating = 0.0
                CoroutineScope(Main).launch {
                    rateNumber.setText("(" + allReviews.size + ")")

                    // Display most recent comment
                    mostRecentComment.text = allReviews[0].comment
                }
                if (allReviews[0].sufficientPaperTowels == 0) {
                    CoroutineScope(Main).launch {
                        paperText.setText("No")
                    }
                } else if (allReviews[0].sufficientPaperTowels == 2) {
                    CoroutineScope(Main).launch {
                        paperText.setText("Unknown")
                    }

                }
                if (allReviews[0].sufficientSoap == 0) {
                    CoroutineScope(Main).launch {
                        soapText.setText("No")
                    }

                } else if (allReviews[0].sufficientSoap == 2) {
                    CoroutineScope(Main).launch {
                        paperText.setText("Unknown")
                    }
                }
                for (review in allReviews) {
                    rating += review.cleanliness
                    val dateTimeFormat : DateFormat = SimpleDateFormat ("MMM.dd.yyyy")
                    date = dateTimeFormat.format(review.dateAdded)
                    val user = UserComment(review.id,date, rating.toFloat(), review.comment)
                    userCommentList.add(user)
                    listAdapter.update(userCommentList)
                    CoroutineScope(Main).launch {
                        listAdapter.notifyDataSetChanged()
                    }
                }
                rating /= allReviews.size
                CoroutineScope(Main).launch {
                    rate.setRating(rating.toFloat())
                }
            }
        }

        addButton.setOnClickListener {
            val admin = true
            if (!admin) {
                val intent = Intent(this, NormalUserAddReviewActivity::class.java)
                intent.putExtra("location", washroomId)
                startActivity(intent)
            } else {
                val bundle = Bundle()
                bundle.putString("message",washroomId);
                val filterDialogFragment = AdminFragment()
                filterDialogFragment.arguments = bundle
                filterDialogFragment.show(supportFragmentManager, "Admin")
            }
        }

    }
}