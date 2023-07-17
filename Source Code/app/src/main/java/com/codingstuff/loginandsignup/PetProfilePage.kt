package com.codingstuff.loginandsignup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.codingstuff.loginandsignup.databinding.ActivityPetProfilePageBinding
import com.codingstuff.loginandsignup.databinding.ActivityProfilePageBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class PetProfilePage : AppCompatActivity() {

    private lateinit var binding: ActivityPetProfilePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPetProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListener()

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

    private fun setupClickListener() {
        binding.BackButton.setOnClickListener{
            navigatetoUserProfile()
        }
    }

    private fun navigatetoUserProfile() {
        val intent = Intent(this, ProfilePage::class.java)
        startActivity(intent)
    }
}