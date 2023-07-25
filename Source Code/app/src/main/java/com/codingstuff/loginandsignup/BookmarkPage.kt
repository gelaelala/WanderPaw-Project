package com.codingstuff.loginandsignup

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codingstuff.loginandsignup.databinding.ActivityBookmarkPageBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BookmarkPage : AppCompatActivity() {

    private lateinit var binding: ActivityBookmarkPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark_page)

        binding = ActivityBookmarkPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.UserProfile

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.UserProfile -> {
                    startActivity(Intent(applicationContext, ProfilePage::class.java))
                    overridePendingTransition(R.anim.stay, R.anim.stay)
                    finish()
                    true
                }

                R.id.UserPetMatching -> {
                    startActivity(Intent(applicationContext, UserPetMatching::class.java))
                    overridePendingTransition(R.anim.stay, R.anim.stay)
                    finish()
                    true
                }

                R.id.AddPetInformation -> {
                    startActivity(Intent(applicationContext, AddPetInformation::class.java))
                    overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
                    finish()
                    true
                }

                else -> false
            }
        }

        binding.BackButton.setOnClickListener {
            navigateToUserProfile()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()
        ConnectivityUtils.registerConnectivityCallback(this)
    }

    override fun onStop() {
        super.onStop()
        ConnectivityUtils.unregisterConnectivityCallback()
    }

    private fun navigateToUserProfile() {
        val intent = Intent(this, ProfilePage::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.stay, R.anim.stay)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        navigateToUserProfile()
    }
}