package com.example.ratemytoilet

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.slider.RangeSlider


class FilterDialogFragment : DialogFragment() {
    var listener : FilterListener?= null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val layoutInflater = requireActivity().layoutInflater
        val dialogLayout = layoutInflater.inflate(R.layout.fragment_dialog, null)
        val builder = AlertDialog.Builder(requireActivity(), R.style.DialogAnimation).setView(dialogLayout)
        val saveButton = dialogLayout.findViewById<Button>(R.id.applyButton)
        val cancelButton = dialogLayout.findViewById<Button>(R.id.cancelDialogButton)
        val paperCheck = dialogLayout.findViewById<Chip>(R.id.paperChip)
        val soapCheck = dialogLayout.findViewById<Chip>(R.id.soapChip)
        val accessCheck = dialogLayout.findViewById<Chip>(R.id.accChip)
        val maleCheck = dialogLayout.findViewById<Chip>(R.id.maleChip)
        val femaleCheck = dialogLayout.findViewById<Chip>(R.id.femaleChip)
        val cleanliness = dialogLayout.findViewById<RangeSlider>(R.id.cleanRange)

        saveButton.setOnClickListener {
            listener?.onFilterConditionPassed(paperCheck.isChecked, soapCheck.isChecked, accessCheck.isChecked, maleCheck.isChecked, femaleCheck.isChecked, cleanliness.values[0], cleanliness.values[1])
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FilterListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement FilterListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
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

    interface FilterListener {
        fun onFilterConditionPassed(paperCheck : Boolean, soapCheck : Boolean, accessCheck : Boolean, maleCheck : Boolean, femaleCheck : Boolean, startValue : Float, endValue : Float)
    }

}