package com.example.rootmap

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ExpenditureDetailActivity : AppCompatActivity() {
    private lateinit var expensesAdapter: ExpensesAdapter
    private lateinit var expensesList: MutableList<Expense>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenditure_detail)

        val btnGoBack: Button = findViewById(R.id.btnGoBack)
        btnGoBack.setOnClickListener {
            finish() // 현재 액티비티를 종료하고 이전 액티비티
        }

        //임시
        expensesList = mutableListOf(
            Expense("2024-05-26", "식사~~~~~~~~~~~~~~", "해운대", "10,000"),
            Expense("2024-05-27", "교통", "광안리", "5,000")
        )

        expensesAdapter = ExpensesAdapter(expensesList)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewExpenses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = expensesAdapter


    }
}
