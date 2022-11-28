package com.example.ratemytoilet

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RatingBar
import android.widget.TextView

class UserCommentListAdapter(var context: Context, var arrayList: ArrayList<UserComment>) : BaseAdapter(){
    override fun getCount(): Int {
        return arrayList.size
    }

    override fun getItem(position: Int): Any {
       return arrayList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view:View = View.inflate(context, R.layout.cardview_list_layout, null)
        val userNameText = view.findViewById<TextView>(R.id.userNameText)
        val dateText = view.findViewById<TextView>(R.id.userDate)
        val comment = view.findViewById<TextView>(R.id.userComment)
        val rating = view.findViewById<RatingBar>(R.id.singleRating)
        val userInformation = arrayList.get(position)

        userNameText.setText(userInformation.userName)
        dateText.setText(userInformation.date)
        comment.setText(userInformation.comment)
        userInformation.rate?.let { rating.setRating(it) }


        return view

    }

    fun update(newList: ArrayList<UserComment>) {
        arrayList = newList
    }

}