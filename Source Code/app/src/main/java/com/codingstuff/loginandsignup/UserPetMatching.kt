package com.codingstuff.loginandsignup

import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codingstuff.loginandsignup.databinding.ActivityUserPetMatchingBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserPetMatching : AppCompatActivity() {

    private lateinit var binding: ActivityUserPetMatchingBinding
    private var firebaseAuth: FirebaseAuth? = null
    private lateinit var databaseRef: DatabaseReference
    private val authToastLess = AuthToastLess(this)

    private lateinit var nRecyclerView: RecyclerView
    private lateinit var nAdapter: CardAdapter
    private lateinit var nUploads : List<CardUpload>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserPetMatchingBinding.inflate(layoutInflater)
        setContentView(binding.root)


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

        // RecyclerView set up
        nRecyclerView = binding.petProfilesList
        nRecyclerView.setHasFixedSize(true)
        nRecyclerView.layoutManager = LinearLayoutManager(this)

        // list of items in recyclerview
        nUploads = mutableListOf()

        databaseRef = FirebaseDatabase.getInstance().reference
        this.firebaseAuth = FirebaseAuth.getInstance()

        val currentUser = firebaseAuth!!.currentUser
        val userId = currentUser?.uid

        // addValueEventListener - Add a listener for changes in the data at this location. Each time the data changes, your listener will be called with
        // an immutable snapshot of the data
        if (userId != null) {
            databaseRef.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    (nUploads as MutableList<CardUpload>).clear() // Clear the list to avoid duplicate data
                    for (userSnapshot in dataSnapshot.children) {
                        val userIdSnapshot = userSnapshot.key
                        val animalProfilesSnapshot = userSnapshot.child("Animal Profiles Created")
                        for (petSnapshot in animalProfilesSnapshot.children) {
                            val petCardId = petSnapshot.key
                            val name = petSnapshot.child("Name").getValue(String::class.java).toString()
                            val gender = petSnapshot.child("Gender").getValue(String::class.java).toString()
                            val age = petSnapshot.child("Age").getValue(String::class.java).toString()
                            val genderAge = "($gender,$age)"
                            val location = petSnapshot.child("Location").getValue(String::class.java).toString()
                            val bio = petSnapshot.child("Bio").getValue(String::class.java).toString()
                            val profilePictureUrl =
                                petSnapshot.child("Profile Picture").child("downloadUrl")
                                    .getValue(String::class.java)

                            if (petCardId != null && profilePictureUrl != null) {
                                val upload = userIdSnapshot?.let {
                                    CardUpload(profilePictureUrl, name, genderAge,
                                        location, bio, it, petCardId)
                                }
                                if (upload != null) {
                                    (nUploads as MutableList<CardUpload>).add(upload)
                                }
                            }
                        }
                    }

                        nAdapter = CardAdapter(this@UserPetMatching, nUploads)
                        nRecyclerView.adapter = nAdapter
                    }


                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle the database error
                        runOnUiThread {
                            authToastLess.showToast("Data retrieval cancelled: ${databaseError.message}")
                        }
                    }
                })
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}


