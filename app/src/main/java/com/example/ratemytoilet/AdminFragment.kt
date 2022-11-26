package com.example.ratemytoilet

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment


class AdminFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layoutInflater = requireActivity().layoutInflater
        val dialogLayout = layoutInflater.inflate(R.layout.fragment_admin_check, null)
        val builder = AlertDialog.Builder(requireActivity(), R.style.DialogAnimation).setView(dialogLayout)
        val saveButton = dialogLayout.findViewById<Button>(R.id.applyAdminButton)
        val cancelButton = dialogLayout.findViewById<Button>(R.id.cancelAdminButton)

        saveButton.setOnClickListener {
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if(dialog!= null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window!!.setLayout(width, height)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            dialog.window!!.setGravity(Gravity.BOTTOM)
        }
    }
}