package com.example.ratemytoilet.database

import com.example.ratemytoilet.database.Location.Companion.toLocation
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


/**
 * refs:
 * https://medium.com/firebase-developers/android-mvvm-firestore-37c3a8d65404
 * https://stackoverflow.com/questions/72833016/how-to-return-data-from-inside-of-addonsuccesslistener-method-in-kotlin
 */
class LocationRepository {

    companion object {
        private var firestore = Firebase.firestore
        private var collection = firestore.collection("location")

        fun getLocationById() {
            CoroutineScope(IO).launch {
                var location = collection.document("KrsezdPuMUVlMxezoYxc").get().await().toLocation()
            }
        }

        suspend fun getAllLocations(): List<Location>  {
            return collection.get().await().documents.map { it.toLocation() }
        }

        fun getAllLocationsFlow(): Flow<List<Location>> {
            return callbackFlow {
                var listener = collection.addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (firebaseFirestoreException == null && querySnapshot != null) {
                        var ret = querySnapshot.documents.mapNotNull { it.toLocation() };
                        trySend(ret)
                    }
                }
                awaitClose {
                    listener.remove()
                }
            }
        }

        suspend fun addLocation(newLocation: Location): Flow<String> {
            return callbackFlow {
                var listener = collection.add(newLocation.toLocationMap())
                    .addOnSuccessListener { documentReference ->
                        var documentId = documentReference.id
                        trySend(documentId)
                    }
                awaitClose {
                }
            }
        }
    }
}