package com.example.ratemytoilet.launch

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.ratemytoilet.R
import com.example.ratemytoilet.database.ToiletUser
import com.example.ratemytoilet.databinding.ActivityConfirmPhoneBinding
import com.example.ratemytoilet.dialogs.DialogManager
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.concurrent.TimeUnit

private const val TAG = "ConfirmPhoneActivity"

/**
 * Activity for confirming phone number using the verification code that
 * was sent.
 */
class ConfirmPhoneActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfirmPhoneBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var verificationId = ""
    private var authCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithCredential(credential)
        }

        override fun onVerificationFailed(ex: FirebaseException) {
            DialogManager.showAlertDialog(
                this@ConfirmPhoneActivity,
                getString(R.string.error),
                ex.message ?: getString(R.string.verification_failed)
            ) { finish() }
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            this@ConfirmPhoneActivity.verificationId = verificationId
            Log.i(TAG, "set verification id to: $verificationId")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Retrieve the phone the user will verify and display it to the user
        val phone = getSharedPreferences(getString(R.string.prefs_filename), Context.MODE_PRIVATE)
            .getString(getString(R.string.prefs_key_phone), "null")!!

        // Run the Firebase phone verification procedure
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phone)       // Phone number to verify
            .setTimeout(60, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(authCallbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun onVerifyCodeClick(view: View) {
        val credential = PhoneAuthProvider.getCredential(verificationId, binding.verificationEditText.text.toString())
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener(this) {
                loginOrRegister {
                    finish()
                    Log.d("TAp", "success")
                }
            }
            .addOnFailureListener(this) {
                val ex = it as? FirebaseAuthException
                DialogManager.showAlertDialog(this,
                    getString(R.string.error),
                    getString(R.string.invalid_auth_code)
                )
                ex?.printStackTrace()
            }
    }

    private fun loginOrRegister(completion: () -> Unit) {
        val currentUserID = auth.currentUser!!.uid
        db.document("users/${currentUserID}").get().addOnCompleteListener {
            if (it.result.exists()) {
                completion()
            } else {
                val currentUserID = auth.currentUser!!.uid
                val toiletUser = ToiletUser(auth.currentUser!!.phoneNumber!!)
                db.document("users/${currentUserID}").set(toiletUser).addOnSuccessListener {
                    completion()
                }.addOnFailureListener {
                    val ex = it as? FirebaseFirestoreException
                    DialogManager.showAlertDialog(this,
                        getString(R.string.error),
                        ex?.message ?: getString(R.string.verification_failed)
                    ) { finish() }
                }
            }
        }
    }

}