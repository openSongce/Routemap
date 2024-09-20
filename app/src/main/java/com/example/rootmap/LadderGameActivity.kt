package com.example.rootmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LadderGameActivity : AppCompatActivity() {


    private lateinit var participantInputLayout: LinearLayout
    private lateinit var nameInputLayout: LinearLayout
    private lateinit var amountInputLayout: LinearLayout
    private lateinit var ladderView: LadderView
    private lateinit var seekBarParticipantCount: SeekBar
    private lateinit var textViewSelectedCount: TextView
    private lateinit var buttonNext: Button
    private lateinit var buttonStartGame: View
    private lateinit var textViewResult: TextView
    private lateinit var buttonConfirm: Button
    private var numberOfPeople: Int = 2
    private val names = mutableListOf<String>()
    private val amounts = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ladder_game)

        participantInputLayout = findViewById(R.id.participantInputLayout)
        nameInputLayout = findViewById(R.id.nameInputLayout)
        amountInputLayout = findViewById(R.id.amountInputLayout)
        seekBarParticipantCount = findViewById(R.id.seekBarParticipantCount)
        textViewSelectedCount = findViewById(R.id.textViewSelectedCount)
        buttonNext = findViewById(R.id.buttonNext)
        ladderView = findViewById(R.id.ladderView)
        buttonStartGame = findViewById(R.id.buttonStartGame)
        textViewResult = findViewById(R.id.textViewResult)
        buttonConfirm = findViewById(R.id.buttonConfirm)

        // SeekBar 값 변경 시 선택된 인원 수 업데이트
        seekBarParticipantCount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                numberOfPeople = progress + 2 // 0~6을 2~8로 변환
                textViewSelectedCount.text = "참여 인원: ${numberOfPeople}명"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // "다음" 버튼 클릭 시 입력 필드 생성
        buttonNext.setOnClickListener {
            generateInputFields(numberOfPeople)
            nameInputLayout.visibility = View.VISIBLE
            amountInputLayout.visibility = View.VISIBLE
            buttonStartGame.visibility = View.VISIBLE
        }

        buttonStartGame.setOnClickListener {
            collectInputData()
            startLadderGame()
        }

        buttonConfirm.setOnClickListener {
            finish() // 액티비티 종료
        }
    }

    private fun generateInputFields(count: Int) {
        nameInputLayout.removeAllViews()
        amountInputLayout.removeAllViews()

        for (i in 1..count) {
            val editTextName = EditText(this).apply {
                hint = "이름 $i 입력"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            nameInputLayout.addView(editTextName)

            val editTextAmount = EditText(this).apply {
                hint = "금액 $i 입력"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            amountInputLayout.addView(editTextAmount)
        }
    }

    private fun collectInputData() {
        names.clear()
        amounts.clear()

        for (i in 0 until nameInputLayout.childCount) {
            val nameField = nameInputLayout.getChildAt(i) as EditText
            val amountField = amountInputLayout.getChildAt(i) as EditText

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
        participantInputLayout.visibility = View.GONE
        nameInputLayout.visibility = View.GONE
        amountInputLayout.visibility = View.GONE
        buttonNext.visibility = View.GONE
        buttonStartGame.visibility = View.GONE
        ladderView.visibility = View.VISIBLE

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

            // 게임이 끝난 후 확인 버튼 표시
            buttonConfirm.visibility = View.VISIBLE
        }
    }
}