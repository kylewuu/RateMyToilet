package com.example.ratemytoilet.database

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Location object for the Location collection on firebase.
 *
 * refs:
 * https://medium.com/firebase-developers/android-mvvm-firestore-37c3a8d65404
 */
data class Location (
    var id: String = "",
    var roomNumber: Int = 0,
    var gender: Int = 0,
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var date: Long = 0L,
    var name: String = ""
) {
    companion object {
        fun DocumentSnapshot.toLocation(): Location {
            var ret = Location()
            ret.id = id
            ret.roomNumber = getLong("roomNumber")?.toInt()!!
            ret.gender = getLong("gender")?.toInt()!!
            ret.lat = getDouble("lat")!!
            ret.lng = getDouble("lng")!!
            ret.date = getLong("date")!!
            ret.name = getString("name").toString()

            return ret
        }
    }

    fun toLocationMap(): HashMap<String, Any> {
        return hashMapOf(
            "roomNumber" to this.roomNumber,
            "gender" to this.gender,
            "lat" to this.lat,
            "lng" to this.lng,
            "date" to this.date,
            "name" to this.name
        )
    }

}