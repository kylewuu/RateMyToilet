package com.example.ratemytoilet.listadapters

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import com.example.ratemytoilet.R
import com.example.ratemytoilet.database.UserComment

/**
 * Adapter for each review in washroom details.
 */
class WashroomDetailsReviewAdapter(var context: Context, var arrayList: ArrayList<UserComment>) : BaseAdapter(){
    override fun getCount(): Int {
        return arrayList.size
    }

    override fun getItem(position: Int): Any {
       return arrayList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * Gets the view for the adapter, will set the texts with the passed in values
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        if (arrayList == null || arrayList.size == 0) {
            return TextView(context)
        }
        val view:View = View.inflate(context, R.layout.cardview_list_layout, null)
        val dateText = view.findViewById<TextView>(R.id.userDate)
        val comment = view.findViewById<EditText>(R.id.userComment)
        val rating = view.findViewById<RatingBar>(R.id.singleRating)
        val paperState = view.findViewById<TextView>(R.id.paperStateText)
        val soapState = view.findViewById<TextView>(R.id.soapStateText)
        val accState = view.findViewById<TextView>(R.id.accessStateText)
        val userInformation = arrayList.get(position)
        dateText.setText(userInformation.date)
        comment.setText(userInformation.comment)
        userInformation.paper?.let { setTextState(paperState, it) }
        userInformation.soap?.let { setTextState(soapState, it) }
        userInformation.access?.let { setTextState(accState, it) }
        userInformation.rate?.let { rating.setRating(it) }

        return view
    }

    fun update(newList: ArrayList<UserComment>) {
        arrayList = newList
    }

    /**
     * Decides what colour the attributes should have in the review card details
     */
    private fun setTextState(textView: TextView, state: Int) {
        if (state == 1) {
            textView.setText("Yes")
            textView.setTextColor(Color.GREEN)
        } else if (state == 0) {
            textView.setText("No")
            textView.setTextColor(Color.RED)
        } else {
            textView.setText("Unknown")
            textView.setTextColor(Color.DKGRAY)
        }
    }
}
