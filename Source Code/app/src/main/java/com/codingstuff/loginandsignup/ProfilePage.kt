package com.codingstuff.loginandsignup

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.codingstuff.loginandsignup.databinding.ActivityProfilePageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

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

        setupClickListener()

        databaseRef = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()
        auth = Firebase.auth

        val currentUser = firebaseAuth.currentUser
        val userId = currentUser?.uid

        if (isNetworkConnected()) {
            userId?.let { retrieveUserData(it) }
        } else {
            // Handle no internet connection case
            // Display an appropriate message or take necessary actions
            Toast.makeText(this,"There is a network connectivity issue. Please check your network.", Toast.LENGTH_LONG).show()
        }


        binding.settingsBtn.setOnClickListener{
            val message : String? = "What do you want to do?"
            showSettings(message)
        }

    }

    private fun showSettings(message: String?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.activity_settings)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvMessage : TextView = dialog.findViewById(R.id.tvMessage)
        val btnEdit : Button = dialog.findViewById(R.id.editProfile)
        val btnLogout : Button = dialog.findViewById(R.id.logout_btn)

        tvMessage.text = message

        btnLogout.setOnClickListener{
            showLogout(message)
        }

        dialog.show()
    }

    private fun showLogout(message: String?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.logout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val messagetv : TextView = dialog.findViewById(R.id.Messagetv)
        val yesbtn : Button = dialog.findViewById(R.id.yes_btn)
        val nobtn : Button = dialog.findViewById(R.id.no_btn)

        messagetv.text = message

        yesbtn.setOnClickListener{
            firebaseAuth.signOut()
            startActivity(Intent(this, LogInActivity::class.java))
            finish()
        }

        nobtn.setOnClickListener{
            dialog.dismiss()
        }

        dialog.show()
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
    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    private fun setupClickListener() {
        binding.AddPetInfo  .setOnClickListener {
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


