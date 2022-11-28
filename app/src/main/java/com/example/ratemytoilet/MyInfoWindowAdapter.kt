package com.example.ratemytoilet

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker

class MyInfoWindowAdapter(context : Context) : GoogleMap.InfoWindowAdapter {
    var mContext = context
    var view : View = LayoutInflater.from(context).inflate(R.layout.custom_inforwindow, null)

    private fun setWindowText(marker: Marker, view: View){
        val soapCheck = view.findViewById<ImageView>(R.id.soapCheck)
        val paperCheck = view.findViewById<ImageView>(R.id.paperCheck)
        val accessCheck = view.findViewById<ImageView>(R.id.accessCheck)
        val location = view.findViewById<TextView>(R.id.locationTextView)
        val rate = view.findViewById<TextView>(R.id.rateText)

        val checkResult = marker.title
        val locationResult = marker.snippet

        val results = checkResult?.split(",")
        if (results != null && results.size >= 3) {
            val soapResult = results[0]
            val paperResult = results[1]
            val accessResult = results[2]
            if (soapResult == "true") {
                soapCheck.setImageResource(R.drawable.checkmark)
            } else if (soapResult == "false"){
                soapCheck.setImageResource(R.drawable.uncheck)
            } else {
                soapCheck.setImageResource(R.drawable.unknown)
            }

            if (paperResult == "true") {
                paperCheck.setImageResource(R.drawable.checkmark)
            } else if (paperResult == "false"){
                paperCheck.setImageResource(R.drawable.uncheck)
            } else {
                paperCheck.setImageResource(R.drawable.unknown)
            }

            if (accessResult == "true") {
                accessCheck.setImageResource(R.drawable.checkmark)
            } else if (accessResult == "false"){
                accessCheck.setImageResource(R.drawable.uncheck)
            } else {
                accessCheck.setImageResource(R.drawable.unknown)
            }
        }

        val locResults = locationResult?.split(";")
        if (locResults != null && locResults.size >= 2) {
            val locResult = locResults[0]
            val rateResult = locResults[1]
            location.setText(locResult)
            rate.setText(rateResult)
        }
    }


    override fun getInfoContents(marker: Marker): View? {
        setWindowText(marker, view)
        return view
    }

    override fun getInfoWindow(marker: Marker): View? {
        setWindowText(marker, view)
        return view
    }

}