package com.example.ratemytoilet.database

data class Review (
    var id: Long,
    var locationId: String,
    var cleanliness: Int,
    var dateAdded: Long,
    var sufficientPaperTowels: Boolean?,
    var sufficientSoap: Boolean?,
    var accessibility: Boolean?,
    var comment: String
)