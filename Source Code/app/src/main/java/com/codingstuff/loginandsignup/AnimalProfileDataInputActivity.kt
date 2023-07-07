package com.codingstuff.loginandsignup

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.codingstuff.loginandsignup.databinding.ActivityAnimalProfileDataInputBinding
import com.google.firebase.FirebaseApiNotAvailableException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

@Suppress("DEPRECATION")
class AnimalProfileDataInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnimalProfileDataInputBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var storageRef: StorageReference
    private val authToastLess = AuthToastLess(this)
    private lateinit var imageUri: Uri
//    private var counter = 0
    private val aboutMeEditTextList = mutableListOf<EditText>()


    companion object {
        private const val pickImageRequest = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnimalProfileDataInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseRef = FirebaseDatabase.getInstance().reference
        storageRef = FirebaseStorage.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()

//        binding.addButton.setOnClickListener {
//            addNewAboutMeInputField()
//        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.nextButton.setOnClickListener {
            try {
                val currentUser = firebaseAuth.currentUser
                val userId = currentUser?.uid
                val petCardID = databaseRef.push().key
                val name = binding.nameInputField.text.toString()
                val age = binding.ageInputField1.text.toString()
                val gender = binding.genderInputField.selectedItem
                val location = binding.locationInputField.text.toString()
                val bio = binding.bioInputField.text.toString()
                val aboutMe = binding.aboutMeInputField.text.toString()

                val petCardData = HashMap<String, Any>().apply {
                    put("Name", name)
                    put("Age", age)
                    put("Gender", gender)
                    put("Location", location)
                    put("Bio", bio)

                    // Append the values from the bioEditTextList as a list
//                    put("About Me", listOf(aboutMe) + aboutMeEditTextList.map { editText -> editText.text.toString() })
                }


                if (userId != null && petCardID != null) {
                    databaseRef.child("Users").child(userId).child("Animal Profiles Created").child(petCardID)
                        .updateChildren(petCardData)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Pet card data successfully written to the database
                                // Perform any additional actions or show success message
                                uploadFile(petCardID)
                            }
                            // else {
                            // Error occurred while writing pet card data to the database
                            // Handle the error appropriately (e.g., show error message)
                            // }
                        }
                }
            } catch (exception: Exception) {
                handleException(exception)
            }
        }

        binding.uploadButton.setOnClickListener {
            openFileChooser()
        }

        binding.backButton.setOnClickListener {
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
        if (requestCode == pickImageRequest&& resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data!!

            Picasso.get().load(imageUri).into(binding.petProfilePic)
//            uploadFile()
        }
    }

    private fun getFileExtension(uri: Uri): String? {
        // Overall, the getFileExtension function takes a Uri parameter, retrieves the MIME type using ContentResolver, and maps it to a file extension
        // using MimeTypeMap. The file extension is then returned as a String.
        val cR: ContentResolver = contentResolver
        val mime: MimeTypeMap = MimeTypeMap.getSingleton() // MimeTypeMap is a class in Android that maps MIME types to file extensions.
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    private fun uploadFile(petCardID: String) {
        // This line creates a new StorageReference named fileReference by appending a child path to the storageRef. The child
        // path is formed using the current timestamp (System.currentTimeMillis()) concatenated with a period (.) and the file
        // extension obtained using the getFileExtension function.
        val fileReference: StorageReference = storageRef.child("Uploads").child("${System.currentTimeMillis()}." +
                "${getFileExtension(imageUri)}") // path for Storage database - where the file really is
        fileReference.putFile(imageUri)
            .addOnCompleteListener { task ->
                val delayProgressHandler = Handler()
                val progressRunnable = Runnable { binding.progressBarImg.progress = 0 } // progress bar operation
                delayProgressHandler.postDelayed(progressRunnable, 500)
                Toast.makeText(this@AnimalProfileDataInputActivity, "Profile uploaded.", Toast.LENGTH_SHORT).show()
                val upload = Upload(task.result?.storage?.downloadUrl.toString()) // gets the string/link
                val uploadId = databaseRef.push().key // unique id
                if (uploadId != null) {
                    val currentUser = firebaseAuth.currentUser
                    val userId = currentUser?.uid

                    // stores within the initial structure
                    val petCardData = HashMap<String, Any>().apply {
                        put("Pet Profile ID", uploadId)
                        put("Profile Picture", upload)
                    }

                    if (userId != null) {
                        databaseRef.child("Users").child(userId).child("Animal Profiles Created").child(petCardID)
                            .updateChildren(petCardData)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@AnimalProfileDataInputActivity, e.message, Toast.LENGTH_SHORT).show()
            }
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt() // progress bar progress
                binding.progressBarImg.progress = progress
            }
    }
// temp name
    private fun addNewAboutMeInputField() {
        val newAboutMeEditText = EditText(this)
        newAboutMeEditText.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        newAboutMeEditText.id = View.generateViewId()
        binding.inputContainer.addView(newAboutMeEditText)
        aboutMeEditTextList.add(newAboutMeEditText) // Add the reference to the list
    }

    private fun handleException(exception: Exception) {
        val errorMessage = exception?.let { AuthExceptionHandler.handleException(it) }
        if (errorMessage != null) {
            authToastLess.showToast(errorMessage)
        }
    }
}

data class Upload(val downloadUrl: String)








