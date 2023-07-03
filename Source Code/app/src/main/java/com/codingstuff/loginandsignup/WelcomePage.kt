package com.codingstuff.loginandsignup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.codingstuff.loginandsignup.databinding.ActivityWelcomePageBinding
import com.google.firebase.auth.FirebaseAuth

class WelcomePage : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomePageBinding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener {
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up, R.anim.push_out)
        }

        binding.signupButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up, R.anim.push_out)
        }
    }

    override fun onStart() {
        super.onStart()

//        temporary page
        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this, ProfilePage::class.java)
            startActivity(intent)
        }
    }
}