package com.example.ratemytoilet.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData

class LocationViewModel : ViewModel() {
    var locations: LiveData<List<Location>> = LocationRepository.getAllLocationsFlow().asLiveData()

    fun addLocation(location: Location) {
        LocationRepository.addLocation(location)
    }
}