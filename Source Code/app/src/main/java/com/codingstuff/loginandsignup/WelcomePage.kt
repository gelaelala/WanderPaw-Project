package com.codingstuff.loginandsignup

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.Button
import com.codingstuff.loginandsignup.databinding.ActivitySignUpBinding
import com.codingstuff.loginandsignup.databinding.ActivityWelcomePageBinding

class WelcomePage : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
            intent.putExtra("key", "login_button")
            overridePendingTransition(R.anim.slide_up, R.anim.push_out)
        }

        binding.signupButton.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            intent.putExtra("key", "signup_button")
            overridePendingTransition(R.anim.slide_up, R.anim.push_out)
        }
    }

}