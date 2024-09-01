package com.example.rootmap

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import kotlin.random.Random // Add this import statement

class RouletteGameActivity : AppCompatActivity() {

    private lateinit var participantNames: ArrayList<String>
    private lateinit var rouletteView: RouletteView
    private lateinit var resultTextView: TextView
    private lateinit var participantInputLayout: LinearLayout
    private lateinit var namesInputLayout: LinearLayout
    private lateinit var editTextParticipantCount: EditText
    private lateinit var buttonNext: Button
    private lateinit var buttonSpin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roulette_game)

        participantInputLayout = findViewById(R.id.participantInputLayout)
        namesInputLayout = findViewById(R.id.namesInputLayout)
        editTextParticipantCount = findViewById(R.id.editTextParticipantCount)
        buttonNext = findViewById(R.id.buttonNext)
        buttonSpin = findViewById(R.id.buttonSpin)
        resultTextView = findViewById(R.id.textViewResult)
        rouletteView = findViewById(R.id.rouletteView)

        buttonNext.setOnClickListener {
            val countText = editTextParticipantCount.text.toString()
            if (countText.isNotEmpty()) {
                val participantCount = countText.toInt()
                showNameInputFields(participantCount)
            }
        }

        buttonSpin.setOnClickListener {
            spinRoulette()
        }
    }

    private fun showNameInputFields(participantCount: Int) {
        participantInputLayout.visibility = View.GONE
        namesInputLayout.removeAllViews()

        for (i in 1..participantCount) {
            val editText = EditText(this)
            editText.hint = "참여자 이름 $i"
            namesInputLayout.addView(editText)
        }

        val buttonStartGame = Button(this).apply {
            text = "룰렛 시작"
            setOnClickListener {
                participantNames = ArrayList()
                for (i in 0 until participantCount) {
                    val editText = namesInputLayout.getChildAt(i) as EditText
                    participantNames.add(editText.text.toString())
                }
                startRouletteGame()
            }
        }
        namesInputLayout.addView(buttonStartGame)

        namesInputLayout.visibility = View.VISIBLE
    }

    private fun startRouletteGame() {
        namesInputLayout.visibility = View.GONE
        rouletteView.visibility = View.VISIBLE
        buttonSpin.visibility = View.VISIBLE
        rouletteView.setNames(participantNames)
    }

    private fun spinRoulette() {
        val spinDuration = 3000L
        val totalRotation = Random.nextInt(360) + 720f // 720도 이상 회전시켜 충분히 돌아가도록 함

        val animator = ObjectAnimator.ofFloat(rouletteView, "rotation", rouletteView.rotation, rouletteView.rotation + totalRotation)
        animator.duration = spinDuration
        animator.start()

        animator.doOnEnd {
            // 최종 회전 각도 계산 (0 ~ 360도 사이로 조정)
            val finalRotation = (rouletteView.rotation % 360 + 360) % 360

            // 각 이름이 차지하는 각도
            val sweepAngle = 360f / participantNames.size

            // 12시 방향을 기준으로 면제자 인덱스 계산
            val index = ((360f - finalRotation + sweepAngle / 2) / sweepAngle).toInt() % participantNames.size
            val exemptedPerson = participantNames[index]

            resultTextView.text = "면제자: $exemptedPerson"
            resultTextView.visibility = View.VISIBLE
        }
    }


}