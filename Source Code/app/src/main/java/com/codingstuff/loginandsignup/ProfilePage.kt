package com.codingstuff.loginandsignup

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codingstuff.loginandsignup.databinding.ActivityProfilePageBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Suppress("DEPRECATION")
class ProfilePage : AppCompatActivity() {

    private lateinit var binding: ActivityProfilePageBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private val authToastLess = AuthToastLess(this)

    private val refreshInterval = 1.5 * 1000 // 10 secs in milliseconds -- refresh interval
    // handles automatic refresh
    private val refreshHandler = Handler()
    private val refreshRunnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.M)
        override fun run() {
            if (isNetworkConnected(this@ProfilePage)) {
                val currentUser = firebaseAuth.currentUser
                val userId = currentUser?.uid
                userId?.let { retrieveUserData(it) }
            }
            refreshHandler.postDelayed(this, refreshInterval.toLong())
        }
    }

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: ImageAdapter
    private lateinit var mUploads : List<ImageUpload>


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        setupClickListener()

        // RecyclerView set up
        mRecyclerView = binding.petProfilesRecyclerView
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = GridLayoutManager(this, 3)

        // list of items in recyclerview
        mUploads = mutableListOf()

        databaseRef = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()

        val currentUser = firebaseAuth.currentUser
        val userId = currentUser?.uid

        if (isNetworkConnected(this@ProfilePage)) {
            userId?.let { retrieveUserData(it) }
        } else {
            // Handle no internet connection case
            // Display an appropriate message or take necessary actions
            Toast.makeText(
                this,
                "There is a network connectivity issue. Please check your network.",
                Toast.LENGTH_LONG
            ).show()
        }

        // addValueEventListener - Add a listener for changes in the data at this location. Each time the data changes, your listener will be called with
        // an immutable snapshot of the data
        if (userId != null) {
            databaseRef.child("Users").child(userId).child("Animal Profiles Created").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val uploads = mutableListOf<ImageUpload>()

                    for (petSnapshot in dataSnapshot.children) {
                        val petCardId = petSnapshot.key
                        val name = petSnapshot.child("Name").getValue(String::class.java)
                        val profilePictureUrl = petSnapshot.child("Profile Picture").child("downloadUrl").getValue(String::class.java)

                        if (petCardId != null && name != null && profilePictureUrl != null) {
                            val upload = ImageUpload(profilePictureUrl, name)
                            uploads.add(upload)
                        }
                    }

                    mUploads = uploads
                    mAdapter = ImageAdapter(this@ProfilePage, mUploads)
                    mRecyclerView.adapter = mAdapter
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

        // change the listener if there is time for settings
    private fun retrieveUserData(userId: String) {
        val userRef = databaseRef.child("Users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val displayNameValue = dataSnapshot.child("Display Name").getValue(String::class.java)
                if (!displayNameValue.isNullOrEmpty()) {
                    runOnUiThread {
                        val displayNameTextView = binding.displayNameTextView
                        displayNameTextView.text = displayNameValue
                    }
                }
            }
            // think of possible errors - use toast
            override fun onCancelled(databaseError: DatabaseError) {
                // Runs the specified action on the UI thread. If the current thread is the UI thread, then the action is executed immediately. If the
                // current thread is not the UI thread, the action is posted to the event queue of the UI thread.
                runOnUiThread {
                    authToastLess.showToast("Data retrieval cancelled: ${databaseError.message}")
                }
            }
        })
    }

    // checks for network connectivity before retrieving form the database
    @RequiresApi(Build.VERSION_CODES.M)
    private fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val activeNetwork =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun setupClickListener() {
        binding.AddPetInfo.setOnClickListener {
            navigateToAddPetInfo()
        }
    }

    private fun navigateToAddPetInfo() {
        val intent = Intent(this, AddPetInformation::class.java)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        refreshHandler.postDelayed(refreshRunnable, refreshInterval.toLong())
    }

    override fun onStop() {
        super.onStop()
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}


