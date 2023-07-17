//package com.codingstuff.loginandsignup
//
//import android.content.Context
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.squareup.picasso.Picasso
//
//class ImageAdapter(private val mContext: Context, private val mUploads: List<ImageUpload>) :
//    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
//
//    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val textViewName: TextView = itemView.findViewById(R.id.card_name)
//        val imageView: ImageView = itemView.findViewById(R.id.card_image)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
//        val itemView = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false)
//        return ImageViewHolder(itemView)
//    }
//
//    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
//        val uploadCurrent = mUploads[position]
//        holder.textViewName.text = uploadCurrent.name
//        Log.d("ImageURL", uploadCurrent.imageUrl) // Print the image URL for debugging
//        Picasso.get().load(uploadCurrent.imageUrl).fit().centerCrop().into(holder.imageView)
//    }
//
//
//    override fun getItemCount(): Int {
//        // Return the total number of items in the dataset
//        return mUploads.size
//    }
//}
//
//data class ImageUpload(
//    val imageUrl: String,
//    val name: String
//)
