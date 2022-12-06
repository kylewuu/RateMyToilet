package com.example.ratemytoilet.launch

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.View
import com.example.ratemytoilet.dialogs.DialogManager
import com.example.ratemytoilet.R
import com.example.ratemytoilet.databinding.ActivityPhoneBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*

private const val TAG = "PhoneActivity"

class PhoneActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhoneBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.phoneEditText.addTextChangedListener(PhoneNumberFormattingTextWatcher())
    }

    override fun onStart() {
        super.onStart()
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            finish()
        }
    }

    fun onVerifyPhoneClick(view: View) {
        var rawPhoneNumber = binding.phoneEditText.text.toString()
//        rawPhoneNumber = "+1" + rawPhoneNumber.filter { it.isDigit() }
        Log.i(TAG, "rawPhoneNumber is now: $rawPhoneNumber")
        val formattedPhoneNumber = PhoneNumberUtils.formatNumberToE164(
            rawPhoneNumber,
            Locale.getDefault().country
        )
        Log.i(TAG, "formattedPhoneNumber for country code ${Locale.getDefault().country}: $formattedPhoneNumber")
//        val formattedPhoneNumber = rawPhoneNumber
        if (formattedPhoneNumber != null) {
            getSharedPreferences(getString(R.string.prefs_filename), Context.MODE_PRIVATE)
                .edit()
                .putString(getString(R.string.prefs_key_phone), formattedPhoneNumber)
                .commit()

            val intent = Intent(this, ConfirmPhoneActivity::class.java)
            startActivity(intent)
        } else {
            DialogManager.showAlertDialog(
                this,
                getString(R.string.error),
                getString(R.string.invalid_phone)
            )
        }
    }

}