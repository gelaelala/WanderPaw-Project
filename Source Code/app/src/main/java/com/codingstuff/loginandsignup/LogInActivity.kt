package com.codingstuff.loginandsignup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.codingstuff.loginandsignup.AuthExceptionHandler.Companion.handleException
import com.codingstuff.loginandsignup.databinding.ActivityLogInBinding
import com.google.firebase.auth.FirebaseAuth

class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val authToastLess = AuthToastLess(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        setupClickListener()
    }

    // function for button clicks
    private fun setupClickListener() {
        binding.button.setOnClickListener {
            handleLogin()
        }

        binding.backIcon.setOnClickListener {
            navigateToWelcomePage()
        }
    }

    // log in verification
    private fun handleLogin() {
        val email = binding.emailEt.text.toString()
        val password = binding.passET.text.toString()

        if (areFieldsNotEmpty(email, password)) {
            signInWithEmailAndPassword(email, password)
        } else {
            showEmptyFieldsMessage()
        }
    }

    // empty fields
    private fun areFieldsNotEmpty(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }

    // function for logging in an account
    private fun signInWithEmailAndPassword(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                navigateToHomePage()
            } else {
                handleLoginFailure(task.exception)
            }
        }
    }

    // strips firebase exception errors
    private fun handleLoginFailure(exception: Exception?) {
        authToastLess.cancelToast() // Cancel any active Toast message since empty fields is determined first before auth exceptions
        val errorMessage = exception?.let { handleException(it) }
        if (errorMessage != null) {
            authToastLess.showToast(errorMessage)
        }
    }

    // response for empty fields
    private fun showEmptyFieldsMessage() {
        authToastLess.showToast("Fields cannot be empty!")
    }

    // start welcome page
    private fun navigateToWelcomePage() {
        super.onBackPressed()
    }

    // start logged in act
    private fun navigateToHomePage() {
        val intent = Intent(this, UserPetMatching::class.java)
        overridePendingTransition(R.anim.slide_up, R.anim.stay)
        startActivity(intent)
    }
}