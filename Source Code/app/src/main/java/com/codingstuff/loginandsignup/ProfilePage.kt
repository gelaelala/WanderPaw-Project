package com.codingstuff.loginandsignup

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class ProfilePage : AppCompatActivity(), ImageAdapter.OnItemClickListener {

    private lateinit var binding: ActivityProfilePageBinding
    private var firebaseAuth: FirebaseAuth? = null
    private lateinit var databaseRef: DatabaseReference
    private val authToastLess = AuthToastLess(this)

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: ImageAdapter
    private lateinit var mUploads : List<ImageUpload>

    private lateinit var userProfilePictureUrl: String

    companion object {
        fun showFullScreenImage(context: Context, imageUrl: String) {
            val intent = Intent(context, FullScreenImageActivity::class.java)
            intent.putExtra(FullScreenImageActivity.IMAGE_URL_EXTRA, imageUrl)
            context.startActivity(intent)

            if (context is Activity) {
                context.overridePendingTransition(R.anim.stay, R.anim.stay)
            }
        }

        private var isThereAProfile = false
    }

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
                    overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
                    finish()
                    true
                }

                else -> false
            }
        }

        binding.settingsButton.setOnClickListener{
            val message = "What do you want to do?"
            message.showSettings()
        }

        binding.BookmarkButton.setOnClickListener{
            navigateToBookmarkPage()
        }


        // RecyclerView set up
        mRecyclerView = binding.petProfilesRecyclerView
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = GridLayoutManager(this, 1)

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

                    mAdapter = ImageAdapter(this@ProfilePage, mUploads, this@ProfilePage)
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

        binding.userProfilePic.setOnClickListener {
            if (isThereAProfile) {
                showFullScreenImage(this@ProfilePage, userProfilePictureUrl)
            }
        }
    }

    // change the listener if there is time for settings
    private fun retrieveUserData(userId: String) {
        val userRef = databaseRef.child("Users").child(userId)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val displayNameValue = dataSnapshot.child("Display Name").getValue(String::class.java)
                val userProfilePictureUrl = dataSnapshot.child("User Profile Picture").child("downloadUrl").getValue(String::class.java)
                if (userProfilePictureUrl != null) {
                    this@ProfilePage.userProfilePictureUrl = userProfilePictureUrl
                }

                if (!displayNameValue.isNullOrEmpty()) {
                    runOnUiThread {
                        val displayNameTextView = binding.displayNameTextView
                        displayNameTextView.text = displayNameValue
                    }
                }
                if (!userProfilePictureUrl.isNullOrEmpty()) {
                    isThereAProfile = true
                    Picasso.get().load(userProfilePictureUrl)
                        //.error(R.drawable.error_placeholder) // Replace with your error placeholder drawable
                        .into(binding.userProfilePic, object : Callback {
                            override fun onSuccess() {
                                val placeholder= binding.userIcon
                                placeholder.visibility = View.GONE
                            }

                            override fun onError(e: Exception) {
                                val placeholder = binding.userIcon
                                placeholder.visibility = View.VISIBLE
                                Toast.makeText(
                                    this@ProfilePage,
                                    "Error loading the image.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
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

        buttonEdit.setOnClickListener {
            navigateToEditProfilePage()
        }

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

    override fun onItemClick(petCardId: String) {
        startPetProfilePageActivity(petCardId)
    }

    private fun startPetProfilePageActivity(petCardId: String) {
        val intent = Intent(this, PetProfilePage::class.java)
        intent.putExtra("petCardId", petCardId)
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

    private fun navigateToBookmarkPage() {
        val intent = Intent(this, BookmarkPage::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.stay, R.anim.stay)
        finish()
    }

    private fun navigateToEditProfilePage() {
        val intent = Intent(this, EditProfilePage::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        navigateToUserProfile()
    }
}

