package com.example.ratemytoilet.database

import com.example.ratemytoilet.database.Review.Companion.toReview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await


/**
 * Repository for review view model and review object. Retrieves and writes data
 * for review collect in firestore.
 *
 * refs:
 * https://stackoverflow.com/questions/49592124/android-firestore-querying-and-returning-a-custom-object
 * https://firebase.google.com/docs/firestore/query-data/queries#kotlin+ktx_1
 */
class ReviewRepository {
    companion object {
        private var firestore = Firebase.firestore
        private var reviewCollection = firestore.collection("review")
        private var userCollection = firestore.collection("users")

        suspend fun getReviewsForLocation(locationId: String): List<Review> {
            return reviewCollection.whereEqualTo("locationId", locationId).get().await().documents.map { it.toReview() }
        }

        suspend fun getReviewsForUserId(userId: String): List<Review> {
            return firestore.collection("users/${userId}/reviews").get().await().documents.map { it.toReview() }
        }

        fun addReviewForLocation(review: Review) {
            val mappedReview = review.toReviewMap()
            reviewCollection.add(mappedReview)
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val userDocument = userCollection.document("${currentUser!!.uid}")
                userDocument.update("totalReviews", FieldValue.increment(1))
                userDocument.collection("reviews").add(mappedReview)
            }
        }
    }
}
