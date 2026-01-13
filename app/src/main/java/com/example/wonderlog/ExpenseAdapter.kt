package com.example.wonderlog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wonderlog.data.ExpenseItem

class ExpenseAdapter(
    private val expenseList: List<ExpenseItem>,
    private val onItemClick: (ExpenseItem) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expenseItem = expenseList[position]
        holder.bind(expenseItem)
        holder.itemView.setOnClickListener {
            onItemClick(expenseItem)
        }
    }

    override fun getItemCount(): Int = expenseList.size

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_expense_icon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_expense_title)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_expense_category)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_expense_date)
        private val tvAmount: TextView = itemView.findViewById(R.id.tv_expense_amount)

        fun bind(expenseItem: ExpenseItem) {
            ivIcon.setImageResource(expenseItem.iconResId)
            tvTitle.text = expenseItem.title
            tvCategory.text = expenseItem.category
            tvDate.text = expenseItem.date
            tvAmount.text = expenseItem.amount
        }
    }
}