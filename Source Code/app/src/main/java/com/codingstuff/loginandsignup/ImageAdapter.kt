package com.codingstuff.loginandsignup

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ImageAdapter(private val mContext: Context, private val mUploads: List<ImageUpload>) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // RecyclerView.ViewHolder. It represents a single item view in the RecyclerView and holds references to the views within that item view
        // (textViewName and imageView).
        val textViewName: TextView = itemView.findViewById(R.id.card_name)
        val imageView: ImageView = itemView.findViewById(R.id.card_image)
    }

    // The onCreateViewHolder function is overridden to create and return a new instance of the ImageViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false)
        return ImageViewHolder(itemView)
    }

    // The onBindViewHolder function is overridden to bind data to the views within the ImageViewHolder. It takes the current item position (position) and
    // retrieves the corresponding ImageUpload object from the mUploads list. It sets the text of textViewName to the name of the image and uses Picasso
    // library to load the image from the imageUrl into the imageView.
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uploadCurrent = mUploads[position]
        holder.textViewName.text = uploadCurrent.name
        Picasso.get().load(uploadCurrent.imageUrl).fit().centerCrop().into(holder.imageView)

        holder.itemView.setOnClickListener {
            val petCardId = mUploads[position].petCardId
            val intent = Intent(mContext, PetProfilePage::class.java)
            intent.putExtra("petCardId", petCardId)
            mContext.startActivity(intent)
        }
    }

    // return the total number of items in the mUploads list.
    override fun getItemCount(): Int {
        // Return the total number of items in the dataset
        return mUploads.size
    }
}

// stores the two properties
data class ImageUpload(
    val imageUrl: String,
    val name: String,
    val petCardId: String
)