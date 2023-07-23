package com.codingstuff.loginandsignup

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
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
    private var firebaseAuth: FirebaseAuth? = null
    private lateinit var databaseRef: DatabaseReference
    private val authToastLess = AuthToastLess(this)

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
                    overridePendingTransition(R.anim.stay, R.anim.stay)
                    finish()
                    true
                }
                R.id.AddPetInformation -> {
                    startActivity(Intent(applicationContext, AddPetInformation::class.java))
                    finish()
                    overridePendingTransition(R.anim.slide_up, R.anim.stay)
                    true
                }

                else -> false
            }
        }

        binding.settingsButton.setOnClickListener{
            val message = "What do you want to do?"
            message.showSettings()
        }


        // RecyclerView set up
        mRecyclerView = binding.petProfilesRecyclerView
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = GridLayoutManager(this, 3)

        // list of items in recyclerview
        mUploads = mutableListOf()

        databaseRef = FirebaseDatabase.getInstance().reference
        this.firebaseAuth = FirebaseAuth.getInstance()

        val currentUser = firebaseAuth!!.currentUser
        val userId = currentUser?.uid

        userId?.let { retrieveUserData(it)}

        // addValueEventListener - Add a listener for changes in the data at this location. Each time the data changes, your listener will be called with
        // an immutable snapshot of the data
        if (userId != null) {
            databaseRef.child("Users").child(userId).child("Animal Profiles Created").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    (mUploads as MutableList<ImageUpload>).clear()

                    for (petSnapshot in dataSnapshot.children) {
                        val petCardId = petSnapshot.key
                        val name = petSnapshot.child("Name").getValue(String::class.java)
                        val profilePictureUrl = petSnapshot.child("Profile Picture").child("downloadUrl").getValue(String::class.java)

                        if ((petCardId != null) && (name != null) && (profilePictureUrl != null)) {
                            val upload = ImageUpload(profilePictureUrl, name, petCardId)
                            (mUploads as MutableList<ImageUpload>).add(upload)
                        }
                    }

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

    private fun String?.showSettings() {
        val dialog = Dialog(this@ProfilePage)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.activity_settings)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvMessage : TextView = dialog.findViewById(R.id.tvMessage)
        val buttonEdit : Button = dialog.findViewById(R.id.editProfile)
        val buttonLogout : Button = dialog.findViewById(R.id.logOutButton)

        tvMessage.text = this

        buttonLogout.setOnClickListener{
            val reminder = "Are you sure you want to end your session with this device?"
            reminder.showLogout()
        }

        dialog.show()
    }

    private fun String?.showLogout() {
        val dialog = Dialog(this@ProfilePage)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.logout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val messageTV : TextView = dialog.findViewById(R.id.messageTV)
        val yesButton : Button = dialog.findViewById(R.id.yesButton)
        val noButton : Button = dialog.findViewById(R.id.noButton)

        messageTV.text = this

        yesButton.setOnClickListener{
            firebaseAuth?.signOut()
            startActivity(Intent(this@ProfilePage, LogInActivity::class.java))
            finish()
        }

        noButton.setOnClickListener{
            dialog.dismiss()
        }

        dialog.show()
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

    // starting logged in page code
    private fun navigateToHomePage() {
        overridePendingTransition(R.anim.stay, R.anim.stay)
        val intent = Intent(this, UserPetMatching::class.java)
        startActivity(intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        navigateToHomePage()
    }
}

