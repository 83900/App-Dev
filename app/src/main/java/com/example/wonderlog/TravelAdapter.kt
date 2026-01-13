package com.example.wonderlog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wonderlog.data.TravelItem

class TravelAdapter(
    private val travelList: List<TravelItem>,
    private val onItemClick: (TravelItem) -> Unit
) : RecyclerView.Adapter<TravelAdapter.TravelViewHolder>() {

    inner class TravelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.text_view_travel_title)
        val locationTextView: TextView = itemView.findViewById(R.id.text_view_travel_location)
        val dateTextView: TextView = itemView.findViewById(R.id.text_view_travel_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_travel, parent, false)
        return TravelViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TravelViewHolder, position: Int) {
        val currentItem = travelList[position]
        holder.titleTextView.text = currentItem.title
        // 将locations列表转换为字符串，用逗号分隔
        holder.locationTextView.text = currentItem.locations.joinToString(", ")
        holder.dateTextView.text = currentItem.date
        
        holder.itemView.setOnClickListener {
            onItemClick(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return travelList.size
    }
}