package com.example.ratemytoilet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class AddNewLocationFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        var inflate = inflater.inflate(R.layout.fragment_add_new_location, container, false)

        //activity?.actionBar?.show()
        return inflate
    }

}