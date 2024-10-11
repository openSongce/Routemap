package com.example.rootmap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
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
import android.view.View

class ExpenditureDetailActivity : AppCompatActivity() {
    private lateinit var expensesAdapter: ExpensesAdapter
    private lateinit var expensesList: MutableList<Expense>
    private lateinit var firestore: FirebaseFirestore
    private lateinit var totalExpenditureTextView: TextView
    private lateinit var sharedInfoTextView: TextView
    private var totalExpenditure: Int = 0 // 총 지출 금액 변수 추가


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
            intent.putExtra("totalExpenditure", totalExpenditure) // 총 지출 금액 전달
            startActivity(intent)
        }

        val spinner: Spinner = findViewById(R.id.daysCategory)

        ArrayAdapter.createFromResource(
            this,
            R.array.days_categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> loadExpenses(createdBy ?: "", tripname ?: "") // 모든 날짜 선택
                    1 -> filterByDay("1") // DAY 1 선택
                    2 -> filterByDay("2") // DAY 2 선택
                    3 -> filterByDay("3") // DAY 3 선택
                    4 -> filterByDay("4") // DAY 4 선택
                    5 -> filterByDay("5") // DAY 5 선택
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                expensesAdapter.updateList(expensesList)
            }
        }

        // 카테고리별 버튼 클릭 이벤트 처리
        val btnAll: Button = findViewById(R.id.btnAll)
        val btnFood: Button = findViewById(R.id.btnFood)
        val btnTransport: Button = findViewById(R.id.btnTransport)
        val btnLodging: Button = findViewById(R.id.btnLodging)
        val btnLeisure: Button = findViewById(R.id.btnLeisure)
        val btnOther: Button = findViewById(R.id.btnOther)

        btnAll.setOnClickListener { filterByCategory("모두 보기") }
        btnFood.setOnClickListener { filterByCategory("식비") }
        btnTransport.setOnClickListener { filterByCategory("교통비") }
        btnLodging.setOnClickListener { filterByCategory("숙박") }
        btnLeisure.setOnClickListener { filterByCategory("여가") }
        btnOther.setOnClickListener { filterByCategory("기타") }
    }

    private var originalExpensesList: MutableList<Expense> = mutableListOf()
    private fun loadExpenses(createdBy: String, tripname: String) {
        runBlocking {
            try {
                val documentSnapshot =
                    firestore.collection("user").document(createdBy).collection("route")
                        .whereEqualTo("tripname", tripname).get().await()

                if (!documentSnapshot.isEmpty) {
                    val document = documentSnapshot.documents[0]
                    val routeList = document.get("routeList") as? List<Map<String, Any>> ?: emptyList()

                    expensesList.clear() // 기존 리스트 초기화
                    totalExpenditure = 0 // 초기화

                    for (item in routeList) {
                        val name = item["name"] as? String ?: ""
                        val spending = item["spending"] as? String ?: "0"
                        val day = item["day"] as? String ?: ""
                        val category = item["category"] as? String ?: ""

                        expensesList.add(Expense(name, spending, day, category))
                        totalExpenditure += spending.replace(",", "").toIntOrNull() ?: 0
                    }

                    originalExpensesList = expensesList.toMutableList() // 원본 리스트 저장
                    expensesAdapter.notifyDataSetChanged() // 어댑터 업데이트

                    val totalExpenditureFormatted = NumberFormat.getNumberInstance(Locale.US).format(totalExpenditure)
                    totalExpenditureTextView.text = "${totalExpenditureFormatted}원 지출"

                    // 더치페이 정보는 이제 표시하지 않음
                    sharedInfoTextView.visibility = TextView.GONE
                } else {
                    Log.d("ExpenditureDetailActivity", "No documents found for tripname: $tripname")
                    // 데이터가 없음을 사용자에게 알릴 방법을 추가할 수 있음
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error getting documents: ", e)
            }
        }
    }

    private fun filterByCategory(category: String) {
        val filteredList = if (category == "모두 보기") {
            originalExpensesList // 원본 목록으로 복원
        } else {
            originalExpensesList.filter { it.category == category } // 원본 데이터에서 필터링
        }

        Log.d("ExpenditureDetail", "Filtered list size: ${filteredList.size}")

        expensesAdapter.updateList(filteredList)
    }

    private fun filterByDay(day: String) {
        val filteredList = expensesList.filter { it.day == day }
        expensesAdapter.updateList(filteredList)
    }

    fun updateTotalExpenditure(totalExpenditure: Int) {
        val totalExpenditureFormatted = NumberFormat.getNumberInstance(Locale.US).format(totalExpenditure)
        totalExpenditureTextView.text = "${totalExpenditureFormatted}원 지출"
        // 필요 시, 업데이트된 총 지출에 따라 더치페이 금액도 업데이트
    }
}