package com.codingstuff.loginandsignup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.codingstuff.loginandsignup.AuthExceptionHandler.handleException
import com.codingstuff.loginandsignup.AuthExceptionHandler.validatePassword
import com.codingstuff.loginandsignup.databinding.ActivitySignUpBinding
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.button.setOnClickListener{
            val firstName = binding.firstNameET.text.toString()
            val lastName = binding.lastNameET.text.toString()
            val email = binding.emailEt.text.toString()
            val password = binding.passET.text.toString()
            val confirmPassword = binding.confirmPassEt.text.toString()
            val missingConditions = validatePassword(password)

            if (firstName.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()){
                if (password == confirmPassword) {
                    if (missingConditions.isEmpty()) {

                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    when (val exception = it.exception) {
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
                            }
                    } else {
                        val weakPwd = "Weak password. The following conditions are missing: ${missingConditions.joinToString(", ")}"
                        Toast.makeText(this, weakPwd, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Password does not match!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.backIcon.setOnClickListener{
            val intent = Intent(this, WelcomePage::class.java)
            startActivity(intent)
        }
    }
}