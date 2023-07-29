package com.codingstuff.loginandsignup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.codingstuff.loginandsignup.AuthExceptionHandler.Companion.handleException
import com.codingstuff.loginandsignup.databinding.ActivityLogInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val authToastLess = AuthToastLess(this)
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference

        setupClickListener()

        // Configure Google Sign-In options
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

    }
    // function for button clicks
    private fun setupClickListener() {
        binding.button.setOnClickListener {
            handleLogin()
        }

        binding.backIcon.setOnClickListener {
            navigateToWelcomePage()
        }

        binding.googleBtn.setOnClickListener{
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val displayName = getDisplayNameFromAccount(account)
                firebaseAuthWithGoogle(account.idToken!!, displayName)
            }catch (e: ApiException){
                Toast.makeText(this, "Google Sign In failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDisplayNameFromAccount(account: GoogleSignInAccount): String {
        val givenName = account.givenName ?: ""
        val familyName = account.familyName ?: ""
        return "$givenName $familyName"
    }

    private fun firebaseAuthWithGoogle(idToken: String, displayName: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful){
                    val user = firebaseAuth.currentUser
                    if(user!=null){
                        val databaseRef = FirebaseDatabase.getInstance().reference
                        val userRef = user?.let { databaseRef.child("Users").child(it.uid) }
                        if (userRef != null) {
                            userRef.child("Display Name").setValue(displayName)
                                .addOnSuccessListener {
                                    // Name stored in the database successfully
                                    navigateToHomePage()
                                }
                                .addOnFailureListener { exception ->
                                    // Error occurred while storing the name
                                    Toast.makeText(this, "Error storing name in the database", Toast.LENGTH_SHORT).show()
                                }
                        }else{
                            handleLoginFailure(task.exception)
                        }
                    }
                }else{
                    handleLoginFailure(task.exception)
                }
            }
    }

    companion object{
        const val RC_SIGN_IN = 1001
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

    private fun navigateToLoginPage() {
        val intent = Intent(this, LogInActivity::class.java)
        overridePendingTransition(R.anim.stay, R.anim.stay)
        startActivity(intent)
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