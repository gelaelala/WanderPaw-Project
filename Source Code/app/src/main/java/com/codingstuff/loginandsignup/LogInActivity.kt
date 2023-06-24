package com.codingstuff.loginandsignup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.codingstuff.loginandsignup.AuthExceptionHandler.handleException
import com.codingstuff.loginandsignup.databinding.ActivityLogInBinding
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    private lateinit var firebaseAuth: FirebaseAuth

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
                navigateToMainActivity()
            } else {
                handleLoginFailure(task.exception)
            }
        }
    }

    // strips firebase exception errors
    private fun handleLoginFailure(exception: Exception?) {
        when (exception) {
            is FirebaseAuthException -> {
                val errorMessage = handleException(exception)
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
            is FirebaseNetworkException -> {
                Toast.makeText(
                    this,
                    "There is a network connectivity issue. Please check your network.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            is FirebaseTooManyRequestsException -> {
                Toast.makeText(this, "Too many requests. Try again later.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // response for empty fields
    private fun showEmptyFieldsMessage() {
        Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show()
    }

    // start welcome page
    private fun navigateToWelcomePage() {
        val intent = Intent(this, WelcomePage::class.java)
        startActivity(intent)
    }

    // start logged in act
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}