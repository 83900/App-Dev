package com.example.wonderlog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class DiaryImagePagerAdapter(
    private val context: Context,
    private val imagePaths: List<String>
) : RecyclerView.Adapter<DiaryImagePagerAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_diary_image_fullscreen, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imagePath = imagePaths[position]
        val imageFile = File(context.getExternalFilesDir(null), imagePath)
        if (imageFile.exists()) {
            holder.imageView.setImageURI(android.net.Uri.fromFile(imageFile))
        }
    }

    override fun getItemCount(): Int = imagePaths.size

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_fullscreen_image)
    }
}