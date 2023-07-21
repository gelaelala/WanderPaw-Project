package com.codingstuff.loginandsignup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView

class UserPetMatching : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_pet_matching)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.UserPetMatching

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.UserPetMatching -> true
                R.id.UserProfile -> {
                    startActivity(Intent(applicationContext, ProfilePage::class.java))
                    finish()
                    true
                }
                R.id.AddPetInformation -> {
                    startActivity(Intent(applicationContext, AddPetInformation::class.java))
                    finish()
                    true
                }

                else -> false
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}