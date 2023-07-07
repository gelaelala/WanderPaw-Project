package com.codingstuff.loginandsignup

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.ImageView
import com.squareup.picasso.Picasso

object FileSelectionUtils {
    private const val pickImageRequest = 1

    fun openFileChooser(activity: Activity) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        activity.startActivityForResult(intent, pickImageRequest)
    }

    fun handleFileSelectionResult(requestCode: Int, resultCode: Int, data: Intent?, activity: Activity, imageView: ImageView) {
        if (requestCode == pickImageRequest && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            Picasso.get().load(imageUri).into(imageView)
        }
    }

    fun getFileExtension(contentResolver: ContentResolver, uri: Uri): String? {
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }
}
