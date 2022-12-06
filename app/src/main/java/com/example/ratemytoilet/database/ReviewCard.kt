package com.example.ratemytoilet.database

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class ReviewCard : ClusterItem {
    private val mPosition: LatLng
    private val mTitle: String
    private val mSnippet: String
    private val mIcon : BitmapDescriptor
    private val myId : String
    private val myDate : Long
    private val myGender : Int

    constructor(mPosition: LatLng, mTitle: String, mSnippet: String, mIcon : BitmapDescriptor, myId : String, myDate : Long, myGender : Int) {
        this.mPosition = mPosition
        this.mTitle = mTitle
        this.mSnippet = mSnippet
        this.mIcon = mIcon
        this.myId = myId
        this.myDate = myDate
        this.myGender = myGender
    }

    override fun getPosition(): LatLng {
        return mPosition
    }

    override fun getTitle(): String? {
        return mTitle
    }

    override fun getSnippet(): String? {
        return mSnippet
    }

    fun getIcon(): BitmapDescriptor? {
        return mIcon
    }

    fun getId() : String? {
        return myId
    }

    fun getDate() : Long? {
        return myDate
    }

    fun getGender() : Int? {
        return myGender
    }

}