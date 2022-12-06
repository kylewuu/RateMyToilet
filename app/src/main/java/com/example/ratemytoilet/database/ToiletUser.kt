package com.example.ratemytoilet.database

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Data object for users.
 */
data class ToiletUser(var phoneNumber: String? = "", var totalReviews: Int? = 0, @DocumentId var reviews: String? = "", var isAdmin: Boolean? = false) {
    companion object {
        fun DocumentSnapshot.toToiletUser(): ToiletUser {
            var ret = ToiletUser()
            ret.phoneNumber = getString("phoneNumber")!!
            ret.totalReviews = getLong("totalReviews")?.toInt() ?: 0
            ret.reviews = getString("reviews") ?: ""
            ret.isAdmin = getBoolean("isAdmin") ?: false

            return ret
        }
    }
}