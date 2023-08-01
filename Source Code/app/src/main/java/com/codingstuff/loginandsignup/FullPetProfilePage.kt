package com.codingstuff.loginandsignup

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.ScrollView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.codingstuff.loginandsignup.databinding.ActivityFullPetProfilePageBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

@Suppress("DEPRECATION")
class FullPetProfilePage : AppCompatActivity() {

    private lateinit var binding: ActivityFullPetProfilePageBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var storageRef: FirebaseStorage
    private var profilePictureUrl: String? = null
    private var petCardRef: DatabaseReference? = null
    private var buttonStateRetrieved = false // A flag to check if button state is retrieved
    private lateinit var callingAdapterClass: Class<*>
    private val authToastLess = AuthToastLess(this)

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullPetProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseRef = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance()

        val petCardId = intent.getStringExtra("petCardId")
        val userId = intent.getStringExtra("userId")
        val currentUser = firebaseAuth.currentUser
        val accountUser = currentUser?.uid

        // Retrieve the calling activity's class
        callingAdapterClass = intent.getSerializableExtra("callingAdapter") as Class<*>

        binding.PetProfileData.setOnClickListener {
            profilePictureUrl?.let { it1 ->
                ProfilePage.showFullScreenImage(this@FullPetProfilePage,
                    it1
                )
            }
        }

