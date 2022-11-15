package com.example.ratemytoilet

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView


class MyListAdapter
    (
    private val context: Activity,
    private val maintitle: Array<String>,
    private val subtitle: Array<String>,
    private val imgid: Array<Int>,
    private val ratings: Array<String>,
    private val distance: Array<String>
) :
    ArrayAdapter<String?>(context, R.layout.review_list, maintitle) {
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView: View = inflater.inflate(R.layout.review_list, null, true)
        val titleText = rowView.findViewById<View>(R.id.title) as TextView
        val imageView = rowView.findViewById<View>(R.id.icon) as ImageView
        val subtitleText = rowView.findViewById<View>(R.id.subtitle) as TextView
        val ratingsText = rowView.findViewById<View>(R.id.tv_rating) as TextView
        val distanceText = rowView.findViewById<View>(R.id.tv_distance) as TextView
        ratingsText.text = ratings[position]
        titleText.text = maintitle[position]
        imageView.setImageResource(imgid[position])
        subtitleText.text = subtitle[position]
        distanceText.text = distance[position]
        return rowView
    }
}