package com.example.ratemytoilet.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow

/**
 * refs:
 * https://stackoverflow.com/questions/68840086/proper-way-to-collect-values-from-flow-in-android-and-coroutines
 *
 */
class LocationViewModel : ViewModel() {
    var locations: LiveData<List<Location>> = LocationRepository.getAllLocationsFlow().asLiveData()

    suspend fun addLocation(location: Location): Flow<String> {
        return LocationRepository.addLocation(location)
    }

    suspend fun getAllLocations(): List<Location> {
        return LocationRepository.getAllLocations()
    }
}