package com.example.ratemytoilet.database

import com.example.ratemytoilet.database.Review.Companion.toReview
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await


/**
 * refs:
 * https://stackoverflow.com/questions/49592124/android-firestore-querying-and-returning-a-custom-object
 * https://firebase.google.com/docs/firestore/query-data/queries#kotlin+ktx_1
 */
class ReviewRepository {

    companion object {
        private var firestore = Firebase.firestore
        private var collection = firestore.collection("review")

        suspend fun getReviewsForLocation(locationId: String): List<Review> {
            return collection.whereEqualTo("locationId", locationId).get().await().documents.map { it.toReview() }
        }

        fun addReviewForLocation(review: Review) {
            collection.add(review.toReviewMap())
        }
    }
}