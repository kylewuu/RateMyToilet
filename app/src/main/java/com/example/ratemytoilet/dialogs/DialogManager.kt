package com.example.ratemytoilet.dialogs

import android.content.Context
import androidx.appcompat.app.AlertDialog

/**
 * Manager for showing dialogs.
 */
class DialogManager {
    companion object {
        fun showAlertDialog(context: Context, title: String, message: String, positiveCallback: (() -> Unit)? = null) {
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    if (positiveCallback != null) {
                        positiveCallback()
                    }
                }
                .show()
        }
    }
}
