package com.codingstuff.loginandsignup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.codingstuff.loginandsignup.databinding.ActivityPetProfilePageBinding
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

class PetProfilePage : AppCompatActivity() {

    private lateinit var binding: ActivityPetProfilePageBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var storageRef: FirebaseStorage
    private var profilePictureUrl: String? = null
    private val authToastLess = AuthToastLess(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPetProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseRef = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance()

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

        val currentUser = firebaseAuth.currentUser
        val userId = currentUser?.uid

        val petCardId = intent.getStringExtra("petCardId")

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
                    this@PetProfilePage.profilePictureUrl = profilePictureUrl

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
                                    this@PetProfilePage,
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
            navigateToUserProfile()
        }

        binding.deleteButton.setOnClickListener{
            val imageRef = storageRef.getReferenceFromUrl(profilePictureUrl!!)
            imageRef.delete().addOnSuccessListener {
                if (petCardId != null) {
                    if (userId != null) {
                        databaseRef.child("Users").child(userId)
                            .child("Animal Profiles Created").child(petCardId).removeValue()
                        Toast.makeText(this, "Pet profile has been deleted.", Toast.LENGTH_SHORT).show()
                        navigateToUserProfile()
                    }
                }
            }

        }
    }

    private fun navigateToUserProfile() {
        val intent = Intent(this, ProfilePage::class.java)
        startActivity(intent)
    }
}