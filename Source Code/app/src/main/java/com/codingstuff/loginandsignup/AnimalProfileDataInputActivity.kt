package com.codingstuff.loginandsignup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.codingstuff.loginandsignup.databinding.ActivityAnimalProfileDataInputBinding
import com.google.firebase.FirebaseApiNotAvailableException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import java.util.UUID

@Suppress("DEPRECATION")
class AnimalProfileDataInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnimalProfileDataInputBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private val authToastLess = AuthToastLess(this)
    private lateinit var imageUri: Uri


    companion object {
        private const val pickImageRequest = 1
    }


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
                            }
//                            else {
                                // Error occurred while writing pet card data to the database
                                // Handle the error appropriately (e.g., show error message)
//                            }
                        }
                }
            } catch (exception: Exception) {
                when (exception) {
                    is DatabaseException -> {
                        authToastLess.showToast("A database exception happened. Please try again.")
                    }
//                    is DatabaseError -> {
//                        authToastLess.showToast("An error happened while interacting with the database. Please try again.")
//                    }
                    is FirebaseApiNotAvailableException -> {
                        authToastLess.showToast("The requested API is not available.")
                    }

                    is FirebaseNetworkException -> {
                        authToastLess.showToast("There is a network connectivity issue. Please check your network.")
                    }
                    is FirebaseTooManyRequestsException -> {
                        authToastLess.showToast("Too many requests. Try again later.")
                    }
                    else -> {
                        authToastLess.showToast("An undefined error happened.")
                    }
                }
            }
        }

        binding.uploadButton.setOnClickListener{
            openFileChooser()
        }

        binding.backButton.setOnClickListener{
            val intent = Intent(this, ProfilePage::class.java)
            startActivity(intent)
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT) // This action is commonly used to open a file picker or gallery to select content.
        intent.type = "image/*" // This specifies that we want to select an image file. The "*" is a wildcard that allows any subtype of images (e.g.,
        // JPEG, PNG, etc.)
        startActivityForResult(intent, pickImageRequest)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        binding = ActivityAnimalProfileDataInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (requestCode == pickImageRequest && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data!!

            Picasso.get().load(imageUri).into(binding.petProfilePic)
        }
    }

}

fun createAnimalProfileId(): String {
    // Generate a unique ID using UUID
    return UUID.randomUUID().toString()
}
