package com.example.rootmap

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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

    // 리스트 업데이트를 위한 메서드 추가
    fun updateList(newExpenses: List<Expense>) {
        expensesList.clear()
        expensesList.addAll(newExpenses)
        notifyDataSetChanged() // 데이터 변경을 알림
    }

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.expenseName)
        private val spendingTextView: TextView = itemView.findViewById(R.id.expenseAmount)
        private val categoryTextView: TextView = itemView.findViewById(R.id.expenseCategoryText) // 변경된 TextView
        private val editButton: Button = itemView.findViewById(R.id.btnEdit)

        fun bind(expense: Expense) {
            nameTextView.text = expense.name
            val spendingFormatted = NumberFormat.getNumberInstance(Locale.US).format(expense.spending.replace(",", "").toIntOrNull() ?: 0)
            spendingTextView.text = spendingFormatted

            // Firestore에서 가져온 카테고리 값이 있다면 설정, 없다면 '카테고리 없음'을 표시
            if (expense.category.isNotEmpty()) {
                categoryTextView.text = expense.category
            } else {
                categoryTextView.text = "카테고리 없음"
            }

            // 로그 추가 - Expense 객체에 있는 카테고리 값을 확인
            //Log.d("ExpenseDetail", "Loaded category: ${expense.category}")
            //Log.d("ExpenseDetail", "Loaded name: ${expense.name}")

            editButton.setOnClickListener {
                showEditDialog(expense)
            }
        }

        private fun showEditDialog(expense: Expense) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_spending, null)

            val spendingEditText: EditText = dialogView.findViewById(R.id.editSpending)
            val categorySpinner: Spinner = dialogView.findViewById(R.id.categorySpinner) // Spinner 추가

            // 기존 지출 금액 설정
            spendingEditText.setText(expense.spending)

            // Spinner 어댑터 설정 (카테고리 선택)
            val spinnerAdapter = ArrayAdapter.createFromResource(
                context,
                R.array.expense_categories,
                android.R.layout.simple_spinner_item
            )
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = spinnerAdapter

            // Firestore에서 가져온 카테고리 값을 Spinner에 설정 (카테고리가 없으면 '기타'로 설정)
            val categories = context.resources.getStringArray(R.array.expense_categories)
            val category = if (expense.category.isNullOrEmpty()) "기타" else expense.category
            val categoryPosition = categories.indexOf(category)
            if (categoryPosition >= 0) {
                categorySpinner.setSelection(categoryPosition)
            }

            val dialog = AlertDialog.Builder(context)
                .setTitle("지출 수정")
                .setView(dialogView)
                .setPositiveButton("저장") { _, _ ->
                    // 지출 금액 수정
                    val newSpending = spendingEditText.text.toString()
                    val oldSpending = expense.spending
                    expense.spending = newSpending
                    spendingTextView.text = NumberFormat.getNumberInstance(Locale.US).format(newSpending.replace(",", "").toIntOrNull() ?: 0)

                    // 카테고리 수정
                    val selectedCategory = categorySpinner.selectedItem.toString()
                    if (expense.category != selectedCategory) {
                        expense.category = selectedCategory
                        categoryTextView.text = selectedCategory // 수정된 카테고리 TextView에 반영
                    }

                    updateTotalExpenditure()
                    updateFirestore(expense.name, oldSpending, newSpending, selectedCategory)
                }
                .setNegativeButton("취소", null)
                .create()

            dialog.show()
        }


        private fun updateFirestore(name: String, oldSpending: String, newSpending: String, newCategory: String) {
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
                                    mutableItem["category"] = newCategory
                                    mutableRouteList[mutableRouteList.indexOf(item)] = mutableItem
                                    break
                                }
                            }
                            document.reference.update("routeList", mutableRouteList)
                        }
                    }
                }
        }

        private fun updateTotalExpenditure() {
            val totalExpenditure = expensesList.sumOf { it.spending.replace(",", "").toIntOrNull() ?: 0 }
            (context as ExpenditureDetailActivity).updateTotalExpenditure(totalExpenditure)
        }
    }
}