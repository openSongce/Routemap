package com.example.rootmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Expense(val date: String, val itemName: String, val location: String, val amount: String)

class ExpensesAdapter(private val expenses: List<Expense>) : RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvExpenseDate: TextView = itemView.findViewById(R.id.tvExpenseDate)
        val tvExpenseItemName: TextView = itemView.findViewById(R.id.tvExpenseItemName)
        val tvExpenseLocation: TextView = itemView.findViewById(R.id.tvExpenseLocation)
        val tvExpenseAmount: TextView = itemView.findViewById(R.id.tvExpenseAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val currentItem = expenses[position]
        holder.tvExpenseDate.text = currentItem.date
        holder.tvExpenseItemName.text = currentItem.itemName
        holder.tvExpenseLocation.text = currentItem.location
        holder.tvExpenseAmount.text = currentItem.amount
    }

    override fun getItemCount() = expenses.size
}
