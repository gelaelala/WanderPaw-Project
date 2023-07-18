package com.codingstuff.loginandsignup

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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

class ProfilePage : AppCompatActivity() {

    private lateinit var binding: ActivityProfilePageBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private val authToastLess = AuthToastLess(this)

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

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

        // Initialize the ConnectivityManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val currentUser = firebaseAuth.currentUser
        val userId = currentUser?.uid

        // Create a NetworkCallback to handle network connectivity changes
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Network connection is available
                userId?.let { retrieveUserData(it) }
            }

            override fun onLost(network: Network) {
                // Network connection is lost
                // You can handle any necessary actions here
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "There is a network connectivity issue. Please check your network.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

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
                            val upload = ImageUpload(profilePictureUrl, name, petCardId)
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

    private fun setupClickListener() {
        binding.AddPetInfo.setOnClickListener {
            navigateToAddPetInfo()
        }
    }
    private fun navigateToAddPetInfo() {
        val intent = Intent(this, AddPetInformation::class.java)
        startActivity(intent)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()
        registerConnectivityCallback()
    }

    override fun onStop() {
        super.onStop()
        unregisterConnectivityCallback()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun registerConnectivityCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    private fun unregisterConnectivityCallback() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}


