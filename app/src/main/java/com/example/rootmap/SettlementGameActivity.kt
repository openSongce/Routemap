package com.example.rootmap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
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

        // ExpenditureDetailActivity에서 전달된 totalExpenditure 값을 가져옴
        totalExpenditure = intent.getIntExtra("totalExpenditure", 0)

        binding.buttonLadderGame.setOnClickListener {
            selectedGame = "LADDER"
            showParticipantCountInput()
        }

        binding.buttonRouletteGame.setOnClickListener {
            selectedGame = "ROULETTE"
            showParticipantCountInput()
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
                } else {
                    showNameInputFields()
                }
            }
        }
    }

    private fun showParticipantCountInput() {
        binding.buttonLadderGame.visibility = View.GONE
        binding.buttonRouletteGame.visibility = View.GONE
        binding.buttonDutchPay.visibility = View.GONE
        binding.participantInputLayout.visibility = View.VISIBLE
    }

    private fun showNameInputFields() {
        binding.buttonNext.visibility = View.GONE
        binding.editTextParticipantCount.visibility = View.GONE

        // 참여자 이름 입력 필드는 Ladder와 Roulette 게임에서만 필요합니다.
        if (selectedGame != "DUTCH_PAY") {
            for (i in 1..participantCount) {
                val editText = EditText(this)
                editText.hint = "참여자 이름 $i"
                binding.namesInputLayout.addView(editText)
            }

            val buttonNextStep = Button(this).apply {
                text = "게임 시작"
                setOnClickListener {
                    startSelectedGame()
                }
            }
            binding.namesInputLayout.addView(buttonNextStep)

            binding.namesInputLayout.visibility = View.VISIBLE
        }
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

    private fun startSelectedGame() {
        val participantNames = ArrayList<String>()
        for (i in 0 until participantCount) {
            val editText = binding.namesInputLayout.getChildAt(i) as EditText
            participantNames.add(editText.text.toString())
        }

        when (selectedGame) {
            "LADDER" -> startLadderGame(participantNames)
            "ROULETTE" -> startRouletteGame(participantNames)
        }
    }

    private fun startLadderGame(participantNames: ArrayList<String>) {
        val intent = Intent(this, LadderGameActivity::class.java)
        intent.putStringArrayListExtra("participantNames", participantNames)
        startActivity(intent)
    }

    private fun startRouletteGame(participantNames: ArrayList<String>) {
        val intent = Intent(this, RouletteGameActivity::class.java)
        intent.putStringArrayListExtra("participantNames", participantNames)
        startActivity(intent)
    }
}
