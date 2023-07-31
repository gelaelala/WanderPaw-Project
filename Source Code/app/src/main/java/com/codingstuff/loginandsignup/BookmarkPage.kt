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
    private lateinit var nUploads : List<CardUpload>

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
        nUploads = mutableListOf()

        databaseRef = FirebaseDatabase.getInstance().reference
        this.firebaseAuth = FirebaseAuth.getInstance()

        val currentUser = firebaseAuth!!.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            databaseRef.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    (nUploads as MutableList<CardUpload>).clear() // Clear the list to avoid duplicate data

                    // Loop through each user node
                    for (userSnapshot in dataSnapshot.children) {
                        // Get the user's ID
                        val userIds = userSnapshot.key

                        // Get the "Bookmarked Pet Profiles" node of the user
                        val bookmarkedPetProfilesNode = userSnapshot.child("Bookmarked Pet Profiles")

                        // Loop through each child node under "Bookmarked Pet Profiles"
                        for (petSnapshot in bookmarkedPetProfilesNode.children) {
                            // Get the key of the child node (excluding "button_state")
                            val petCardId = petSnapshot.key

                            // Check if the "button_state" child is true
                            val buttonState = petSnapshot.child("button_state").getValue(Boolean::class.java)

                            if (buttonState == true) {
                                // Find matches in "Animal Profiles Created" of the current user
                                val animalProfilesNode = userSnapshot.child("Animal Profiles Created")
                                val petProfileNode = petCardId?.let { animalProfilesNode.child(it) }

                                // Retrieve necessary data from "Animal Profiles Created" node
                                val name = petProfileNode?.child("Name")?.getValue(String::class.java)
                                    .toString()
                                val gender = petProfileNode?.child("Gender")?.getValue(String::class.java)
                                    .toString()
                                val age = petProfileNode?.child("Age")?.getValue(String::class.java)
                                    .toString()
                                val genderAge = "($gender, $age)"
                                val location = petProfileNode?.child("Location")?.getValue(String::class.java)
                                    .toString()
                                val bio = petProfileNode?.child("Bio")?.getValue(String::class.java)
                                    .toString()
                                val profilePictureUrl = petProfileNode?.child("Profile Picture")
                                    ?.child("downloadUrl")?.getValue(String::class.java)

                                if (petCardId != null && profilePictureUrl != null) {
                                    val upload = userIds?.let {
                                        CardUpload(
                                            profilePictureUrl, name, genderAge,
                                            location, bio, it, petCardId
                                        )
                                    }
                                    if (upload != null) {
                                        (nUploads as MutableList<CardUpload>).add(upload)
                                    }
                                }
                            }
                        }
                    }

                    oAdapter = CardAdapter(this@BookmarkPage, nUploads, this@BookmarkPage)
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
}