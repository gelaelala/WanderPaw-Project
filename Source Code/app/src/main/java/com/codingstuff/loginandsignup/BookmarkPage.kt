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

class BookmarkPage : AppCompatActivity(), CardAdapter.OnItemClickListener  {

    private lateinit var binding: ActivityBookmarkPageBinding
    private var firebaseAuth: FirebaseAuth? = null
    private lateinit var databaseRef: DatabaseReference
    private val authToastLess = AuthToastLess(this)

    private lateinit var oRecyclerView: RecyclerView
    private lateinit var oAdapter: CardAdapter
    private lateinit var oUploads : List<CardUpload>

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

        // RecyclerView set up
        oRecyclerView = binding.petProfilesList
        oRecyclerView.setHasFixedSize(true)
        oRecyclerView.layoutManager = LinearLayoutManager(this)

        // list of items in recyclerview
        oUploads = mutableListOf()

        databaseRef = FirebaseDatabase.getInstance().reference
        this.firebaseAuth = FirebaseAuth.getInstance()


        val currentUser = firebaseAuth!!.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            databaseRef.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    (oUploads as MutableList<CardUpload>).clear() // Clear the list to avoid duplicate data
                    for (userSnapshot in dataSnapshot.children) {
                        val userIdSnapshot = userSnapshot.key
                        val selectedPetProfiles = userSnapshot.child("Bookmarked Pet Profiles")

                        val animalProfilesSnapshot = userSnapshot.child("Animal Profiles Created")
                        for (petSnapshot in animalProfilesSnapshot.children) {
                            val petCardId = petSnapshot.key
                            val name = petSnapshot.child("Name").getValue(String::class.java).toString()
                            val gender = petSnapshot.child("Gender").getValue(String::class.java).toString()
                            val age = petSnapshot.child("Age").getValue(String::class.java).toString()
                            val genderAge = "($gender, $age)"
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
                                    (oUploads as MutableList<CardUpload>).add(upload)
                                }
                            }
                        }
                    }

                    oAdapter = CardAdapter(this@BookmarkPage, oUploads, this@BookmarkPage)
                    oRecyclerView.adapter = oAdapter
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

    override fun onItemClick(userId: String, petCardId: String) {
        startFullPetProfilePageActivity(userId, petCardId)
    }


    private fun startFullPetProfilePageActivity(userId: String, petCardId: String) {
        val intent = Intent(this, FullPetProfilePage::class.java)
        intent.putExtra("petCardId", petCardId)
        intent.putExtra("userId", userId)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        finish()
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

    override fun onItemClick(userId: String, petCardId: String) {
        TODO("Not yet implemented")
    }
}