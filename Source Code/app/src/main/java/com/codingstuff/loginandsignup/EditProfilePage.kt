package com.codingstuff.loginandsignup

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.codingstuff.loginandsignup.databinding.ActivityEditProfilePageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.util.Locale

@Suppress("DEPRECATION")
class EditProfilePage : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfilePageBinding
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var databaseRef: DatabaseReference
    private lateinit var storageRef: StorageReference

    private lateinit var imageUri: Uri
    private val authToastLess = AuthToastLess(this)

    private var isImageSelected: Boolean = false

    private val currentUser = firebaseAuth.currentUser
    private val userId = currentUser?.uid
    private var profilePictureUrl: String? = null
    private var displayNameValue: String? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the database reference
        databaseRef = FirebaseDatabase.getInstance().reference

        // Initialize the storage reference
        storageRef = FirebaseStorage.getInstance().reference

        binding.imageButton.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(
                        this@EditProfilePage,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    openFileChooser()
                } else {
                    requestStoragePermission()
                }
            }
        }

        if (userId != null) {
            if (isNetworkConnected(this)) {
                retrieveUserData(userId)
            }
        }

        binding.BackButton.setOnClickListener{
            navigateToProfilePage()
        }

        binding.discardButton.setOnClickListener {
            navigateToProfilePage()
        }

        binding.saveButton.setOnClickListener {
            val displayName = binding.userName.text.toString()

            if (displayName.isEmpty() || (displayName.equals("n/a", ignoreCase = true) || displayName.equals("none", ignoreCase = true))) {
                authToastLess.showToast("WanderPaw requires user's name!")
            } else {
                if (displayName != displayNameValue) {
                    val petCardData = HashMap<String, Any>().apply {
                        put("Display Name", capitalizeWords(displayName))
                    }

                    if (userId != null) {
                        databaseRef.child("Users").child(userId)
                            .updateChildren(petCardData)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Pet card data successfully written to the database
                                    // Perform any additional actions or show success message
                                    Toast.makeText(this, "User profile changes will be reflected after page refresh.", Toast.LENGTH_LONG).show()
                                } else {
                                    // Error occurred while writing pet card data to the database
                                    // Handle the error appropriately (e.g., show error message)
                                    handleEditUserProfileFailure(task.exception)
                                }
                            }
                    }
                }

            if (isImageSelected) {
                uploadFile()
                Toast.makeText(this, "User profile changes will be reflected after page refresh.", Toast.LENGTH_SHORT).show()
            }
                navigateToProfilePage()

            }
        }
    }

    // change the listener if there is time for settings
    private fun retrieveUserData(userId: String) {
        val userRef = databaseRef.child("Users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val displayNameValue = dataSnapshot.child("Display Name").getValue(String::class.java)
                this@EditProfilePage.displayNameValue = displayNameValue
                val profilePictureUrl = dataSnapshot.child("User Profile Picture").child("downloadUrl").getValue(String::class.java)
                this@EditProfilePage.profilePictureUrl = profilePictureUrl

                if (!displayNameValue.isNullOrEmpty()) {
                    runOnUiThread {
                        val displayNameTextView = binding.userName

                        // Set the retrieved data to the EditText
                        displayNameTextView.setText(displayNameValue)
                    }
                }

                if (!profilePictureUrl.isNullOrEmpty()) {
                    Picasso.get().invalidate(profilePictureUrl) // Invalidate the cached image for the given URL
                    Picasso.get().load(profilePictureUrl)
                        //.error(R.drawable.error_placeholder) // Replace with your error placeholder drawable
                        .into(binding.imageHolder, object : Callback {
                            override fun onSuccess() {
                            }

                            override fun onError(e: Exception) {
                                Toast.makeText(
                                    this@EditProfilePage,
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

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@EditProfilePage,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            val builder = AlertDialog.Builder(this).apply {
                setMessage("WanderPaw would like to access your gallery for photo uploads.")
                setTitle("Want to upload a photo?")
                setPositiveButton("ALLOW") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this@EditProfilePage,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        AddPetInformation.STORAGE_CODE_PERMISSION
                    )
                }
                setNegativeButton("CANCEL") { dialog, _ ->
                    dialog.dismiss()
                }
            }

            builder.create().show()
        } else {
            ActivityCompat.requestPermissions(
                this@EditProfilePage,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                AddPetInformation.STORAGE_CODE_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AddPetInformation.STORAGE_CODE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@EditProfilePage, "Permission granted.", Toast.LENGTH_SHORT).show()
                openFileChooser()
            } else {
                Toast.makeText(this@EditProfilePage, "Permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openFileChooser() {
        val intent =
            Intent(Intent.ACTION_GET_CONTENT) // This action is commonly used to open a file picker or gallery to select content.
        intent.type =
            "image/*" // This specifies that we want to select an image file. The "*" is a wildcard that allows any subtype of images (e.g.,
        // JPEG, PNG, etc.)
        startActivityForResult(intent, AddPetInformation.pickImageRequest)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AddPetInformation.pickImageRequest && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data!!

            Picasso.get().load(imageUri)
                //.error(R.drawable.error_placeholder) // Replace with your error placeholder drawable
                .into(binding.imageHolder, object : Callback {
                    override fun onSuccess() {
                        isImageSelected = true
                        val placeholderLayout: LinearLayout = findViewById(R.id.placeholderIcon)
                        placeholderLayout.visibility = View.GONE
                    }

                    override fun onError(e: Exception) {
                        isImageSelected = false
                        Toast.makeText(this@EditProfilePage, "Error loading the image.", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun getFileExtension(uri: Uri): String? {
        // Overall, the getFileExtension function takes a Uri parameter, retrieves the MIME type using ContentResolver, and maps it to a file extension
        // using MimeTypeMap. The file extension is then returned as a String.
        val cR: ContentResolver = contentResolver
        val mime: MimeTypeMap =
            MimeTypeMap.getSingleton() // MimeTypeMap is a class in Android that maps MIME types to file extensions.
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun uploadFile() {
        if (isNetworkConnected(this)) {
            val fileReference: StorageReference? =
                userId?.let {
                    storageRef.child("Uploads").child(
                        "${System.currentTimeMillis()}." +
                                "${getFileExtension(imageUri)}"
                    )
                } // path for Storage database - where the file really is

            fileReference?.putFile(imageUri)?.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        handleEditUserProfileFailure(task.exception) }
                }
                fileReference.downloadUrl // retrieves downloadUrl of the uploaded file on storage database of firebase
            }?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val upload = Upload(downloadUrl.toString()) // gets string link

                    // stores within the initial structure
                    val petCardData = HashMap<String, Any>().apply {
                        put("User Profile Picture", upload)
                    }

                    if (userId != null) {
                        databaseRef.child("Users").child(userId)
                            .updateChildren(petCardData)
                    }
                }
            }?.addOnFailureListener { exception: Exception ->
                handleEditUserProfileFailure(exception)
            }

        } else {
            authToastLess.showToast("There is a network connectivity issue. Please check your network.")
        }
    }

    private fun capitalizeWords(input: String): String {
        return input.split(" ")
            .joinToString(" ") { it -> it.replaceFirstChar { if (it.isLowerCase()) it.titlecase(
                Locale.ROOT) else it.toString().trim() } }
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

    // function for stripping firebase exceptions
    private fun handleEditUserProfileFailure(exception: Exception?) {
        authToastLess.cancelToast() // Cancel any active Toast message since empty fields and password mismatch are determined first before auth exceptions
        val errorMessage = exception?.let { AuthExceptionHandler.handleException(it) }
        if (errorMessage != null) {
            authToastLess.showToast(errorMessage)
        }
    }

    private fun navigateToProfilePage() {
        val intent = Intent(this, ProfilePage::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.stay, R.anim.stay)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        navigateToProfilePage()
    }
}