package com.example.ratemytoilet.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.ratemytoilet.MainActivity
import com.example.ratemytoilet.MyItem
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ui.IconGenerator
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * refs:
 * https://stackoverflow.com/questions/68840086/proper-way-to-collect-values-from-flow-in-android-and-coroutines
 *
 */
class LocationViewModel : ViewModel() {
    var tempListLocations: MutableLiveData<ArrayList<Location>> = MutableLiveData()
    var tempMarkers: MutableLiveData<ArrayList<MyItem>> = MutableLiveData()
    var locations: LiveData<List<Location>> = LocationRepository.getAllLocationsFlow().asLiveData()

    suspend fun addLocation(location: Location): Flow<String> {
        return LocationRepository.addLocation(location)
    }

    suspend fun getAllLocations(): List<Location> {
        return LocationRepository.getAllLocations()
    }

    suspend fun processMapLocations(bubble: IconGenerator) {
        var newLocations = ArrayList<Location>()
        var arr = ArrayList<MyItem>()
        var allLocations = getAllLocations()
        val reviewViewModel = ReviewViewModel()
        if (MainActivity.isAdmin) allLocations = filterAdminMarkers(allLocations, reviewViewModel)
        if (allLocations != null &&
            (MainActivity.paperCheck ||
                    MainActivity.soapCheck ||
                    MainActivity.accessCheck ||
                    MainActivity.maleCheck ||
                    MainActivity.femaleCheck ||
                    MainActivity.cleanlinessStart != 1f ||
                    MainActivity.cleanlinessEnd != 5f)
        ) {
            for (location in allLocations) {
                var shouldAdd = true
                var rating = 0.0
                var allReviews = reviewViewModel.getReviewsForLocation(location.id)
                allReviews = allReviews.sortedByDescending { it.dateAdded }
                if (allReviews.isNotEmpty()) {
                    for (review in allReviews) {
                        rating += review.cleanliness
                    }
                    rating /= allReviews.size

                    if (MainActivity.paperCheck) {
                        if (allReviews[0].sufficientPaperTowels != 1) {
                            shouldAdd = false
                        }
                    }
                    if (MainActivity.soapCheck) {
                        if (allReviews[0].sufficientSoap != 1) {
                            shouldAdd = false
                        }
                    }
                    if (MainActivity.accessCheck) {
                        if (allReviews[0].accessibility != 1) {
                            shouldAdd = false
                        }
                    }
                }
                if ((MainActivity.femaleCheck && !MainActivity.maleCheck) || (!MainActivity.femaleCheck && MainActivity.maleCheck)) {
                    if (MainActivity.maleCheck) {
                        if (location.gender != 0 && location.gender != 2) {
                            shouldAdd = false
                        }
                    }
                    if (MainActivity.femaleCheck) {
                        if (location.gender != 1 && location.gender != 2) {
                            shouldAdd = false
                        }
                    }
                }

                if (rating !in MainActivity.cleanlinessStart..MainActivity.cleanlinessEnd) {
                    shouldAdd = false
                }

                if (shouldAdd) newLocations.add(location)
            }
        } else if (allLocations != null){
            newLocations = allLocations as java.util.ArrayList<Location>
        }

        if (newLocations.isNotEmpty()) {
            val updates = newLocations.distinctBy { it.id }
            for(update in updates) {
                val updateLatLng = LatLng(update.lat, update.lng)
                var updateRating = 0.0
                var updateSoap = "true"
                var updatePaper = "true"
                var updateAccess = "true"
                var updateAllReviews = reviewViewModel.getReviewsForLocation(update.id)
                updateAllReviews = updateAllReviews.sortedByDescending { it.dateAdded }
                if (updateAllReviews.isNotEmpty()) {
                    for (review in updateAllReviews) {
                        updateRating += review.cleanliness
                    }
                    updateRating /= updateAllReviews.size
                    if (updateAllReviews[0].sufficientSoap == 0) {
                        updateSoap = "false"
                    } else if (updateAllReviews[0].sufficientSoap == 2) {
                        updateSoap = "unknown"
                    }

                    if (updateAllReviews[0].sufficientPaperTowels == 0) {
                        updatePaper = "false"
                    } else if (updateAllReviews[0].sufficientPaperTowels == 2) {
                        updatePaper = "unknown"
                    }

                    if (updateAllReviews[0].accessibility == 0) {
                        updateAccess = "false"
                    } else if (updateAllReviews[0].accessibility == 2) {
                        updateAccess = "unknown"
                    }
                }
                val snippet = update.name + ", " + update.roomNumber + ";" + updateRating.toInt()
                val title = "$updateSoap,$updatePaper,$updateAccess"
                val item = MyItem(
                    updateLatLng,
                    title,
                    snippet,
                    BitmapDescriptorFactory.fromBitmap(
                        bubble.makeIcon(
                            updateRating.toInt().toString()
                        )
                    ),
                    update.id,
                    update.date,
                    update.gender
                )
                arr.add(item)
            }
        }

        withContext(Main) {
            tempMarkers.value = arr
        }
    }

