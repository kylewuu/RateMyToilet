package com.example.ratemytoilet.launch

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ratemytoilet.R
import com.example.ratemytoilet.databinding.ActivityConfirmEmailBinding
import com.example.ratemytoilet.databinding.ActivityRegisterEmailBinding

class ConfirmEmailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfirmEmailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the email the user will verify and display it to the user
        val email = getSharedPreferences(getString(R.string.prefs_filename), Context.MODE_PRIVATE)
            .getString(getString(R.string.prefs_key_email), "null")!!
        binding.confirmationEmailTextView.text = getString(R.string.desc_confirmation_code).format(
            email
        )
    }
}