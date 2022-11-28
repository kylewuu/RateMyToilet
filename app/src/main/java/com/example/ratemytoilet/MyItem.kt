package com.example.ratemytoilet

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class MyItem : ClusterItem {
    private val mPosition: LatLng
    private val mTitle: String
    private val mSnippet: String
    private val mIcon : BitmapDescriptor
    private val myId : String

    constructor(mPosition: LatLng, mTitle: String, mSnippet: String, mIcon : BitmapDescriptor, myId : String) {
        this.mPosition = mPosition
        this.mTitle = mTitle
        this.mSnippet = mSnippet
        this.mIcon = mIcon
        this.myId = myId
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

}