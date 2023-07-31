package com.codingstuff.loginandsignup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class CardAdapter(private val nContext: Context, private val nUploads: List<CardUpload>, private val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(userId: String, petCardId: String)
//        fun onPetItemClick(userId: String, petCardId: String)
    }

    private var connectivityCallbackRegistered = false

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // RecyclerView.ViewHolder. It represents a single item view in the RecyclerView and holds references to the views within that item view
        // (textViewName and imageView).
        val dataViewName: TextView = itemView.findViewById(R.id.animal_name)
        val dataViewGenderAge: TextView = itemView.findViewById(R.id.gender_age)
        val dataViewLocation: TextView = itemView.findViewById(R.id.anim_location)

        val dataViewBio: TextView = itemView.findViewById(R.id.animal_bio)
        val imageView: ImageView = itemView.findViewById(R.id.animal_image)
    }

    // The onCreateViewHolder function is overridden to create and return a new instance of the ImageViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val itemView = LayoutInflater.from(nContext).inflate(R.layout.card_item, parent, false)
        return CardViewHolder(itemView)
    }

    // The onBindViewHolder function is overridden to bind data to the views within the ImageViewHolder. It takes the current item position (position) and
    // retrieves the corresponding ImageUpload object from the mUploads list. It sets the text of textViewName to the name of the image and uses Picasso
    // library to load the image from the imageUrl into the imageView.
    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val uploadCurrent = nUploads[position]
        holder.dataViewName.text = uploadCurrent.name
        holder.dataViewGenderAge.text = uploadCurrent.genderAge
        holder.dataViewLocation.text = uploadCurrent.location
        holder.dataViewBio.text = uploadCurrent.bio
        Picasso.get().load(uploadCurrent.imageUrl).fit().centerCrop().into(holder.imageView)

        holder.itemView.setOnClickListener {
            val userId = nUploads[position].userId
            val petCardId = nUploads[position].petCardId
            itemClickListener.onItemClick(userId, petCardId)
//            itemClickListener.onPetItemClick(userId, petCardId)
        }
    }

    // return the total number of items in the mUploads list.
    override fun getItemCount(): Int {
        // Return the total number of items in the dataset
        return nUploads.size
    }

    // Register the connectivity callback when the adapter is attached to RecyclerView
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (!connectivityCallbackRegistered) {
            ConnectivityUtils.registerConnectivityCallback(nContext)
            connectivityCallbackRegistered = true
        }
    }

    // Unregister the connectivity callback when the adapter is detached from RecyclerView
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        if (connectivityCallbackRegistered) {
            ConnectivityUtils.unregisterConnectivityCallback()
            connectivityCallbackRegistered = false
        }
    }
}


// stores the two properties
data class CardUpload(
    val imageUrl: String,
    val name: String,
    val genderAge: String,
    val location: String,
    val bio: String,
    val userId: String,
    val petCardId: String
)