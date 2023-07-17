package com.codingstuff.loginandsignup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView

class PetProfilePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_profile_page)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.UserProfile

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.UserProfile -> true
                R.id.UserPetMatching -> {
                    startActivity(Intent(applicationContext, UserPetMatching::class.java))
                    finish()
                    true
                }

                else -> false
            }
        }
    }
}