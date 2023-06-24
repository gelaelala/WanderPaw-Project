package com.codingstuff.loginandsignup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val password = binding.passET.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {

                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        when (val exception = it.exception) {
                            is FirebaseAuthException -> {
                                val errorMessage = AuthExceptionHandler.handleException(exception)
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
                }
            } else {
                Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.backIcon.setOnClickListener {
            val intent = Intent(this, WelcomePage::class.java)
            startActivity(intent)
        }
    }
}