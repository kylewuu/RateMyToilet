package com.example.ratemytoilet

import androidx.lifecycle.ViewModel
import com.example.ratemytoilet.database.Review
import com.example.ratemytoilet.database.ReviewRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat

class ProfileViewModel: ViewModel() {
    var reviews = arrayListOf<UserComment>()
        private set

    suspend fun getReviewsForUserId(userId: String) {
        reviews.clear()
        val retrievedReviews = ReviewRepository.getReviewsForUserId(userId)
        for (review in retrievedReviews) {
            val dateTimeFormat : DateFormat = SimpleDateFormat ("MMM dd, yyyy")
            val date = dateTimeFormat.format(review.dateAdded)

            val userComment = UserComment(review.id,date, review.cleanliness.toFloat(), review.comment)
            reviews.add(userComment)
        }
    }
}