package com.example.ratemytoilet.database

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Review object for washroom reviews for the review collection on Firebase.
 */
data class Review (
    var id: String = "",
    var locationId: String = "",
    var leftByAdmin: Boolean = false,
    var cleanliness: Int = 0,
    var dateAdded: Long = 0L,
    var sufficientPaperTowels: Int = 0,
    var sufficientSoap: Int = 0,
    var accessibility: Int = 0,
    var comment: String = ""
) {
    companion object {
        fun DocumentSnapshot.toReview(): Review {
            var ret = Review()
            ret.id = id
            ret.locationId = getString("locationId")!!
            ret.leftByAdmin = getBoolean("leftByAdmin")!!
            ret.cleanliness = getLong("cleanliness")?.toInt()!!
            ret.dateAdded = getLong("dateAdded")!!
            ret.sufficientPaperTowels = getLong("sufficientPaperTowels")?.toInt()!!
            ret.sufficientSoap = getLong("sufficientSoap")?.toInt()!!
            ret.accessibility = getLong("accessibility")?.toInt()!!
            ret.comment = getString("comment").toString()

            return ret
        }
    }

    fun toReviewMap(): HashMap<String, Any> {
        return hashMapOf(
            "locationId" to this.locationId,
            "leftByAdmin" to this.leftByAdmin,
            "cleanliness" to this.cleanliness,
            "dateAdded" to this.dateAdded,
            "sufficientPaperTowels" to sufficientPaperTowels,
            "sufficientSoap" to sufficientSoap,
            "accessibility" to accessibility,
            "comment" to this.comment
        )
    }
}
