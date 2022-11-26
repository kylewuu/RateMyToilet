package com.example.ratemytoilet.database

import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * Examples for using firestore functions.
 */
class DatabaseUsageExamples {

    companion object {
        /**
         * Sets the location viewmodel observer. Prints out the size of
         * the locations. Function inside observe will be called every time
         * locations is updated.
         */
        fun initializeLocationViewModel(owner: LifecycleOwner) {
            var locationsViewModel = LocationViewModel()
            locationsViewModel.locations.observe(owner) {
                println("debugk: ${it.size}")
            }
        }

        /**
         * Manually retrieves all the locations without using the viewmodel.
         * Needs to be called in a coroutine.
         */
        fun getAllLocations() {
            var locationViewModel = LocationViewModel()
            CoroutineScope(Dispatchers.IO).launch {
                var allLocations = locationViewModel.getAllLocations()
                for (location in allLocations) {
                    println("debugk: $location")
                }
            }
        }

        /**
         * Example for adding a new location to the firestore. The locations
         * observer found in `initializeLocationViewModel()` will fire if new location
         * is added
         */
        fun addNewLocation() {
            var locationViewModel = LocationViewModel()

            var newLocation = Location()
            newLocation.roomNumber = 789
            newLocation.gender = 1
            newLocation.lat = 123.456
            newLocation.lng = 123.123
            newLocation.date = Calendar.getInstance().timeInMillis
            newLocation.name = "Second washroom"
            locationViewModel.addLocation(newLocation)
        }

        /**
         * Example for retrieving all reviews for a given location. Needs
         * to be called inside a coroutine. The location id can be retrieved
         * from Location.
         */
        fun getReviewsForLocation() {
            var reviewViewModel = ReviewViewModel()

            CoroutineScope(Dispatchers.IO).launch{
                var locationId = "KrsezdPuMUVlMxezoYxc"
                var reviews = reviewViewModel.getReviewsForLocation(locationId)
                for (review in reviews) {
                    println("debugk: $review")
                }
            }
        }

        /**
         * Adds a review for a location. Does not need to be run in a coroutine.
         */
        fun addReview() {
            var locationId = "KrsezdPuMUVlMxezoYxc"

            var newReview = Review()
            newReview.locationId = locationId
            newReview.leftByAdmin = false
            newReview.cleanliness = 5
            newReview.dateAdded = Calendar.getInstance().timeInMillis
            newReview.sufficientPaperTowels = 1
            newReview.sufficientSoap = 2
            newReview.accessibility = 0
            newReview.comment = "New comment"

            var reviewViewModel = ReviewViewModel()
            reviewViewModel.addReviewForLocation(newReview)
        }
    }

}