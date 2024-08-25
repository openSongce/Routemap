package com.example.rootmap
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LadderGameActivity : AppCompatActivity() {

    private lateinit var layoutInputFields: LinearLayout
    private lateinit var ladderView: LadderView
    private lateinit var editTextNumberOfPeople: EditText
    private lateinit var buttonSubmitNumber: View
    private lateinit var buttonStartGame: View
    private lateinit var textViewResult: TextView
    private var numberOfPeople: Int = 0
    private val names = mutableListOf<String>()
    private val amounts = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ladder_game)

        editTextNumberOfPeople = findViewById(R.id.editTextNumberOfPeople)
        buttonSubmitNumber = findViewById(R.id.buttonSubmitNumber)
        layoutInputFields = findViewById(R.id.layoutInputFields)
        ladderView = findViewById(R.id.ladderView)
        buttonStartGame = findViewById(R.id.buttonStartGame)
        textViewResult = findViewById(R.id.textViewResult)

        buttonSubmitNumber.setOnClickListener {
            numberOfPeople = editTextNumberOfPeople.text.toString().toIntOrNull() ?: 0
            if (numberOfPeople > 0) {
                generateInputFields(numberOfPeople)
                layoutInputFields.visibility = View.VISIBLE
                buttonStartGame.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "유효한 인원 수를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        buttonStartGame.setOnClickListener {
            collectInputData()
            startLadderGame()
        }
    }

    private fun generateInputFields(count: Int) {
        layoutInputFields.removeAllViews()
        for (i in 1..count) {
            val editTextName = EditText(this).apply {
                hint = "이름 $i 입력"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            layoutInputFields.addView(editTextName)

            val editTextAmount = EditText(this).apply {
                hint = "금액 $i 입력"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            layoutInputFields.addView(editTextAmount)
        }
    }

    private fun collectInputData() {
        names.clear()
        amounts.clear()

        for (i in 0 until layoutInputFields.childCount step 2) {
            val nameField = layoutInputFields.getChildAt(i) as EditText
            val amountField = layoutInputFields.getChildAt(i + 1) as EditText

            val name = nameField.text.toString()
            val amount = amountField.text.toString().toIntOrNull() ?: 0

            if (name.isNotEmpty() && amount > 0) {
                names.add(name)
                amounts.add(amount)
            }
        }
    }

    private fun startLadderGame() {
        if (names.size != numberOfPeople || amounts.size != numberOfPeople) {
            Toast.makeText(this, "모든 이름과 금액을 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 입력 필드와 버튼 숨기기
        editTextNumberOfPeople.visibility = View.GONE
        buttonSubmitNumber.visibility = View.GONE
        layoutInputFields.visibility = View.GONE
        buttonStartGame.visibility = View.GONE

        val ladderLines = ladderView.generateComplexLadderLines(numberOfPeople)

        // 사다리 데이터와 초기 이름, 금액 설정
        ladderView.setLadderData(numberOfPeople, ladderLines, names, amounts)

        // 애니메이션 시작 및 결과 표시
        ladderView.startPlayerAnimationSequentially {
            // 최종 위치에 따른 금액을 텍스트뷰에 표시
            val results = ladderView.calculateLadderResults().mapIndexed { index, finalPosition ->
                "${names[index]} -> ${amounts[finalPosition - 1]}"
            }

            textViewResult.text = results.joinToString("\n")
        }
    }
}