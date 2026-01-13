package com.example.wonderlog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wonderlog.data.DiaryItem

class DiaryAdapter(
    private val diaryList: List<DiaryItem>,
    private val onItemClick: (DiaryItem) -> Unit
) : RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_diary, parent, false)
        return DiaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        val diaryItem = diaryList[position]
        holder.bind(diaryItem)
        holder.itemView.setOnClickListener {
            onItemClick(diaryItem)
        }
    }

    override fun getItemCount(): Int = diaryList.size

    class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_diary_title)
        private val tvContent: TextView = itemView.findViewById(R.id.tv_diary_content)
        private val tvLocation: TextView = itemView.findViewById(R.id.tv_diary_location)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_diary_date)

        fun bind(diaryItem: DiaryItem) {
            tvTitle.text = diaryItem.title
            tvContent.text = diaryItem.content
            tvLocation.text = diaryItem.location
            tvDate.text = diaryItem.date
        }
    }
}