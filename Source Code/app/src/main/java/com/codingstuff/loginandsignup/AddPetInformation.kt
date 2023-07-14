package com.codingstuff.loginandsignup

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.codingstuff.loginandsignup.AuthExceptionHandler.Companion.handleException
import com.codingstuff.loginandsignup.databinding.ActivityAddPetInformationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

@Suppress("DEPRECATION")
class AddPetInformation : AppCompatActivity() {

    private lateinit var binding: ActivityAddPetInformationBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var storageRef: StorageReference

//    private val medicalConditionTextList = mutableListOf<TextInputEditText>()
//    private val vaccinesTextList = mutableListOf<TextInputEditText>()
//    private val otherNeedsTextList = mutableListOf<TextInputEditText>()
//    private val requirementsTextList = mutableListOf<TextInputEditText>()
//    private val contactInfoTextList = mutableListOf<TextInputEditText>()

//    private val authToastLess = AuthToastLess(this)
    private lateinit var imageUri: Uri


    companion object {
        private const val pickImageRequest = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddPetInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseRef = FirebaseDatabase.getInstance().reference
        storageRef = FirebaseStorage.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()

        val spinner = binding.genderInputField
        val items = resources.getStringArray(R.array.gender_options)

        val customColor = ContextCompat.getColor(this, R.color.light_brown)
        val backgroundColor = ContextCompat.getColor(this, R.color.white)
        val initialText = "Select Here"

        val adapter =
            object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
                override fun getCount(): Int {
                    return super.getCount() + 1
                }

                override fun getItem(position: Int): String? {
                    return if (position == 0) initialText else super.getItem(position - 1)
                }

                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    val textView = view.findViewById<TextView>(android.R.id.text1)

                    if (position == 0) {
                        textView.text = initialText
                        textView.setTextColor(customColor)
                        textView.setTextSize(
                            TypedValue.COMPLEX_UNIT_SP, 13f
                        )
                    } else {
                        textView.setTextColor(customColor)
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                    }

                    return view
                }

                override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    val textView = view.findViewById<TextView>(android.R.id.text1)

                    if (position == 0) {
                        textView.text = initialText
                        textView.setTextColor(customColor)
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                        view.setBackgroundColor(backgroundColor)
                    } else {
                        textView.setTextColor(customColor)
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                        view.setBackgroundColor(backgroundColor)
                    }

                    return view
                }
            }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                adapter.notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        binding.imageButton?.setOnClickListener {
            openFileChooser()
        }

