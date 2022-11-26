package com.example.ratemytoilet.database

import androidx.lifecycle.ViewModel

class ReviewViewModel: ViewModel() {
    suspend fun getReviewsForLocation(locationId: String): List<Review> {
        return ReviewRepository.getReviewsForLocation(locationId)
    }

    fun addReviewForLocation(review: Review) {
        ReviewRepository.addReviewForLocation(review)
    }
}