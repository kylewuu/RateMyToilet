package com.example.ratemytoilet

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import androidx.fragment.app.DialogFragment
import com.example.ratemytoilet.database.Review
import com.example.ratemytoilet.database.ReviewViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.*


class AdminFragment : DialogFragment() {
    private var paperResult = 1
    private var soapResult = 1
    private var cleanResult = 1
    private var comment = ""
    private var washroomId : String ?= null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layoutInflater = requireActivity().layoutInflater
        val dialogLayout = layoutInflater.inflate(R.layout.fragment_admin_check, null)
        val builder = AlertDialog.Builder(requireActivity(), R.style.DialogAnimation).setView(dialogLayout)
        val saveButton = dialogLayout.findViewById<Button>(R.id.applyAdminButton)
        val cancelButton = dialogLayout.findViewById<Button>(R.id.cancelAdminButton)

        val message = arguments?.getString("message")

        saveButton.setOnClickListener {

            val cleanGroup = dialogLayout.findViewById<ChipGroup>(R.id.cleanGroupAdmin)
            val cleanChipT = cleanGroup.getChildAt(0) as Chip
            val cleanChipF = cleanGroup.getChildAt(1) as Chip
            if (cleanChipF.isChecked) {
                cleanResult = 0
            }
            if (!cleanChipT.isChecked && !cleanChipF.isChecked) {
                cleanResult = 2
            }

            val soapGroup = dialogLayout.findViewById<ChipGroup>(R.id.soapGroupAdmin)
            val soapChipT = soapGroup.getChildAt(0) as Chip
            val soapChipF = soapGroup.getChildAt(1) as Chip
            if (soapChipF.isChecked) {
                soapResult = 0
            }
            if (!soapChipT.isChecked && !soapChipF.isChecked) {
                soapResult = 2
            }

            val paperGroup = dialogLayout.findViewById<ChipGroup>(R.id.paperGroupAdmin)
            val paperChipT = paperGroup.getChildAt(0) as Chip
            val paperChipF = paperGroup.getChildAt(1) as Chip
            if (paperChipF.isChecked) {
                paperResult = 0
            }
            if (!paperChipT.isChecked && !paperChipF.isChecked) {
                paperResult = 2
            }

            val commentText = dialogLayout.findViewById<EditText>(R.id.adminCommentText)
            comment = commentText.text.toString()
            val review = Review()
            review.locationId = washroomId.toString()
            review.leftByAdmin = true
            review.dateAdded = Calendar.getInstance().timeInMillis
            review.sufficientSoap = soapResult
            review.sufficientPaperTowels = paperResult
            review.comment = comment
            var reviewViewModel = ReviewViewModel()
            reviewViewModel.addReviewForLocation(review)
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