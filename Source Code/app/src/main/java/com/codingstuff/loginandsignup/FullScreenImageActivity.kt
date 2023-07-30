// FullScreenImageActivity.kt
package com.codingstuff.loginandsignup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.codingstuff.loginandsignup.databinding.ActivityFullScreenImageBinding
import com.squareup.picasso.Picasso


class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullScreenImageBinding

    companion object {
        const val IMAGE_URL_EXTRA = "image_url_extra"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUrl = intent.getStringExtra(IMAGE_URL_EXTRA)
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).into(binding.fullScreenImageView)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.stay, R.anim.stay)
        finish()
    }
}
