package com.codingstuff.loginandsignup

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.codingstuff.loginandsignup.databinding.ActivityProfilePageBinding
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
            if (isNetworkConnected()) {
                val currentUser = firebaseAuth.currentUser
                val userId = currentUser?.uid
                userId?.let { retrieveUserData(it) }
            }
            refreshHandler.postDelayed(this, refreshInterval.toLong())
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseRef = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()

        val currentUser = firebaseAuth.currentUser
        val userId = currentUser?.uid

        if (isNetworkConnected()) {
            userId?.let { retrieveUserData(it) }
        } else {
            // Handle no internet connection case
            // Display an appropriate message or take necessary actions
            Toast.makeText(this,"There is a network connectivity issue. Please check your network.", Toast.LENGTH_LONG).show()
        }

        binding.addPetProfileButton.setOnClickListener {
            val intent = Intent(this, AnimalProfileDataInputActivity::class.java)
            startActivity(intent)
        }
    }

    private fun retrieveUserData(userId: String) {
        databaseRef.child("Users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var displayName = ""
                    for (snapshot in dataSnapshot.children) {
                        val userName = snapshot.value?.toString()
                        if (!userName.isNullOrEmpty()) {
                            displayName = userName
                        }
                    }
                    runOnUiThread {
                        val displayNameTextView = binding.displayNameTextView
                        displayNameTextView.text = displayName
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    runOnUiThread {
                        authToastLess.showToast("Data retrieval cancelled: ${databaseError.message}")
                    }
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
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
