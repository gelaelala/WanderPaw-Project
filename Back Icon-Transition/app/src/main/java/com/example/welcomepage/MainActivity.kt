package com.example.welcomepage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.view.animation.AnimationUtils


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginActbtn = findViewById<Button>(R.id.login_button)
        val signupActbtn = findViewById<Button>(R.id.signup_button)

        loginActbtn.setOnClickListener{
            val i = Intent(this, Login::class.java )
            startActivity(i)
            i.putExtra("key", "loginBTN")
            overridePendingTransition(R.anim.slide_up, R.anim.push_out)
        }

        signupActbtn.setOnClickListener{
            val i = Intent(this, Login::class.java )
            i.putExtra("key", "signupBTN")
            startActivity(i)
            overridePendingTransition(R.anim.slide_up, R.anim.push_out)
        }
    }

}

