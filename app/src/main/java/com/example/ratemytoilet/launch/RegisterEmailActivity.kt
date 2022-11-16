package com.example.ratemytoilet.launch

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.ratemytoilet.R
import com.example.ratemytoilet.databinding.ActivityRegisterEmailBinding
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private const val TAG = "RegisterEmailActivity"

class RegisterEmailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterEmailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
    }

    fun onRegisterEmailClick(view: View) {
        if (binding.passwordEditText.text.toString() == binding.confirmPasswordEditText.text.toString()) {
            createAccount()
        } else {
            showAlertDialog(
                getString(R.string.title_password_mismatch),
                getString(R.string.desc_password_mismatch)
            )
        }
    }

    private fun createAccount() {
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        Firebase.auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                    verifyEmail(email)
                finish()
                }
            .addOnFailureListener {
                val ex = it as FirebaseAuthException
                showAlertDialog(
                    getString(R.string.error),
                    ex.message ?: getString(R.string.registration_failed)
                )
                ex.printStackTrace()
            }
    }

    private fun verifyEmail(email: String) {
        val actionCodeSettings = actionCodeSettings {
            url = "https://ratemytoilet-da4c8.firebaseapp.com"
            handleCodeInApp = true
            setAndroidPackageName(
                "com.example.ratemytoilet",
                false,
                "1"
            )
        }

//        Firebase.auth.sendSignInLinkToEmail(email, actionCodeSettings)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    // Save the email
//                    getSharedPreferences(getString(R.string.prefs_filename), Context.MODE_PRIVATE)
//                        .edit()
//                        .putString(getString(R.string.prefs_key_email), email)
//                        .commit()
//                    showConfirmScreen()
//                } else {
//                    showAlertDialog(
//                        getString(R.string.error),
//                        task.exception?.message ?: getString(R.string.registration_failed)
//                    )
//                    task.exception?.printStackTrace()
//                }
//            }
    }

    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun showConfirmScreen() {
        val intent = Intent(this, ConfirmEmailActivity::class.java)
        startActivity(intent)
    }


}