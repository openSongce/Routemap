package com.example.rootmap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.Locale

class ExpenditureDetailActivity : AppCompatActivity() {
    private lateinit var expensesAdapter: ExpensesAdapter
    private lateinit var expensesList: MutableList<Expense>
    private lateinit var firestore: FirebaseFirestore
    private lateinit var totalExpenditureTextView: TextView
    private lateinit var sharedInfoTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenditure_detail)

        val tripname = intent.getStringExtra("tripname")
        val createdBy = intent.getStringExtra("createdBy")

        val btnGoBack: ImageButton = findViewById(R.id.btnGoBack)
        btnGoBack.setOnClickListener {
            finish() // 현재 액티비티를 종료하고 이전 액티비티로
        }

        val tourPlanNameTextView: TextView = findViewById(R.id.tourPlanName)
        tourPlanNameTextView.text = tripname

        totalExpenditureTextView = findViewById(R.id.totalExpenditure)
        sharedInfoTextView = findViewById(R.id.sharedInfo)

        expensesList = mutableListOf()
        expensesAdapter = ExpensesAdapter(expensesList, this, createdBy ?: "", tripname ?: "")

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewExpenses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = expensesAdapter

        // DividerItemDecoration 추가
        val dividerItemDecoration =
            DividerItemDecoration(recyclerView.context, LinearLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)

        firestore = FirebaseFirestore.getInstance()

        if (tripname != null && createdBy != null) {
            loadExpenses(createdBy, tripname)
        }

        val btnSettlementGame: Button = findViewById(R.id.btnSettlementGame)
        btnSettlementGame.setOnClickListener {
            val intent = Intent(this, SettlementGameActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadExpenses(createdBy: String, tripname: String) {
        runBlocking {
            try {
                val documentSnapshot =
                    firestore.collection("user").document(createdBy).collection("route")
                        .whereEqualTo("tripname", tripname).get().await()
                if (!documentSnapshot.isEmpty) {
                    val document = documentSnapshot.documents[0]
                    val routeList = document.get("routeList") as? List<Map<String, Any>>
                    val sharedList = document.get("shared") as? List<String> ?: emptyList()
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
                    val totalExpenditureFormatted = NumberFormat.getNumberInstance(Locale.US).format(totalExpenditure)
                    totalExpenditureTextView.text = "${totalExpenditureFormatted}원 지출"

                    // 공유된 사람 수와 더치페이 금액 계산
                    if (sharedList.isNotEmpty()) {
                        val totalPeople = sharedList.size + 1
                        val dutchPayAmount = totalExpenditure / totalPeople
                        val dutchPayAmountFormatted = NumberFormat.getNumberInstance(Locale.US).format(dutchPayAmount)
                        sharedInfoTextView.text = "공유: ${totalPeople}명\n더치페이 금액: ${dutchPayAmountFormatted}원"
                        sharedInfoTextView.visibility = TextView.VISIBLE
                    } else {
                        sharedInfoTextView.visibility = TextView.GONE
                    }
                } else {
                    Log.d("ExpenditureDetailActivity", "No documents found for tripname: $tripname")
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error getting documents: ", e)
            }
        }
    }

    fun updateTotalExpenditure(totalExpenditure: Int) {
        val totalExpenditureFormatted = NumberFormat.getNumberInstance(Locale.US).format(totalExpenditure)
        totalExpenditureTextView.text = "${totalExpenditureFormatted}원 지출"
        // 필요 시, 업데이트된 총 지출에 따라 더치페이 금액도 업데이트
        // 단, sharedInfoTextView가 이미 표시되고 있는 경우에만 업데이트
        if (sharedInfoTextView.visibility == TextView.VISIBLE) {
            val totalPeopleText = sharedInfoTextView.text.toString().split("명")[0].split(":")[1].trim()
            val totalPeople = totalPeopleText.toInt()
            val dutchPayAmount = totalExpenditure / totalPeople
            val dutchPayAmountFormatted = NumberFormat.getNumberInstance(Locale.US).format(dutchPayAmount)
            sharedInfoTextView.text = "공유: ${totalPeople}명\n더치페이 금액: ${dutchPayAmountFormatted}원"
        }
    }
}