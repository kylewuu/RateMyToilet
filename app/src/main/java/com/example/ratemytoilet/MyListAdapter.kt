package com.example.ratemytoilet


import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.ratemytoilet.database.Location
import com.google.android.gms.maps.model.LatLng
import java.text.DecimalFormat

// Based off class demo
class MyListAdapter(private val context: Context, private var commentList: List<Location>) : BaseAdapter() {

    // Returns the values associated with a key in the database
    override fun getItem(position: Int): (Array<String>) {
        return arrayOf(
            commentList.get(position).id

        )

    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return commentList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.layout_adapter, null)

        val header = view.findViewById(R.id.tv_header) as TextView
        val distanceAndDuration = view.findViewById(R.id.tv_distanceAndDuration) as TextView

        header.text = commentList.get(position).id
        distanceAndDuration.text = commentList.get(position).name


        return view
    }

    fun replace(newCommentList: List<Location>) {
        commentList = newCommentList
    }
}

