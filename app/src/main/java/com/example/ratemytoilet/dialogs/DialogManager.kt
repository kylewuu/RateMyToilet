package com.example.ratemytoilet.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

class DialogManager {
    companion object {
        fun showAlertDialog(context: Context, title: String, message: String, positiveCallback: (() -> Unit)? = null) {
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { _, _ ->
                    if (positiveCallback != null) {
                        positiveCallback()
                    }
                })
                .show()
        }
    }
}