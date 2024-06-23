package com.example.rootmap

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class ExpensesAdapter(
    private val expensesList: MutableList<Expense>,
    private val context: Context,
    private val userEmail: String,
    private val tripname: String
) : RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder>() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expensesList[position])
    }

    override fun getItemCount(): Int {
        return expensesList.size
    }

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.expenseName)
        private val spendingTextView: TextView = itemView.findViewById(R.id.expenseAmount)
        private val editButton: Button = itemView.findViewById(R.id.btnEdit)

        fun bind(expense: Expense) {
            nameTextView.text = expense.name
            val spendingFormatted = NumberFormat.getNumberInstance(Locale.US).format(expense.spending.replace(",", "").toIntOrNull() ?: 0)
            spendingTextView.text = spendingFormatted

            editButton.setOnClickListener {
                showEditDialog(expense)
            }
        }

        private fun showEditDialog(expense: Expense) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_spending, null)
            val spendingEditText: EditText = dialogView.findViewById(R.id.editSpending)

            spendingEditText.setText(expense.spending)

            val dialog = AlertDialog.Builder(context)
                .setTitle("지출 수정")
                .setView(dialogView)
                .setPositiveButton("저장") { _, _ ->
                    val newSpending = spendingEditText.text.toString()
                    val oldSpending = expense.spending
                    expense.spending = newSpending
                    spendingTextView.text = NumberFormat.getNumberInstance(Locale.US).format(newSpending.replace(",", "").toIntOrNull() ?: 0)
                    updateTotalExpenditure()
                    updateFirestore(expense.name, oldSpending, newSpending)
                }
                .setNegativeButton("취소", null)
                .create()

            dialog.show()
        }

        private fun updateTotalExpenditure() {
            val totalExpenditure = expensesList.sumOf { it.spending.replace(",", "").toIntOrNull() ?: 0 }
            (context as ExpenditureDetailActivity).updateTotalExpenditure(totalExpenditure)
        }

        private fun updateFirestore(name: String, oldSpending: String, newSpending: String) {
            firestore.collection("user").document(userEmail).collection("route")
                .whereEqualTo("tripname", tripname).get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val routeList = document.get("routeList") as? MutableList<Map<String, Any>>
                        if (routeList != null) {
                            val mutableRouteList = routeList.toMutableList()
                            for (item in mutableRouteList) {
                                if (item["name"] == name && item["spending"] == oldSpending) {
                                    val mutableItem = item.toMutableMap()
                                    mutableItem["spending"] = newSpending
                                    mutableRouteList[mutableRouteList.indexOf(item)] = mutableItem
                                    break
                                }
                            }
                            document.reference.update("routeList", mutableRouteList)
                        }
                    }
                }
        }
    }
}
