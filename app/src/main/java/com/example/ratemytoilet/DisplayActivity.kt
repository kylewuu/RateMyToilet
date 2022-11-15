package com.example.ratemytoilet

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DisplayActivity : AppCompatActivity() {
    private lateinit var userCommentList: ArrayList<UserComment>
    private lateinit var commentList: ListView
    private lateinit var listAdapter: UserCommentListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)
        commentList = findViewById<ListView>(R.id.commentListView)
        userCommentList = ArrayList()
        listAdapter = UserCommentListAdapter(applicationContext, userCommentList)
        commentList.adapter = listAdapter
        setData()
    }

    fun setData() {
        val dataText = findViewById<TextView>(R.id.dateText)
        val rate = findViewById<RatingBar>(R.id.overallRate)
        val genderText = findViewById<TextView>(R.id.genderText)
        val paperText = findViewById<TextView>(R.id.paperCheckText)
        val soapText = findViewById<TextView>(R.id.soapCheckText)

        val editButton = findViewById<ImageButton>(R.id.editButton)
        val washroomName = findViewById<TextView>(R.id.washroomNameText)
        val rateNumber = findViewById<TextView>(R.id.reviewNumberText)

        val user1 = UserComment("TollerUser#123", "Nov 9, 2022", 4.3f, "Very clean washroom would totally come back again", 0L)
        val user2 = UserComment("TollerUser#123", "Nov 9, 2022", 3.4f, "Very clean washroom would totally come back again", 0L)
        val user3 = UserComment("TollerUser#123", "Nov 9, 2022", 4.5f, "Very clean washroom would totally come back again", 0L)
        val user4 = UserComment("TollerUser#123", "Nov 9, 2022", 4.5f, "Very clean washroom would totally come back again", 0L)
        val user5 = UserComment("TollerUser#123", "Nov 9, 2022", 4.5f, "Very clean washroom would totally come back again", 0L)
        val user6 = UserComment("TollerUser#123", "Nov 9, 2022", 4.5f, "Very clean washroom would totally come back again", 0L)
        userCommentList.add(user1)
        userCommentList.add(user2)
        userCommentList.add(user3)
        userCommentList.add(user4)
        userCommentList.add(user5)
        userCommentList.add(user6)

        washroomName.setText("AQ Washroom")
        rateNumber.setText("(6)")
        genderText.setText("Male")
        paperText.setText("Yes")
        soapText.setText("Yes")
        dataText.setText("Nov.7, 2022")
        rate.setRating(4.7f)

    }
}