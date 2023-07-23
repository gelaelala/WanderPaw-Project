package com.codingstuff.loginandsignup

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.codingstuff.loginandsignup.AuthExceptionHandler.Companion.handleException
import com.codingstuff.loginandsignup.databinding.ActivityAddPetInformationBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.util.Locale
import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import androidx.annotation.NonNull
import com.google.android.gms.common.PackageVerificationResult


@Suppress("DEPRECATION")
class AddPetInformation : AppCompatActivity() {

    private lateinit var binding: ActivityAddPetInformationBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var storageRef: StorageReference

    private val medicalConditionTextList = mutableListOf<TextInputEditText>()
    private val vaccinesTextList = mutableListOf<TextInputEditText>()
    private val otherNeedsTextList = mutableListOf<TextInputEditText>()
    private val requirementsTextList = mutableListOf<TextInputEditText>()
    private val contactInfoTextList = mutableListOf<TextInputEditText>()

    private val authToastLess = AuthToastLess(this)
    private lateinit var imageUri: Uri

    private var isImageSelected: Boolean = false

    companion object {
        private const val pickImageRequest = 1
        private const val STORAGE_CODE_PERMISSION = 1

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddPetInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListener()

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

        binding.imageButton.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(
                        this@AddPetInformation,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    openFileChooser()
                } else {
                    requestStoragePermission()
                }
            }
        }

        // Write the pet card data to the database under the user's petCards node
        binding.SaveButton.setOnClickListener {

            val currentUser = firebaseAuth.currentUser
            val userId = currentUser?.uid
            val petCardID = databaseRef.push().key

            val name = binding.petName.text.toString().toUpperCase(Locale.ROOT)
            val age = binding.petAge.text.toString().trim()
            val gender = binding.genderInputField.selectedItem
            val location = capitalizeWords(binding.petLocation.text.toString())
            val bio = formatStringWithCapital(binding.petBio.text.toString())
            val aboutMe = formatStringWithCapital(binding.aboutPet.text.toString())
            val breed = capitalizeWords(binding.petBreed.text.toString())


            val medicalCon: String? = capitalizeWords(binding.petMedicalConditions.text.toString())
            val medicalConditions = mutableListOf<String>()
            if (!medicalCon.isNullOrBlank()) {
                medicalConditions.add(medicalCon)
            }
            medicalConditions.addAll(medicalConditionTextList.map { textInputEditText ->
                capitalizeWords(textInputEditText.text.toString())
            }.filter { it.isNotBlank() })
            val medicalConditionsValue = if (isNAList(medicalConditions)) "Nothing to show here" else medicalConditions

            val vaccineInputOne: String? = capitalizeWords(binding.petVaccinesTaken.text.toString())
            val vaccine = mutableListOf<String>()
            if (!vaccineInputOne.isNullOrBlank()) {
                vaccine.add(vaccineInputOne)
            }
            vaccine.addAll(vaccinesTextList.map { textInputEditText ->
                capitalizeWords(textInputEditText.text.toString())
            }.filter { it.isNotBlank() })
            val vaccineValue = if (isNAList(vaccine)) "Nothing to show here" else vaccine

            val diet = formatStringWithCapital(binding.petDiet.text.toString())
            val reason = formatStringWithCapital(binding.petReasonforAdoption.text.toString())


            val needsInputOne: String? = capitalizeWords(binding.petOtherNeeds.text.toString())
            val otherNeeds = mutableListOf<String>()
            if (!needsInputOne.isNullOrBlank()) {
                otherNeeds.add(needsInputOne)
            }
            otherNeeds.addAll(otherNeedsTextList.map { textInputEditText ->
                capitalizeWords(textInputEditText.text.toString())
            }.filter { it.isNotBlank() })
            val needsValue = if (isNAList(otherNeeds)) "Nothing to show here" else otherNeeds


            val reqsInputOne: String? = capitalizeWords(binding.adopterRequirements.text.toString())
            val requirements = mutableListOf<String>()
            if (!reqsInputOne.isNullOrBlank()) {
                requirements.add(reqsInputOne)
            }
            requirements.addAll(requirementsTextList.map { textInputEditText ->
                capitalizeWords(textInputEditText.text.toString())
            }.filter { it.isNotBlank() })
            val reqsValue = if (isNAList(requirements)) "Nothing to show here" else requirements


            val contactCon: String? = capitalizeWords(binding.userContactInfo.text.toString())
            val contact = mutableListOf<String>()
            if (!contactCon.isNullOrBlank()) {
                contact.add(contactCon)
            }
            contact.addAll(contactInfoTextList.map { textInputEditText ->
                capitalizeWords(textInputEditText.text.toString())
            }.filter { it.isNotBlank() })
            val contactValue = if (isNAList(contact)) "Nothing to show here" else contact


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
                        if (isImageSelected) {
                            if (isNetworkConnected(this@AddPetInformation)) {
                                // Create a HashMap to store user data
                                val petCardData = HashMap<String, Any>().apply {
                                    put("Name", if (isNAText(name)) "Nothing to show here" else name)
                                    put("Age", if (isNAText(age)) "Nothing to show here" else age)
                                    put("Gender", gender)
                                    put("Location", if (isNAText(location)) "Nothing to show here" else location)
                                    put("Bio", if (isNAText(bio)) "Nothing to show here" else bio)
                                    put("About Me", if (isNAText(aboutMe)) "Nothing to show here" else aboutMe)
                                    put("Breed", if (isNAText(breed)) "Nothing to show here" else breed)
                                    put("Medical Conditions",medicalConditionsValue)
                                    put("Vaccine_s Taken", vaccineValue)
                                    put("Pet's Diet", if (isNAText(diet) || diet == "") "Nothing to show here" else diet)
                                    put("Reason for Adoption", if (isNAText(reason)) "Nothing to show here" else reason)
                                    put("Other Needs", needsValue)
                                    put("Requirements for Adopter", reqsValue)
                                    put("Contact Information", contactValue)


                                }
                                //                                if (binding.imageHolder == null) {
                                databaseRef.child("Users").child(userId)
                                    .child("Animal Profiles Created")
                                    .child(petCardID)
                                    .updateChildren(petCardData)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            // Pet card data successfully written to the database
                                            // Perform any additional actions or show success message
                                            uploadFile(petCardID)
                                            Toast.makeText(this, "Pet profile is uploading..", Toast.LENGTH_SHORT).show()
                                            navigateToProfilePage()
                                        } else {
                                            // Error occurred while writing pet card data to the database
                                            // Handle the error appropriately (e.g., show error message)
                                            handleAddPetProfileFailure(task.exception)
                                        }
                                    }
                            } else {
                                Toast.makeText(
                                    this@AddPetInformation,
                                    "There is a network connectivity issue. Please check your network.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(this@AddPetInformation, "Please choose a profile picture for the pet.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(
                            this@AddPetInformation,
                            "Some required fields are empty! Please put N/A or none or choose other options available.",
                            Toast.LENGTH_LONG
                        ).show()

                    }
                }
            }

        }

        binding.addMedCon.setOnClickListener{
            addNewMedConInputField()
        }

        binding.addVaccine.setOnClickListener{
            addNewVaccineInputField()
        }

        binding.addNeeds.setOnClickListener{
            addNewNeedsInputField()
        }

        binding.addReqs.setOnClickListener{
            addNewReqsInputField()
        }

        binding.addContact.setOnClickListener{
            addNewContactInputField()
        }
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@AddPetInformation,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            val builder = AlertDialog.Builder(this).apply {
                setMessage("WanderPaw would like to access your gallery for photo uploads.")
                setTitle("Want to upload a photo?")
                setPositiveButton("ALLOW") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this@AddPetInformation,
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        STORAGE_CODE_PERMISSION
                    )
                }
                setNegativeButton("CANCEL") { dialog, _ ->
                    dialog.dismiss()
                }
            }

            builder.create().show()
        } else {
            ActivityCompat.requestPermissions(
                this@AddPetInformation,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_CODE_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_CODE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@AddPetInformation, "Permission granted.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@AddPetInformation, "Permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun formatStringWithCapital(input: String): String {
        return input.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString().trim() }
    }

    private fun capitalizeWords(input: String): String {
        return input.split(" ")
            .joinToString(" ") { it -> it.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString().trim() } }
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

            Picasso.get().load(imageUri)
                //.error(R.drawable.error_placeholder) // Replace with your error placeholder drawable
                .into(binding.imageHolder, object : Callback {
                    override fun onSuccess() {
                        isImageSelected = true
                    }

                    override fun onError(e: Exception) {
                        isImageSelected = false
                        Toast.makeText(this@AddPetInformation, "Error loading the image.", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            isImageSelected = false
            Toast.makeText(this@AddPetInformation, "Please choose a profile picture for the pet.", Toast.LENGTH_LONG).show()
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
    private fun uploadFile(petCardID: String) {
        // This line creates a new StorageReference named fileReference by appending a child path to the storageRef. The child
        // path is formed using the current timestamp (System.currentTimeMillis()) concatenated with a period (.) and the file
        // extension obtained using the getFileExtension function.
        if (isNetworkConnected(this)) {
            val fileReference: StorageReference = storageRef.child("Uploads").child(
                "${System.currentTimeMillis()}." +
                        "${getFileExtension(imageUri)}"
            ) // path for Storage database - where the file really is


            fileReference.putFile(imageUri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            handleAddPetProfileFailure(task.exception) }
                    }
                    fileReference.downloadUrl // retrieves downloadUrl of the uploaded file on storage database of firebase
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        val upload = Upload(downloadUrl.toString()) // gets string link
                        val currentUser = firebaseAuth.currentUser
                        val userId = currentUser?.uid


                        // stores within the initial structure
                        val petCardData = HashMap<String, Any>().apply {
                            put("Profile Picture", upload)
                        }


                        if (userId != null) {
                            databaseRef.child("Users").child(userId)
                                .child("Animal Profiles Created")
                                .child(petCardID)
                                .updateChildren(petCardData)
                        }
                    }
                }
                .addOnFailureListener { exception: Exception ->
                    handleAddPetProfileFailure(exception)
                }

        } else {
            Toast.makeText(this, "There is a network connectivity issue. Please check your network.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun areFieldsNotEmpty(
        name: String,
        age: String,
        gender: String,
        location: String,
        bio: String,
        aboutMe: String,
        breed: String,
        medicalConditions: MutableList<String>,
        vaccine: MutableList<String>,
        reason: String,
        contact: MutableList<String>
    ): Boolean {
        return name.isNotEmpty() && age.isNotEmpty() && gender != "Select Here" && location.isNotEmpty() && bio.isNotEmpty() && aboutMe.isNotEmpty() && breed.isNotEmpty() && medicalConditions.isNotEmpty() && vaccine.isNotEmpty() && reason.isNotEmpty() && contact.isNotEmpty()
    }

    // function for stripping firebase exceptions
    private fun handleAddPetProfileFailure(exception: Exception?) {
        authToastLess.cancelToast() // Cancel any active Toast message since empty fields and password mismatch are determined first before auth exceptions
        val errorMessage = exception?.let { handleException(it) }
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

    private fun isNAText(input: String): Boolean {
        // Convert the input and target strings to lowercase for case-insensitive comparison
        val lowerInput = input.trim().toLowerCase(Locale.ROOT)
        val targetStrings = listOf("n/a", "none")

        // Check if the lowercase input matches any of the target strings
        return targetStrings.any { lowerInput == it }
    }

    private fun isNAList(severalInputs: List<String>): Boolean {
        if (severalInputs.isEmpty()) {
            return true
        }

        val firstCondition = severalInputs.first().trim().lowercase(Locale.ROOT)
        return firstCondition == "n/a" || firstCondition == "none"
    }

    private val customWidth = 810
    private val customHeight = 150
    private val marginLeftSize = 63
    private val marginBottomSize = 30

    @SuppressLint("ResourceType")
    private fun addNewMedConInputField() {
        val textInputLayout = TextInputLayout(this)
        textInputLayout.id = View.generateViewId()
        val layoutParams = LinearLayout.LayoutParams(
            customWidth,
            customHeight
        )

        layoutParams.leftMargin = marginLeftSize
        textInputLayout.layoutParams = layoutParams
        layoutParams.bottomMargin = marginBottomSize
        textInputLayout.layoutParams = layoutParams
        layoutParams.gravity = Gravity.CENTER_VERTICAL

        textInputLayout.hint = " "

        val textInputEditText = TextInputEditText(this)
        textInputEditText.id = View.generateViewId()
        textInputEditText.layoutParams = LinearLayout.LayoutParams(
            customWidth,
            customHeight
        )
        textInputEditText.setPadding(45, 0, 0, 0)

        textInputEditText.setBackgroundResource(R.drawable.input_field_add_pet)
        textInputEditText.setTextColor(ContextCompat.getColor(this, R.color.light_brown))
        textInputEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        textInputEditText.hint = "Enter Here"

        // Set font programmatically
        val typeface = ResourcesCompat.getFont(this, R.font.inter)
        textInputEditText.typeface = typeface

        textInputLayout.addView(textInputEditText)
        binding.medConInputCon.addView(textInputLayout)
        medicalConditionTextList.add(textInputEditText)
    }

    @SuppressLint("ResourceType")
    private fun addNewVaccineInputField() {
        val textInputLayout = TextInputLayout(this)
        textInputLayout.id = View.generateViewId()
        val layoutParams = LinearLayout.LayoutParams(
            customWidth,
            customHeight
        )

        layoutParams.leftMargin = marginLeftSize
        textInputLayout.layoutParams = layoutParams
        layoutParams.bottomMargin = marginBottomSize
        textInputLayout.layoutParams = layoutParams
        layoutParams.gravity = Gravity.CENTER_VERTICAL

        textInputLayout.hint = " "

        val textInputEditText = TextInputEditText(this)
        textInputEditText.id = View.generateViewId()
        textInputEditText.layoutParams = LinearLayout.LayoutParams(
            customWidth,
            customHeight
        )
        textInputEditText.setPadding(45, 0, 0, 0)

        textInputEditText.setBackgroundResource(R.drawable.input_field_add_pet)
        textInputEditText.setTextColor(ContextCompat.getColor(this, R.color.light_brown))
        textInputEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        textInputEditText.hint = "Enter Here"

        // Set font programmatically
        val typeface = ResourcesCompat.getFont(this, R.font.inter)
        textInputEditText.typeface = typeface

        textInputLayout.addView(textInputEditText)
        binding.vaccineInputCon.addView(textInputLayout)
        vaccinesTextList.add(textInputEditText)
    }

    private fun addNewNeedsInputField() {
        val textInputLayout = TextInputLayout(this)
        textInputLayout.id = View.generateViewId()
        val layoutParams = LinearLayout.LayoutParams(
            customWidth,
            customHeight
        )

        layoutParams.leftMargin = marginLeftSize
        textInputLayout.layoutParams = layoutParams
        layoutParams.bottomMargin = marginBottomSize
        textInputLayout.layoutParams = layoutParams
        layoutParams.gravity = Gravity.CENTER_VERTICAL

        textInputLayout.hint = " "

        val textInputEditText = TextInputEditText(this)
        textInputEditText.id = View.generateViewId()
        textInputEditText.layoutParams = LinearLayout.LayoutParams(
            customWidth,
            customHeight
        )
        textInputEditText.setPadding(45, 0, 0, 0)

        textInputEditText.setBackgroundResource(R.drawable.input_field_add_pet)
        textInputEditText.setTextColor(ContextCompat.getColor(this, R.color.light_brown))
        textInputEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        textInputEditText.hint = "Enter Here"

        // Set font programmatically
        val typeface = ResourcesCompat.getFont(this, R.font.inter)
        textInputEditText.typeface = typeface

        textInputLayout.addView(textInputEditText)
        binding.needsInputCon.addView(textInputLayout)
        otherNeedsTextList.add(textInputEditText)
    }

    private fun addNewReqsInputField() {
        val textInputLayout = TextInputLayout(this)
        textInputLayout.id = View.generateViewId()
        val layoutParams = LinearLayout.LayoutParams(
            customWidth,
            customHeight
        )

        layoutParams.leftMargin = marginLeftSize
        textInputLayout.layoutParams = layoutParams
        layoutParams.bottomMargin = marginBottomSize
        textInputLayout.layoutParams = layoutParams
        layoutParams.gravity = Gravity.CENTER_VERTICAL

        textInputLayout.hint = " "

        val textInputEditText = TextInputEditText(this)
        textInputEditText.id = View.generateViewId()
        textInputEditText.layoutParams = LinearLayout.LayoutParams(
            customWidth,
            customHeight
        )
        textInputEditText.setPadding(45, 0, 0, 0)

        textInputEditText.setBackgroundResource(R.drawable.input_field_add_pet)
        textInputEditText.setTextColor(ContextCompat.getColor(this, R.color.light_brown))
        textInputEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        textInputEditText.hint = "Enter Here"

        // Set font programmatically
        val typeface = ResourcesCompat.getFont(this, R.font.inter)
        textInputEditText.typeface = typeface

        textInputLayout.addView(textInputEditText)
        binding.reqsInputCon.addView(textInputLayout)
        requirementsTextList.add(textInputEditText)
    }

    private fun addNewContactInputField() {
        val textInputLayout = TextInputLayout(this)
        textInputLayout.id = View.generateViewId()
        val layoutParams = LinearLayout.LayoutParams(
            customWidth,
            customHeight
        )

        layoutParams.leftMargin = marginLeftSize
        textInputLayout.layoutParams = layoutParams
        layoutParams.bottomMargin = marginBottomSize
        textInputLayout.layoutParams = layoutParams
        layoutParams.gravity = Gravity.CENTER_VERTICAL

        textInputLayout.hint = " "

        val textInputEditText = TextInputEditText(this)
        textInputEditText.id = View.generateViewId()
        textInputEditText.layoutParams = LinearLayout.LayoutParams(
            customWidth,
            customHeight
        )
        textInputEditText.setPadding(45, 0, 0, 0)

        textInputEditText.setBackgroundResource(R.drawable.input_field_add_pet)
        textInputEditText.setTextColor(ContextCompat.getColor(this, R.color.light_brown))
        textInputEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        textInputEditText.hint = "Enter Here"

        // Set font programmatically
        val typeface = ResourcesCompat.getFont(this, R.font.inter)
        textInputEditText.typeface = typeface

        textInputLayout.addView(textInputEditText)
        binding.contactInputCon.addView(textInputLayout)
        contactInfoTextList.add(textInputEditText)
    }

    private fun navigateToProfilePage() {
        val intent = Intent(this, ProfilePage::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupClickListener() {
        binding.BackButton.setOnClickListener{
            navigateToProfilePage()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        navigateToProfilePage()
    }

}

data class Upload(val downloadUrl: String)