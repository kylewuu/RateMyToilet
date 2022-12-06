package com.example.ratemytoilet.database

import androidx.lifecycle.ViewModel

/**
 * View model for managing reviews.
 */
class ReviewViewModel: ViewModel() {
    /**
     * Gets all reviews for a location using repository
     */
    suspend fun getReviewsForLocation(locationId: String): List<Review> {
        return ReviewRepository.getReviewsForLocation(locationId)
    }

    /**
     * Adds a review for a location using repository
     */
    fun addReviewForLocation(review: Review) {
        ReviewRepository.addReviewForLocation(review)
    }
}
