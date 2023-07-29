package com.codingstuff.loginandsignup

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.codingstuff.loginandsignup.databinding.ActivityPetProfilePageBinding
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
    private var name: String? = null
    private var gender: String? = null
    private var age: String? = null
    private var location: String? = null
    private var bio: String? = null
    private var aboutMe: String? = null
    private var breed: String? = null
    private var finalMedicalConditions: String? = null
    private var finalVaccine: String? = null
    private var diet: String? = null
    private var reason: String? = null
    private var finalNeeds: String? = null
    private var finalReqs: String? = null
    private var finalContact: String? = null
    private var profilePictureUrl: String? = null
    private val authToastLess = AuthToastLess(this)

    private var noNeedToEdit: String = "no"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPetProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseRef = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance()

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
                    this@PetProfilePage.name = name.toString()

                    val age = dataSnapshot.child("Age").getValue(String::class.java)
                    this@PetProfilePage.age = age.toString()

                    val gender = dataSnapshot.child("Gender").getValue(String::class.java)
                    this@PetProfilePage.gender = gender.toString()

                    val location = dataSnapshot.child("Location").getValue(String::class.java)
                    this@PetProfilePage.location = location.toString()

                    val bio = dataSnapshot.child("Bio").getValue(String::class.java)
                    this@PetProfilePage.bio = bio.toString()

                    val aboutMe = dataSnapshot.child("About Me").getValue(String::class.java)
                    this@PetProfilePage.aboutMe = aboutMe.toString()

                    val breed = dataSnapshot.child("Breed").getValue(String::class.java)
                    this@PetProfilePage.breed = breed.toString()


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
                    this@PetProfilePage.finalMedicalConditions = finalMedicalConditions.toString()


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
                    this@PetProfilePage.finalVaccine = finalVaccine.toString()


                    val diet = dataSnapshot.child("Pet's Diet").getValue(String::class.java)
                    this@PetProfilePage.diet = diet

                    val reason = dataSnapshot.child("Reason for Adoption").getValue(String::class.java)
                    this@PetProfilePage.reason = reason


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
                    this@PetProfilePage.finalNeeds = finalNeeds.toString()


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
                    this@PetProfilePage.finalReqs = finalReqs.toString()


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
                    this@PetProfilePage.finalContact = finalContact.toString()

                    val profilePictureUrl = dataSnapshot.child("Profile Picture").child("downloadUrl").getValue(String::class.java)
                    this@PetProfilePage.profilePictureUrl = profilePictureUrl

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

        binding.deleteButton.setOnClickListener {
            if (petCardId != null) {
                if (userId != null) {
                    showDelete(petCardId, userId)
                }
            }
        }

        binding.editButton.setOnClickListener {
            if (petCardId != null) {
                if (userId != null) {
                    noNeedToEdit = "no"
                    val intent = Intent(this, AddPetInformation::class.java)
                    intent.putExtra("noNeedToEdit", noNeedToEdit)
                    intent.putExtra("profilePictureUrl", profilePictureUrl)
                    intent.putExtra("name", name)
                    intent.putExtra("gender", gender)
                    intent.putExtra("age", age)
                    intent.putExtra("location", location)
                    intent.putExtra("bio", bio)
                    intent.putExtra("aboutMe", aboutMe)
                    intent.putExtra("breed", breed)
//                    intent.putExtra("finalMedicalConditions", finalMedicalConditions)
//                    intent.putExtra("finalVaccine", finalVaccine)
                    intent.putExtra("diet", diet)
                    intent.putExtra("reason", reason)
//                    intent.putExtra("finalNeeds", finalNeeds)
//                    intent.putExtra("finalReqs", finalReqs)
//                    intent.putExtra("finalContact", finalContact)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
                    finish()
                }
            }
        }

    }

    private fun showDelete(petCardId: String, userId: String) {
        val dialog = Dialog(this@PetProfilePage)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.delete_pet_profile)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val yesButton: Button = dialog.findViewById(R.id.yesButton)
        val noButton: Button = dialog.findViewById(R.id.noButton)


        yesButton.setOnClickListener {
            val imageRef = storageRef.getReferenceFromUrl(profilePictureUrl!!)
            imageRef.delete().addOnSuccessListener {
                databaseRef.child("Users").child(userId)
                    .child("Animal Profiles Created").child(petCardId).removeValue()
                Toast.makeText(this, "Pet profile has been deleted.", Toast.LENGTH_SHORT).show()
                Toast.makeText(
                    this@PetProfilePage,
                    "Pet profile has been deleted.",
                    Toast.LENGTH_SHORT
                ).show()
                navigateToUserProfile()
            }
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