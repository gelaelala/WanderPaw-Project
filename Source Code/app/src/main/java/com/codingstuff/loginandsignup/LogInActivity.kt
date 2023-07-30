package com.codingstuff.loginandsignup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.codingstuff.loginandsignup.AuthExceptionHandler.Companion.handleException
import com.codingstuff.loginandsignup.databinding.ActivityLogInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient
    private val authToastLess = AuthToastLess(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

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

        binding.googleButton.setOnClickListener {
            signInGoogle()

        }
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
                if (result.resultCode == Activity.RESULT_OK) {

                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    handleResults(task)
                }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account : GoogleSignInAccount = task.result
            updateUI(account)
        } else {
            handleLoginFailure(task.exception)
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                val user = firebaseAuth.currentUser
                if (user != null) {
                    // Access the necessary user information from the GoogleSignInAccount
                    val displayName = account.displayName ?: ""
                    // Save user information to the Realtime Database
                    saveUserToDatabase(user.uid, displayName)
                    navigateToHomePage()
                } else {
                    handleLoginFailure(it.exception)
                }
            } else {
                handleLoginFailure(it.exception)

            }
        }
    }



    private fun saveUserToDatabase(userId: String, displayName: String) {
        val userRef = databaseRef.child("Users").child(userId)

        // Save user information to the database
        userRef.child("Display Name").setValue(displayName)
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
        val intent = Intent(this, WelcomePage::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }

    // start logged in act
    private fun navigateToHomePage() {
        val intent = Intent(this, UserPetMatching::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        navigateToWelcomePage()
    }
}