    suspend fun processListLocations() {
        var newLocations = ArrayList<Location>()
        val reviewViewModel = ReviewViewModel()
        var allLocations = getAllLocations()
        if (MainActivity.isAdmin) allLocations = filterAdminMarkers(allLocations, reviewViewModel)
        if (allLocations != null &&
            (MainActivity.paperCheck ||
                    MainActivity.soapCheck ||
                    MainActivity.accessCheck ||
                    MainActivity.maleCheck ||
                    MainActivity.femaleCheck ||
                    MainActivity.cleanlinessStart != 1f ||
                    MainActivity.cleanlinessEnd != 5f)
        ) {
            for (location in allLocations) {
                var shouldAdd = true
                var rating = 0.0

                var allReviews = reviewViewModel.getReviewsForLocation(location.id)
                allReviews = allReviews.sortedByDescending { it.dateAdded }
                if (allReviews.isNotEmpty()) {
                    for (review in allReviews) {
                        rating += review.cleanliness
                    }
                    rating /= allReviews.size

                    if (MainActivity.paperCheck) {
                        if (allReviews[0].sufficientPaperTowels != 1) {
                            shouldAdd = false
                        }
                    }
                    if (MainActivity.soapCheck) {
                        if (allReviews[0].sufficientSoap != 1) {
                            shouldAdd = false
                        }
                    }
                    if (MainActivity.accessCheck) {
                        if (allReviews[0].accessibility != 1) {
                            shouldAdd = false
                        }
                    }
                }
                if ((MainActivity.femaleCheck && !MainActivity.maleCheck) || (!MainActivity.femaleCheck && MainActivity.maleCheck)) {
                    if (MainActivity.maleCheck) {
                        if (location.gender != 0 && location.gender != 2) {
                            shouldAdd = false
                        }
                    }
                    if (MainActivity.femaleCheck) {
                        if (location.gender != 1 && location.gender != 2) {
                            shouldAdd = false
                        }
                    }
                }

                if (rating !in MainActivity.cleanlinessStart..MainActivity.cleanlinessEnd) {
                    shouldAdd = false
                }

                if (shouldAdd) newLocations.add(location)
            }
        } else {
            newLocations = allLocations as java.util.ArrayList<Location>
        }

        withContext(Main) {
            tempListLocations.value = newLocations
        }
    }

    private suspend fun filterAdminMarkers(allLocations: List<Location>, reviewViewModel: ReviewViewModel): List<Location> {
        var filteredLocations = ArrayList<Location>()
        for (location in allLocations) {
            var shouldAdd = false
            var rating = 0.0
            var allReviews = reviewViewModel.getReviewsForLocation(location.id)
            allReviews = allReviews.sortedByDescending { it.dateAdded }
            if (allReviews.isNotEmpty()) {
                for (review in allReviews) {
                    rating += review.cleanliness
                }
                rating /= allReviews.size

                if (allReviews[0].sufficientPaperTowels == 0) {
                    shouldAdd = true
                }


                if (allReviews[0].sufficientSoap == 0) {
                    shouldAdd = true
                }

            }

            if (rating < 3) {
                shouldAdd = true
            }

            if (shouldAdd) filteredLocations.add(location)
        }

        return filteredLocations
    }

}