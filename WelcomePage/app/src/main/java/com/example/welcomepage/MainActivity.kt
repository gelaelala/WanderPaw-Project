package com.example.welcomepage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    val loginActbtn = findViewById<Button>(R.id.login_button)
    val signupActbtn = findViewById<Button>(R.id.signup_button)

    loginActbtn.setOnClickListener{

    }

    signupActbtn.setOnClickListener{

    }
}