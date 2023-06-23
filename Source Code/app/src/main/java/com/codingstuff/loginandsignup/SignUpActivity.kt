package com.codingstuff.loginandsignup

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.codingstuff.loginandsignup.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()


        firebaseAuth = FirebaseAuth.getInstance()
        binding.button.setOnClickListener{
            val firstName = binding.firstNameET.text.toString()
            val lastName = binding.lastNameET.text.toString()
            val email = binding.emailEt.text.toString()
            val password = binding.passET.text.toString()
            val confirmPassword = binding.confirmPassEt.text.toString()

            if (firstName.isEmpty()) {
                //check if first name is empty.
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
                //this line simply states that no other action should happen unless the first name editText field has been filled
            }

            if (lastName.isEmpty()) {
                //check if last name is empty.
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
                //this line simply states that no other action should happen unless the lastname editText field has been filled
            }

            if (email.isEmpty()) {
                //check if email is empty.
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                binding.emailEt.requestFocus()
                return@setOnClickListener
                //this line simply states that no other action should happen unless the email editText field has been filled
            }

            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                //check if the email entered matches a normal email address pattern
                Toast.makeText(this, "Email Invalid", Toast.LENGTH_SHORT).show()
                binding.emailEt.requestFocus()
                return@setOnClickListener
                //this line simply states that no other action should happen unless the value in email editText matches an email address pattern
            }

            if(password.isEmpty()){
                //check if the password editText field is empty
                Toast.makeText(this, "Enter a password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
                //this line simply states that no other action should happen unless the password editText field has been filled
            }

            if(password.length<6){
                //check if the value in password editText filed is less than 6 characters
                Toast.makeText(this, " Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
                //this line simply states that no other action should happen unless the value in password editText field is more than 6 characters
            }

            if (!password.matches(".*[A-Z].*".toRegex())){
                //check if the password editText filled has an uppercase letter
                Toast.makeText(this, " Password must have uppercase letter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
                //this line simply states that no other action should happen unless the password editText filled has an uppercase letter
            }

            if (!password.matches(".*[a-z].*".toRegex())){
                //check if the password editText filled has a lowercase letter
                Toast.makeText(this, " Password must have lowercase letter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
                //this line simply states that no other action should happen unless password editText filled has a lowercase letter
            }

            if (!password.matches(".*[@#\$%^&+=].*".toRegex())){
                //check if the password editText filled has a special character
                Toast.makeText(this, " Password must have special case character", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
                //this line simply states that no other action should happen unless password editText filled has a special character
            }

            if (confirmPassword.isEmpty()){
                //check if the confirm password editText filled has a special character
                Toast.makeText(this, "Confirm password has to be provided", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
                //this line simply states that no other action should happen unless the confirm password editText field is filled
            }

            if (!confirmPassword.equals(password)){
                //check if value in confirmPassword equals value in password
                Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show()
            }
        }
        binding.backIcon.setOnClickListener{
            val backIntent = Intent(this, WelcomePage::class.java)
            startActivity(backIntent)
        }
    }
    private fun registerFirebase(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this){
            //This line signifies that we use the firebase instance to create user with email and password
            if(it.isSuccessful) {
                // User creation successful
                Toast.makeText(this, "Successfully Signed Up", Toast.LENGTH_SHORT).show()
            }
        }
    }
}