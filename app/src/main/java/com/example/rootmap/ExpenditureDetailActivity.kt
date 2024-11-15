package com.example.rootmap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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

class ExpenditureDetailActivity : AppCompatActivity() {
    private lateinit var expensesAdapter: ExpensesAdapter
    private lateinit var expensesList: MutableList<Expense>
    private lateinit var firestore: FirebaseFirestore
    private lateinit var totalExpenditureTextView: TextView
    private lateinit var sharedInfoTextView: TextView
    private var totalExpenditure: Int = 0 // 총 지출 금액 변수 추가

    private var originalExpensesList: MutableList<Expense> = mutableListOf()
    private var selectedCategory: String = "모두 보기" // 현재 선택된 카테고리
    private var selectedDay: String = "모든 날짜" // 현재 선택된 날짜

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

        // 날짜에 해당하는 스피너 생성
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
                selectedDay = when (position) {
                    0 -> "모든 날짜"
                    1 -> "DAY 1"
                    2 -> "DAY 2"
                    3 -> "DAY 3"
                    4 -> "DAY 4"
                    5 -> "DAY 5"
                    else -> "모든 날짜"
                }
                filterExpenses() // 날짜 선택 시 필터 적용
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedDay = "모든 날짜"
                filterExpenses()
            }
        }

        // 카테고리별 버튼 클릭 이벤트 처리
        findViewById<Button>(R.id.btnAll).setOnClickListener {
            selectedCategory = "모두 보기"
            filterExpenses() // 카테고리 변경 시 필터 적용
        }
        findViewById<Button>(R.id.btnFood).setOnClickListener {
            selectedCategory = "식비"
            filterExpenses()
        }
        findViewById<Button>(R.id.btnTransport).setOnClickListener {
            selectedCategory = "교통비"
            filterExpenses()
        }
        findViewById<Button>(R.id.btnLodging).setOnClickListener {
            selectedCategory = "숙박"
            filterExpenses()
        }
        findViewById<Button>(R.id.btnLeisure).setOnClickListener {
            selectedCategory = "여가"
            filterExpenses()
        }
        findViewById<Button>(R.id.btnOther).setOnClickListener {
            selectedCategory = "기타"
            filterExpenses()
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

                    sharedInfoTextView.visibility = TextView.GONE // 더치페이 정보 숨기기
                } else {
                    Log.d("ExpenditureDetailActivity", "No documents found for tripname: $tripname")
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error getting documents: ", e)
            }
        }
    }

    /**
     * 날짜와 카테고리 조건을 모두 적용한 필터링 메서드
     */
    private fun filterExpenses() {
        val filteredList = originalExpensesList.filter { expense ->
            (selectedDay == "모든 날짜" || expense.day == selectedDay) &&
                    (selectedCategory == "모두 보기" || expense.category == selectedCategory)
        }

        expensesAdapter.updateList(filteredList)

        // 필터링된 지출 총계 계산
        val filteredTotalExpenditure = filteredList.sumOf { it.spending.replace(",", "").toIntOrNull() ?: 0 }

        // 지출 총계를 업데이트
        updateTotalExpenditure(filteredTotalExpenditure)
    }

    fun updateTotalExpenditure(totalExpenditure: Int) {
        val totalExpenditureFormatted = NumberFormat.getNumberInstance(Locale.US).format(totalExpenditure)
        totalExpenditureTextView.text = "${totalExpenditureFormatted}원 지출"
    }
}
