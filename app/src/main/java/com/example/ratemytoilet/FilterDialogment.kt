package com.example.ratemytoilet

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.DialogFragment

class FilterDialogment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layoutInflater = requireActivity().layoutInflater
        val dialogLayout = layoutInflater.inflate(R.layout.fragment_dialog, null)
        val builder = AlertDialog.Builder(requireActivity()).setView(dialogLayout)
        val cleanSpinner = dialogLayout.findViewById<Spinner>(R.id.minimumCleanSpinner)
        cleanSpinner.adapter = ArrayAdapter.createFromResource(requireActivity(), R.array.clean_spinner, R.layout.spinner_style)
        val genderSpinner = dialogLayout.findViewById<Spinner>(R.id.selectGender)
        genderSpinner.adapter = ArrayAdapter.createFromResource(requireActivity(), R.array.gender_spinner, R.layout.spinner_style)
        return builder.create()
    }
}