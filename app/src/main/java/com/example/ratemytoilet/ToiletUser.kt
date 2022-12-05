package com.example.ratemytoilet

import com.google.firebase.firestore.DocumentId

data class ToiletUser(val phoneNumber: String? = "", val totalReviews: Int? = 0, @DocumentId val reviews: String? = "")