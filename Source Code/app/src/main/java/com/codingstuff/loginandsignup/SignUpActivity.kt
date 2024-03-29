package com.codingstuff.loginandsignup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.codingstuff.loginandsignup.AuthExceptionHandler.Companion.handleException
import com.codingstuff.loginandsignup.databinding.ActivitySignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale



class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient
    private val authToastLess = AuthToastLess(this)
    private val authExceptionHandler = AuthExceptionHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseRef = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()

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
            handleSignUp()
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
            handleSignUpFailure(task.exception)
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
                    val photoUrl = account.photoUrl?.toString() ?: ""
                    // Save user information to the Realtime Database
                    saveUserToDatabase(user.uid, displayName, photoUrl)
                    navigateToHomePage()
                } else {
                    handleSignUpFailure(it.exception)
                }
            } else {
                handleSignUpFailure(it.exception)

            }
        }
    }

    private fun saveUserToDatabase(userId: String, displayName: String, photoUrl: String) {
        val userRef = databaseRef.child("Users").child(userId)

        // Save user information to the database
        userRef.child("Display Name").setValue(displayName)
        userRef.child("User Profile Picture").child("downloadUrl").setValue(photoUrl)
    }

    private fun formatStringWithCapital(input: String): String {
        return input.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            .trim()
    }

    private fun handleSignUp() {
        val firstName = formatStringWithCapital(binding.firstNameET.text.toString())
        val lastName = formatStringWithCapital(binding.lastNameET.text.toString())
        val displayName = "$firstName $lastName"
        val email = binding.emailEt.text.toString().trim()
        val password = binding.passET.text.toString().trim()
        val confirmPassword = binding.confirmPassEt.text.toString().trim()
        val missingConditions = authExceptionHandler.validatePassword(password)

        if (areFieldsNotEmpty(firstName, lastName, email, password, confirmPassword)) {
            if (isPasswordMatch(password, confirmPassword)) {
                if (missingConditions.isEmpty()) {
                    createUserWithEmailAndPassword(email, password, displayName)
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
    private fun createUserWithEmailAndPassword(email: String, password: String, displayName: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = firebaseAuth.currentUser
                    val userId = currentUser?.uid

                    // Create a HashMap to store user data
                    val userData = HashMap<String, Any>().apply {
                        put("Display Name", displayName)
                    }

                    // Push the user data to the "Users" node
                    userId?.let {
                        databaseRef.child("Users").child(it).updateChildren(userData)
                            .addOnCompleteListener { databaseTask ->
                                if (databaseTask.isSuccessful) {
                                    navigateToHomePage()
                                }
                            }
                    }
                } else {
                    handleSignUpFailure(task.exception)
                }
            }
    }


    // function for stripping firebase exceptions
    private fun handleSignUpFailure(exception: Exception?) {
        authToastLess.cancelToast() // Cancel any active Toast message since empty fields and password mismatch are determined first before auth exceptions
        val errorMessage = exception?.let { handleException(it) }
        if (errorMessage != null) {
            authToastLess.showToast(errorMessage)
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
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }

    // starting logged in page code
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