//        val progressBar = binding.progressBar
//        if (progressBar != null) {
//            progressBar.visibility = View.GONE
//        }


        // Write the pet card data to the database under the user's petCards node
        binding.SaveButton?.setOnClickListener {
            try {
                val currentUser = firebaseAuth.currentUser
                val userId = currentUser?.uid
                val petCardID = databaseRef.push().key
                //            val petCardID = createAnimalProfileId()
                val name = binding.petName.text.toString().trim()
                val age = binding.petAge.text.toString().trim()
                val gender = binding.genderInputField.selectedItem
                val location = binding.petLocation.text.toString().trim()
                val bio = binding.petBio.text.toString().trim()
                val aboutMe = binding.aboutPet.text.toString().trim()
                val breed = binding.petBreed.text.toString().trim()
                val medicalConditions = binding.petMedicalConditions.text.toString().trim()
                val vaccine = binding.petVaccinesTaken.text.toString().trim()
                val diet = binding.petDiet.text.toString().trim()
                val reason = binding.petReasonforAdoption.text.toString().trim()
                val otherNeeds = binding.petOtherNeeds.text.toString().trim()
                val requirements = binding.adopterRequirements.text.toString().trim()
                val contact = binding.userContactInfo.text.toString().trim()

                val fieldsNotEmpty = areFieldsNotEmpty(
                    name,
                    age,
                    gender as String,
                    location,
                    bio,
                    aboutMe,
                    breed,
                    medicalConditions,
                    vaccine,
                    reason,
                    contact
                )

                if (userId != null) {
                    if (petCardID != null) {
                        if (fieldsNotEmpty) {
                            // Create a HashMap to store user data
                            val petCardData = HashMap<String, Any>().apply {
                                put("Name", name)
                                put("Age", age)
                                put("Gender", gender)
                                put("Location", location)
                                put("Bio", bio)
                                put("About Me", aboutMe)
                                put("Breed", breed)
                                put("Medical Conditions", medicalConditions)
                                put("Vaccine_s Taken", vaccine)
                                put("Pet's Diet", diet)
                                put("Reason for Adoption", reason)
                                put("Other Needs", otherNeeds)
                                put("Requirements for Adopter", requirements)
                                put("Contact Information", contact)
//                    put("About Me", listOf(aboutMe) + aboutMeEditTextList.map { editText -> editText.text.toString() })
                            }
//                                if (binding.imageHolder == null) {
                                    databaseRef.child("Users").child(userId)
                                        .child("Animal Profiles Created")
                                        .child(petCardID)
                                        .updateChildren(petCardData)
                                        .addOnCompleteListener { task -> if (task.isSuccessful) {
                                                // Pet card data successfully written to the database
                                                // Perform any additional actions or show success message
                                                uploadFile(petCardID)
                                            } else {
                                                // Error occurred while writing pet card data to the database
                                                // Handle the error appropriately (e.g., show error message)
                                            }
                                        }
//                                }
//                                else {
//                                    Toast.makeText(this@AddPetInformation, "Please choose a profile picture for the pet.", Toast.LENGTH_LONG).show()
//                                }
                        }
                        else {
                            Toast.makeText(this@AddPetInformation, "Some required fields are empty! Please put N/A or NONE or choose other options available.", Toast.LENGTH_LONG).show()

                        }
                    }
                }
            } catch (exception: Exception) {
                // Handle any exceptions that occur during data saving
                handleException(exception)
            }
        }

    }

    private fun openFileChooser() {
        val intent =
            Intent(Intent.ACTION_GET_CONTENT) // This action is commonly used to open a file picker or gallery to select content.
        intent.type =
            "image/*" // This specifies that we want to select an image file. The "*" is a wildcard that allows any subtype of images (e.g.,
        // JPEG, PNG, etc.)
        startActivityForResult(intent, pickImageRequest)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == pickImageRequest && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data!!

            Picasso.get().load(imageUri).into(binding.imageHolder)
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

    private fun uploadFile(petCardID: String) {
        // This line creates a new StorageReference named fileReference by appending a child path to the storageRef. The child
        // path is formed using the current timestamp (System.currentTimeMillis()) concatenated with a period (.) and the file
        // extension obtained using the getFileExtension function.
        val fileReference: StorageReference = storageRef.child("Uploads").child(
            "${System.currentTimeMillis()}." +
                    "${getFileExtension(imageUri)}"
        ) // path for Storage database - where the file really is


        fileReference.putFile(imageUri)
            .addOnCompleteListener { task ->
//                val delayProgressHandler = Handler()
//                val progressRunnable = Runnable {
//                    if (progressBar != null) {
//                        progressBar.progress = 0
//                        progressBar.visibility = View.GONE
//
//                    }
//                } // progress bar operation
//                delayProgressHandler.postDelayed(progressRunnable, 500)
                Toast.makeText(this@AddPetInformation, "Profile uploaded.", Toast.LENGTH_SHORT)
                    .show()
                val upload =
                    Upload(task.result?.storage?.downloadUrl.toString()) // gets the string/link
                val currentUser = firebaseAuth.currentUser
                val userId = currentUser?.uid

                // stores within the initial structure
                val petCardData = HashMap<String, Any>().apply {
                    put("Profile Picture", upload)
                }

                if (userId != null) {
                    databaseRef.child("Users").child(userId).child("Animal Profiles Created")
                        .child(petCardID)
                        .updateChildren(petCardData)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@AddPetInformation, e.message, Toast.LENGTH_SHORT).show()
            }
//            .addOnProgressListener { taskSnapshot ->
//                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt() // progress bar progress
//                if (progressBar != null) {
//                    progressBar.progress = progress
//                    progressBar.visibility = View.VISIBLE
//
//                }
//            }
    }

    private fun areFieldsNotEmpty(
        name: String,
        age: String,
        gender: String,
        location: String,
        bio: String,
        aboutMe: String,
        breed: String,
        medicalConditions: String,
        vaccine: String,
        reason: String,
        contact: String
    ): Boolean {
        return name.isNotEmpty() && age.isNotEmpty() && gender != "Select Here" && location.isNotEmpty() && bio.isNotEmpty() && aboutMe.isNotEmpty() && breed.isNotEmpty() && medicalConditions.isNotEmpty() && vaccine.isNotEmpty() && reason.isNotEmpty() && contact.isNotEmpty()
    }



}

//    private fun addNewAboutMeInputField() {
//        val newAboutMeEditText = EditText(this)
//        newAboutMeEditText.layoutParams = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            LinearLayout.LayoutParams.WRAP_CONTENT
//        )
//        newAboutMeEditText.id = View.generateViewId()
//        binding.inputContainer.addView(newAboutMeEditText)
//        aboutMeEditTextList.add(newAboutMeEditText) // Add the reference to the list
//    }



data class Upload(val downloadUrl: String)

