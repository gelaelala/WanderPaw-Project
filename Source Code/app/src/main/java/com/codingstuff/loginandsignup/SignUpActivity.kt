package com.codingstuff.loginandsignup

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.codingstuff.loginandsignup.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

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
        }
        binding.backIcon.setOnClickListener{
            val backIntent = Intent(this, WelcomePage::class.java)
            startActivity(backIntent)
        }
    }
}