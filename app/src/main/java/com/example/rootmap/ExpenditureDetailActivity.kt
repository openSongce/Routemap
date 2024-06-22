package com.example.rootmap

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class ExpenditureDetailActivity : AppCompatActivity() {
    private lateinit var expensesAdapter: ExpensesAdapter
    private lateinit var expensesList: MutableList<Expense>
    private lateinit var firestore: FirebaseFirestore
    private lateinit var totalExpenditureTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenditure_detail)

        val tripname = intent.getStringExtra("tripname")
        val userEmail = intent.getStringExtra("userEmail")

        val btnGoBack: Button = findViewById(R.id.btnGoBack)
        btnGoBack.setOnClickListener {
            finish() // 현재 액티비티를 종료하고 이전 액티비티로
        }

        val tourPlanNameTextView: TextView = findViewById(R.id.tourPlanName)
        tourPlanNameTextView.text = tripname

        totalExpenditureTextView = findViewById(R.id.totalExpenditure)

        expensesList = mutableListOf()
        expensesAdapter = ExpensesAdapter(expensesList, this, userEmail ?: "", tripname ?: "")

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewExpenses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = expensesAdapter

        // DividerItemDecoration 추가
        val dividerItemDecoration =
            DividerItemDecoration(recyclerView.context, LinearLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)

        firestore = FirebaseFirestore.getInstance()

        if (tripname != null && userEmail != null) {
            loadExpenses(userEmail, tripname)
        }
    }

    private fun loadExpenses(userEmail: String, tripname: String) {
        runBlocking {
            try {
                val documentSnapshot =
                    firestore.collection("user").document(userEmail).collection("route")
                        .whereEqualTo("tripname", tripname).get().await()
                if (!documentSnapshot.isEmpty) {
                    val document = documentSnapshot.documents[0]
                    val routeList = document.get("routeList") as? List<Map<String, Any>>
                    var totalExpenditure = 0
                    if (routeList != null) {
                        for (item in routeList) {
                            val name = item["name"] as? String ?: ""
                            val spending = item["spending"] as? String ?: "0"
                            expensesList.add(Expense(name, spending))
                            totalExpenditure += spending.replace(",", "").toIntOrNull() ?: 0
                        }
                        expensesAdapter.notifyDataSetChanged()
                    } else {
                        Log.d(
                            "ExpenditureDetailActivity",
                            "Route list is empty for tripname: $tripname"
                        )
                    }
                    totalExpenditureTextView.text = "${totalExpenditure}원 지출"
                } else {
                    Log.d("ExpenditureDetailActivity", "No documents found for tripname: $tripname")
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error getting documents: ", e)
            }
        }
    }

    fun updateTotalExpenditure(totalExpenditure: Int) {
        totalExpenditureTextView.text = "${totalExpenditure}원 지출"
    }
}
