package com.example.ratemytoilet

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment

class FilterDialogment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layoutInflater = requireActivity().layoutInflater
        val dialogLayout = layoutInflater.inflate(R.layout.fragment_dialog, null)
        val builder = AlertDialog.Builder(requireActivity()).setView(dialogLayout)
        val saveButton = dialogLayout.findViewById<Button>(R.id.applyButton)
        val cancelButton = dialogLayout.findViewById<Button>(R.id.cancelDialogButton)
        val paperCheck = dialogLayout.findViewById<SwitchCompat>(R.id.paperSwitchCheck)
        val soapCheck = dialogLayout.findViewById<SwitchCompat>(R.id.soapSwitchCheck)
        val cleanSpinner = dialogLayout.findViewById<Spinner>(R.id.minimumCleanSpinner)
        cleanSpinner.adapter = ArrayAdapter.createFromResource(requireActivity(), R.array.clean_spinner, R.layout.spinner_style)

        val genderSpinner = dialogLayout.findViewById<Spinner>(R.id.selectGender)
        genderSpinner.adapter = ArrayAdapter.createFromResource(requireActivity(), R.array.gender_spinner, R.layout.spinner_style)

        saveButton.setOnClickListener {
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
        return builder.create()
    }
}