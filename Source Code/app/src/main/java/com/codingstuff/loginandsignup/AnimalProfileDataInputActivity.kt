package com.codingstuff.loginandsignup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.codingstuff.loginandsignup.databinding.ActivityAnimalProfileDataInputBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class AnimalProfileDataInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnimalProfileDataInputBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnimalProfileDataInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseRef = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()

        binding.nextButton.setOnClickListener {
            try {
                val currentUser = firebaseAuth.currentUser
                val userId = currentUser?.uid
                val petCardID = createAnimalProfileId()
                val name = binding.nameInputField.text.toString()
                val age = binding.ageInputField1.text.toString()
                val gender = binding.genderInputField.selectedItem
                val location = binding.locationInputField.text.toString()
                val bio = binding.bioInputField.text.toString()
                val aboutMe = binding.aboutMeInputField.text.toString()

                // Create a HashMap to store user data
                val petCardData = HashMap<String, Any>()
                petCardData["Name"] = name
                petCardData["Age"] = age
                petCardData["Gender"] = gender
                petCardData["Location"] = location
                petCardData["Bio"] = bio
                petCardData["About Me"] = aboutMe

                if (userId != null) {
                    databaseRef.child("Users").child(userId).child("Animal Profiles Created").child(petCardID)
                        .updateChildren(petCardData)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Pet card data successfully written to the database
                                // Perform any additional actions or show success message
                            } else {
                                // Error occurred while writing pet card data to the database
                                // Handle the error appropriately (e.g., show error message)
                            }
                        }
                }
            } catch (e: Exception) {
                // Handle any exceptions that occur during data saving
                e.printStackTrace()
            }
        }
    }
}

fun createAnimalProfileId(): String {
    // Generate a unique ID using UUID
    return UUID.randomUUID().toString()
}
