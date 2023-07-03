package com.codingstuff.loginandsignup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.codingstuff.loginandsignup.databinding.ActivityProfilePageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfilePage : AppCompatActivity() {

    private lateinit var binding: ActivityProfilePageBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private val authToastLess = AuthToastLess(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseRef = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()

        val currentUser = firebaseAuth.currentUser
        val userId = currentUser?.uid

        userId?.let { retrieveUserData(it)

        binding.addPetProfileButton.setOnClickListener{
            val intent = Intent(this, AnimalProfileDataInputActivity::class.java)
            startActivity(intent)
        }}
    }

    // change the listener if there is time for settings
    private fun retrieveUserData(userId: String) {
        databaseRef.child("Users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var displayName = ""
                    for (snapshot in dataSnapshot.children) {
                        val userName = snapshot.value?.toString()
                        displayName += if (displayName.isNotEmpty()) " $userName" else userName
                    }
                    val displayNameTextView = binding.displayNameTextView
                    displayNameTextView.text = displayName
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    authToastLess.showToast("Data retrieval cancelled: ${databaseError.message}")
                }
            })
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}

