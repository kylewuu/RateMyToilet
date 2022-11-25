package com.example.ratemytoilet.database

import com.example.ratemytoilet.database.Location.Companion.toLocation
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * refs:
 * https://medium.com/firebase-developers/android-mvvm-firestore-37c3a8d65404
 */
class LocationRepository {

    companion object {
        private var firestore = Firebase.firestore
        private var collection = firestore.collection("location")

        fun getLocationById() {
            CoroutineScope(IO).launch {
                var location = collection.document("KrsezdPuMUVlMxezoYxc").get().await().toLocation()

                println("debugk: ${location.name}")
            }
        }

        fun getAllLocations() {
            CoroutineScope(IO).launch {
                var allLocations: List<Location> = collection.get().await().documents.map({it.toLocation()})

                println("debugk: ${allLocations.size}")
            }
        }

        /** TODO move this comment to the viewmodel
        example usage:
        var newLocation = com.example.ratemytoilet.database.Location()
        newLocation.roomNumber = 1234
        newLocation.gender = 1
        newLocation.lat = 123.456
        newLocation.lng = 123.123
        newLocation.date = Calendar.getInstance().timeInMillis
        newLocation.name = "Second washroom"
        addLocation(newLocation)
         */
        fun addLocation(newLocation: Location) {
            collection.add(newLocation.toMap())
        }
    }
}