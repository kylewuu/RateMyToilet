package com.example.ratemytoilet

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.ratemytoilet.MainActivity.Companion.updateList
import com.example.ratemytoilet.MainActivity.Companion.updateMap
import com.example.ratemytoilet.MainActivity.Companion.updateReviews
import com.example.ratemytoilet.database.Review
import com.example.ratemytoilet.database.ReviewViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.*

/**
 * Activity for adding a new review for admins only.
 *
 */
class NewReviewAdminFragment : DialogFragment() {
    var listener: AdminReviewListener? = null
    private var paperResult = 1
    private var soapResult = 1
    private var cleanResult = 1
    private var accessibility = 2
    private var comment = ""
    private var washroomId = ""

    companion object {
        var LOCATION_ID_KEY = "location_id_key"
        var ACCESSIBILITY_KEY = "accessibility_key"
    }

    /**
     * Creates the dialog for admins adding a new review. Sets the on click listeners for all buttons
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layoutInflater = requireActivity().layoutInflater
        val dialogLayout = layoutInflater.inflate(R.layout.fragment_admin_check, null)
        val builder = AlertDialog.Builder(requireActivity(), R.style.DialogAnimation).setView(dialogLayout)
        val saveButton = dialogLayout.findViewById<Button>(R.id.applyAdminButton)
        val cancelButton = dialogLayout.findViewById<Button>(R.id.cancelAdminButton)

        var bundle = arguments
        accessibility = bundle?.getInt(ACCESSIBILITY_KEY) ?: 2
        washroomId = bundle?.getString(LOCATION_ID_KEY) ?: ""

        saveButton.setOnClickListener {
            val cleanGroup = dialogLayout.findViewById<ChipGroup>(R.id.cleanGroupAdmin)
            val cleanChipT = cleanGroup.getChildAt(0) as Chip
            val cleanChipF = cleanGroup.getChildAt(1) as Chip

            val soapGroup = dialogLayout.findViewById<ChipGroup>(R.id.soapGroupAdmin)
            val soapChipT = soapGroup.getChildAt(0) as Chip
            val soapChipF = soapGroup.getChildAt(1) as Chip

            val paperGroup = dialogLayout.findViewById<ChipGroup>(R.id.paperGroupAdmin)
            val paperChipT = paperGroup.getChildAt(0) as Chip
            val paperChipF = paperGroup.getChildAt(1) as Chip

            val commentText = dialogLayout.findViewById<EditText>(R.id.adminCommentText)
            comment = commentText.text.toString()

            if ((cleanChipT.isChecked || cleanChipF.isChecked) &&
                (soapChipT.isChecked || soapChipF.isChecked) &&
                (paperChipT.isChecked || paperChipF.isChecked) &&
                (comment != "")) {

                if (cleanChipT.isChecked) {
                    cleanResult = 1
                } else {
                    cleanResult = 0
                }

                if (soapChipT.isChecked) {
                    soapResult = 1
                } else {
                    soapResult = 0
                }

                if (paperChipT.isChecked) {
                    paperResult = 1
                } else {
                    paperResult = 0
                }

                val review = Review()
                review.locationId = washroomId
                review.leftByAdmin = true
                review.dateAdded = Calendar.getInstance().timeInMillis
                review.sufficientSoap = soapResult
                review.sufficientPaperTowels = paperResult
                review.comment = comment
                review.cleanliness = if (cleanResult == 1) {5} else {1}
                review.accessibility = accessibility
                var reviewViewModel = ReviewViewModel()
                reviewViewModel.addReviewForLocation(review)
                updateReviews = true
                updateMap = true
                updateList = true
                dismiss()
                listener?.loadReviews()
            } else {
                Toast.makeText(context, "Please fill out all the fields", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        return builder.create()
    }

    /**
     * Stars the dialog with the sizes
     */
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AdminReviewListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement FilterListener")
        }
    }

    /**
     * Interface for loading reviews after admin saves their review
     */
    interface AdminReviewListener {
        fun loadReviews()
    }
}
