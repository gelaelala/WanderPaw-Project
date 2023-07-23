package com.codingstuff.loginandsignup

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.codingstuff.loginandsignup.databinding.ActivityFullPetProfilePageBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class FullPetProfilePage : AppCompatActivity() {

    private lateinit var binding: ActivityFullPetProfilePageBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var storageRef: FirebaseStorage
    private var profilePictureUrl: String? = null
    private val authToastLess = AuthToastLess(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullPetProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseRef = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance()


        val petCardId = intent.getStringExtra("petCardId")
        val userId = intent.getStringExtra("userId")

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
                    val medicalConditions =
                        dataSnapshot.child("Medical Conditions").getValue(String::class.java)
                    val vaccine = dataSnapshot.child("Vaccine_s Taken").getValue(String::class.java)
                    val diet = dataSnapshot.child("Pet's Diet").getValue(String::class.java)
                    val reason = dataSnapshot.child("Reason for Adoption").getValue(String::class.java)
                    val otherNeeds = dataSnapshot.child("Other Needs").getValue(String::class.java)
                    val requirements = dataSnapshot.child("Requirements for Adopter").getValue(String::class.java)
                    val contact = dataSnapshot.child("Contact Information").getValue(String::class.java)
                    val profilePictureUrl = dataSnapshot.child("Profile Picture").child("downloadUrl").getValue(String::class.java)
                    this@FullPetProfilePage.profilePictureUrl = profilePictureUrl

                    binding.PetName.text = name
                    binding.GenderData.text = gender
                    binding.AgeData.text = age
                    binding.LocationData.text = location
                    binding.BioData.text = bio
                    binding.AboutMeData.text = aboutMe
                    binding.BreedData.text = breed
                    binding.MedicalConditionsData.text = medicalConditions
                    binding.VaccineData.text = vaccine
                    binding.DietData.text = diet
                    binding.ReasonData.text = reason
                    binding.OtherNeedsData.text = otherNeeds
                    binding.RequirementsData.text = requirements
                    binding.ContactData.text = contact

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
            navigateToFeedTab()
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

    private fun navigateToFeedTab() {
        val intent = Intent(this, UserPetMatching::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.stay, R.anim.stay)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        navigateToFeedTab()
    }

}