        val scrollView = findViewById<ScrollView>(R.id.ScrollView)
        val fabArrowUp = findViewById<FloatingActionButton>(R.id.fabArrowUp)

        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            // Show or hide the FAB based on the scroll position
            if (scrollY > 0) {
                fabArrowUp.show()
            } else {
                fabArrowUp.hide()
            }
        }

        fabArrowUp.setOnClickListener {
            // Scroll to the top of the ScrollView when the FAB is clicked
            scrollView.smoothScrollTo(0, 0)
        }

        // Fetch the button state only if it's not initialized yet (null or empty)
        petCardId?.let {
            if (accountUser != null) {
                petCardRef = databaseRef.child("Users").child(accountUser)
                    .child("Bookmarked Pet Profiles")
                    .child(it)

                petCardRef?.child("button_state")?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val isActive = snapshot.getValue(Boolean::class.java) ?: false
                        // Initialize button state as inactive (false) if it doesn't exist
                        if (!snapshot.exists()) {
                            petCardRef!!.child("button_state").setValue(false)
                            // Set the button to inactive (false)
                            binding.toggleButton.isChecked = false
                        } else {
                            // Set the button to the retrieved state
                            binding.toggleButton.isChecked = isActive
                        }
                        // Set the flag to true once the button state is retrieved
                        buttonStateRetrieved = true
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error if data retrieval is canceled
                        buttonStateRetrieved = true // Set the flag to true even in case of an error to prevent further action
                        runOnUiThread {
                            authToastLess.showToast("Data retrieval cancelled: ${error.message}")
                        }
                    }
                })
            }
        }

        binding.toggleButton.setOnClickListener {
            if (isNetworkConnected(this@FullPetProfilePage)) {
                // If button state is not retrieved yet, prevent further action
                if (!buttonStateRetrieved) {
                    return@setOnClickListener
                }
                // Check the current state of the button
                val isActive = binding.toggleButton.isChecked

                // Update the button state for the specific petCardId in the Realtime Database
                petCardRef?.child("button_state")?.setValue(isActive)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (isActive) {
                                // Perform action when the button is active (e.g., store data in the database)
                                storeDataInDatabase(petCardRef!!)

                            } else {
                                // Perform action when the button is inactive (e.g., delete data from the database)
                                deleteDataFromDatabase(petCardRef!!)
                            }
                        } else {
                            // Handle error if data is not saved to the database
                            handleAddPetCardIdFailure(task.exception)
                        }
                    }
            } else {
                authToastLess.showToast(
                    "There is a network connectivity issue. Please check your network.",
                    Toast.LENGTH_SHORT
                )
            }
        }


        petCardRef?.child("button_state")?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isActive = snapshot.getValue(Boolean::class.java) ?: false
                if (!snapshot.exists()) {
                    // If button state does not exist (null or empty), initialize it as inactive (false)
                    petCardRef!!.child("button_state").setValue(false)
                }
                binding.toggleButton.isChecked = isActive
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if data retrieval is canceled
                runOnUiThread {
                    authToastLess.showToast("Data retrieval cancelled: ${error.message}")
                }
            }
        })

        if (userId != null) {
            petCardId?.let {
                databaseRef.child("Users").child(userId)
                    .child("Animal Profiles Created").child(it)
            }?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Retrieve the children of the petCardId node
                    val name = dataSnapshot.child("Name").getValue(String::class.java)
                    val age = dataSnapshot.child("Age").getValue(String::class.java)
                    val gender = dataSnapshot.child("Gender").getValue(String::class.java)
                    val location = dataSnapshot.child("Location").getValue(String::class.java)
                    val bio = dataSnapshot.child("Bio").getValue(String::class.java)
                    val aboutMe = dataSnapshot.child("About Me").getValue(String::class.java)
                    val breed = dataSnapshot.child("Breed").getValue(String::class.java)


                    val medicalConditionsSnapshot = dataSnapshot.child("Medical Conditions")
                    val medicalConditionsString = StringBuilder()
                    if (medicalConditionsSnapshot.hasChildren()) {
                        medicalConditionsSnapshot.children.forEach { data ->
                            medicalConditionsString.append("\u2022 ").append(data.value)
                                .append("\n")
                        }
                    } else {
                        medicalConditionsString.append("Nothing to show here")
                    }
                    // Remove the last newline character
                    val finalMedicalConditions = medicalConditionsString.trimEnd()


                    val vaccineSnapshot = dataSnapshot.child("Vaccine_s Taken")
                    val vaccineString = StringBuilder()

                    if (vaccineSnapshot.hasChildren()) {
                        // Process the values if "Vaccine_s Taken" has children
                        vaccineSnapshot.children.forEach { data ->
                            vaccineString.append("\u2022 ").append(data.value).append("\n")
                        }
                    } else {
                        // Handle the case when "Vaccine_s Taken" has no children (no value)
                        vaccineString.append("Nothing to show here")
                    }

                    // Remove the last newline character
                    val finalVaccine = vaccineString.trimEnd()


                    val diet = dataSnapshot.child("Pet's Diet").getValue(String::class.java)
                    val reason = dataSnapshot.child("Reason for Adoption").getValue(String::class.java)


                    val needsSnapshot = dataSnapshot.child("Other Needs")
                    val needsString = StringBuilder()

                    if (needsSnapshot.hasChildren()) {
                        // Process the values if "Vaccine_s Taken" has children
                        needsSnapshot.children.forEach { data ->
                            needsString.append("\u2022 ").append(data.value).append("\n")
                        }
                    } else {
                        // Handle the case when "Vaccine_s Taken" has no children (no value)
                        needsString.append("Nothing to show here")
                    }

                    // Remove the last newline character
                    val finalNeeds = needsString.trimEnd()


                    val reqsSnapshot = dataSnapshot.child("Requirements for Adopter")
                    val reqsString = StringBuilder()

                    if (reqsSnapshot.hasChildren()) {
                        // Process the values if "Vaccine_s Taken" has children
                        reqsSnapshot.children.forEach { data ->
                            reqsString.append("\u2022 ").append(data.value).append("\n")
                        }
                    } else {
                        // Handle the case when "Vaccine_s Taken" has no children (no value)
                        reqsString.append("Nothing to show here")
                    }

                    // Remove the last newline character
                    val finalReqs = reqsString.trimEnd()


                    val contactSnapshot = dataSnapshot.child("Contact Information")
                    val contactString = StringBuilder()

                    if (contactSnapshot.hasChildren()) {
                        // Process the values if "Vaccine_s Taken" has children
                        contactSnapshot.children.forEach { data ->
                            contactString.append("\u2022 ").append(data.value).append("\n")
                        }
                    } else {
                        // Handle the case when "Vaccine_s Taken" has no children (no value)
                        contactString.append("Nothing to show here")
                    }

                    // Remove the last newline character
                    val finalContact = contactString.trimEnd()


                    val profilePictureUrl = dataSnapshot.child("Profile Picture").child("downloadUrl").getValue(String::class.java)
                    this@FullPetProfilePage.profilePictureUrl = profilePictureUrl

                    binding.PetName.text = name
                    binding.GenderData.text = gender
                    binding.AgeData.text = age
                    binding.LocationData.text = location
                    binding.BioData.text = bio
                    binding.AboutMeData.text = aboutMe
                    binding.BreedData.text = breed
                    binding.MedicalConditionsData.text = finalMedicalConditions
                    binding.VaccineData.text = finalVaccine
                    binding.DietData.text = diet
                    binding.ReasonData.text = reason
                    binding.OtherNeedsData.text = finalNeeds
                    binding.RequirementsData.text = finalReqs
                    binding.ContactData.text = finalContact

                    Picasso.get().load(profilePictureUrl)
                        //.error(R.drawable.error_placeholder) // Replace with your error placeholder drawable
                        .into(binding.PetProfileData, object : Callback {
                            override fun onSuccess() {
                            }

                            override fun onError(e: Exception) {
                                Toast.makeText(
                                    this@FullPetProfilePage,
                                    "Error loading the image.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle the error
                    authToastLess.showToast("Data retrieval cancelled: ${databaseError.message}")
                }
            })
        }

        binding.BackButton.setOnClickListener{
            onBackPressed()
        }

    }

    private fun storeDataInDatabase(petCardRef: DatabaseReference) {
        // Set the value of the specific petCardId's "button_state" to true to indicate it's bookmarked
        petCardRef.child("button_state").setValue(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    CustomToast.showToast(this, "Pet profile bookmarked.");
                } else {
                    // Error occurred while writing pet card data to the database
                    // Handle the error appropriately (e.g., show error message)
                    handleAddPetCardIdFailure(task.exception)
                }
            }
    }

    private fun deleteDataFromDatabase(petCardRef: DatabaseReference) {
        // Remove the specific petCardId's "button_state" node to unbookmark it
        petCardRef.child("button_state").removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    CustomToast.showToast(this, "Pet profile removed from bookmarks.");
                } else {
                    // Error occurred while deleting data
                    handleAddPetCardIdFailure(task.exception)
                }
            }
    }

    // function for stripping firebase exceptions
    private fun handleAddPetCardIdFailure(exception: Exception?) {
        authToastLess.cancelToast() // Cancel any active Toast message since empty fields and password mismatch are determined first before auth exceptions
        val errorMessage = exception?.let { AuthExceptionHandler.handleException(it) }
        if (errorMessage != null) {
            authToastLess.showToast(errorMessage)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val activeNetwork =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
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
        when (callingAdapterClass) {
            CardAdapter::class.java -> {
                val intent = Intent(this, UserPetMatching::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.stay, R.anim.stay)
                finish()
            }
            BookmarkAdapter::class.java -> {
                val intent = Intent(this, BookmarkPage::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.stay, R.anim.stay)
                finish()
            }
            // Add cases for other calling adapters if needed
            else -> {
                // Default behavior if the calling adapter is not recognized
                super.onBackPressed()
                overridePendingTransition(R.anim.stay, R.anim.stay)
            }
        }
    }
}