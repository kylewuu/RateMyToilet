package com.example.ratemytoilet.database

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// ref: https://firebase.google.com/docs/firestore/quickstart#kotlin+ktx_1
class Locations(var count: Int) {

    val newLocation = hashMapOf(
        "name" to "New washroom",
        "locationLat" to 123,
        "locationLng" to 456,
        "dateCreated" to count
    )

    init {
        var db = Firebase.firestore
        db.collection("Locations")
            .add(newLocation)
            .addOnSuccessListener { documentReference ->
                println("debug: DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                println("debug: Error adding document $e")
            }
    }

}