package com.codingstuff.loginandsignup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.codingstuff.loginandsignup.AuthExceptionHandler.Companion.handleException
import com.codingstuff.loginandsignup.AuthExceptionHandler.Companion.validatePassword
import com.codingstuff.loginandsignup.databinding.ActivitySignUpBinding
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private val authToastLess = AuthToastLess(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseRef = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()

        setupClickListener()
    }

    // function for button clicks
    private fun setupClickListener() {
        binding.button.setOnClickListener {
            handleSignUp()
        }

        binding.backIcon.setOnClickListener {
            navigateToWelcomePage()
        }
    }

    private fun handleSignUp() {
//        val firstName = binding.firstNameET.text.toString()
//            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
//            .trim()
        val lastName = binding.lastNameET.text.toString().trim()
        val email = binding.emailEt.text.toString().trim()
        val password = binding.passET.text.toString().trim()
        val confirmPassword = binding.confirmPassEt.text.toString().trim()
        val missingConditions = validatePassword(password)

        if (areFieldsNotEmpty(firstName, lastName, email, password, confirmPassword)) {
            if (isPasswordMatch(password, confirmPassword)) {
                if (missingConditions.isEmpty()) {
                    createUserWithEmailAndPassword(email, password, firstName, lastName)
                } else {
                    showWeakPasswordMessage(missingConditions)
                }
            } else {
                showPasswordMismatchMessage()
            }
        } else {
            showEmptyFieldsMessage()
        }
    }

    // function for empty field checking
    private fun areFieldsNotEmpty(firstName: String, lastName: String, email: String, password: String, confirmPassword: String): Boolean {
        return firstName.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()
    }

    // function for password matching
    private fun isPasswordMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    // Function for account creation
    private fun createUserWithEmailAndPassword(email: String, password: String, firstName: String, lastName: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = firebaseAuth.currentUser
                    val userId = currentUser?.uid

                    // Create a HashMap to store user data
                    val userData = HashMap<String, Any>()
                    userData["First Name"] = firstName
                    userData["Last Name"] = lastName

                    // Push the user data to the "Users" node
                    userId?.let {
                        databaseRef.child("Users").child(it).updateChildren(userData)
                            .addOnCompleteListener { databaseTask ->
                                if (databaseTask.isSuccessful) {
                                    navigateToMainActivity()
                                }
                            }
                    }
                } else {
                    handleSignUpFailure(task.exception)
                }
            }
    }


    // function for stripping firebase exceptions
    fun handleSignUpFailure(exception: Exception?) {
        authToastLess.cancelToast() // Cancel any active Toast message since empty fields and password mismatch are determined first before auth exceptions
        when (exception) {
            is FirebaseAuthException -> {
                val errorMessage = handleException(exception)
                authToastLess.showToast(errorMessage)
            }
            is FirebaseNetworkException -> {
                authToastLess.showToast("There is a network connectivity issue. Please check your network.")
            }
            is FirebaseTooManyRequestsException -> {
                authToastLess.showToast("Too many requests. Try again later.")
            }
        }
    }

    // response for password strength testing
    private fun showWeakPasswordMessage(missingConditions: List<String>) {
        authToastLess.cancelToast() // Cancel any active Toast message since empty fields and password mismatch are determined first before auth exceptions
        val weakPwd = "Weak password. The following conditions are missing: ${missingConditions.joinToString(", ")}"
        authToastLess.showToast(weakPwd)
    }

    // response for password mismatch
    private fun showPasswordMismatchMessage() {
        authToastLess.cancelToast() // Cancel any active Toast message since empty fields is determined first before auth exceptions
        authToastLess.showToast("Password does not match!")
    }

    // response for empty fields
    private fun showEmptyFieldsMessage() {
        authToastLess.showToast("Fields cannot be empty!")
    }

    // starting welcome page code
    private fun navigateToWelcomePage() {
        val intent = Intent(this, WelcomePage::class.java)
        startActivity(intent)
    }

    // starting logged in page code
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}