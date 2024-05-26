package com.example.rootmap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ExpenditureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenditure)

        val btnGoBack: Button = findViewById(R.id.btnGoBack)
        btnGoBack.setOnClickListener {
            finish() // 현재 액티비티를 종료하고 이전 액티비티로
        }

        val btnTravelItemTest: Button = findViewById(R.id.btnTravelItemTest)
        btnTravelItemTest.setOnClickListener {
            // ExpenditureDetailActivity로 이동
            val intent = Intent(this, ExpenditureDetailActivity::class.java)
            startActivity(intent)
        }
    }
}
