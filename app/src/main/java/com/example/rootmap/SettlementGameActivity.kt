package com.example.rootmap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.rootmap.databinding.ActivitySettlementGameBinding
import java.text.NumberFormat
import java.util.Locale

class SettlementGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettlementGameBinding
    private var participantCount: Int = 0
    private var selectedGame: String = ""
    private var totalExpenditure: Int = 0 // 총 지출 금액

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettlementGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val btnGoBack: ImageButton = findViewById(R.id.btnGoBack)
        btnGoBack.setOnClickListener {
            finish() // 현재 액티비티를 종료하고 이전 액티비티로
        }

        // ExpenditureDetailActivity에서 전달된 totalExpenditure 값을 가져옴
        totalExpenditure = intent.getIntExtra("totalExpenditure", 0)

        binding.buttonLadderGame.setOnClickListener {
            // 사다리 게임 액티비티로 이동
            val intent = Intent(this, LadderGameActivity::class.java)
            startActivity(intent)
        }

        binding.buttonRouletteGame.setOnClickListener {
            // 룰렛 게임 액티비티로 이동
            val intent = Intent(this, RouletteGameActivity::class.java)
            startActivity(intent)
        }

        binding.buttonDutchPay.setOnClickListener {
            selectedGame = "DUTCH_PAY"
            showParticipantCountInput()
        }

        binding.buttonNext.setOnClickListener {
            val countText = binding.editTextParticipantCount.text.toString()
            if (countText.isNotEmpty()) {
                participantCount = countText.toInt()
                if (selectedGame == "DUTCH_PAY") {
                    calculateDutchPay()  // 더치페이 계산
                }
            }
        }
    }

    private fun showParticipantCountInput() {
        binding.settlementGameLayout.visibility = View.GONE
        binding.participantInputLayout.visibility = View.VISIBLE
    }

    private fun calculateDutchPay() {
        // 더치페이 금액 계산
        val dutchPayAmount = totalExpenditure / participantCount

        // 숫자를 포맷팅하여 3자리마다 쉼표를 추가
        val dutchPayAmountFormatted = NumberFormat.getNumberInstance(Locale.US).format(dutchPayAmount)
        val totalExpenditureFormatted = NumberFormat.getNumberInstance(Locale.US).format(totalExpenditure)

        // 다이얼로그로 결과 표시
        val dutchPayMessage = """
            총 지출: ${totalExpenditureFormatted}원
            참여 인원: ${participantCount}명
            1인당 더치페이 금액: ${dutchPayAmountFormatted}원
        """.trimIndent()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("더치페이 결과")
            .setMessage(dutchPayMessage)
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
                finish() // SettlementGameActivity를 종료하고 ExpenditureDetailActivity로 돌아감
            }
            .show()
    }
}
