package com.example.welcomepage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginActbtn = findViewById<Button>(R.id.login_button)
        val signupActbtn = findViewById<Button>(R.id.signup_button)

        loginActbtn.setOnClickListener{
            val Intent = Intent(this, Login::class.java )
        }

        signupActbtn.setOnClickListener{
            val Intent = Intent(this, Login::class.java )
        }
    }

}