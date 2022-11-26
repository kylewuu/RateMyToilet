package com.example.ratemytoilet

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class NormalUserAddReviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_review_normal)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}