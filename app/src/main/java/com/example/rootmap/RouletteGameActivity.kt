package com.example.rootmap

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
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
    private lateinit var seekBarParticipantCount: SeekBar
    private lateinit var textViewSelectedCount: TextView
    private lateinit var buttonNext: Button
    private lateinit var buttonSpin: Button
    private lateinit var indicatorIcon: ImageView
    private lateinit var buttonConfirm: Button

    private var participantCount = 2 // 기본값 2명

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roulette_game)

        participantInputLayout = findViewById(R.id.participantInputLayout)
        namesInputLayout = findViewById(R.id.namesInputLayout)
        seekBarParticipantCount = findViewById(R.id.seekBarParticipantCount)
        textViewSelectedCount = findViewById(R.id.textViewSelectedCount)
        buttonNext = findViewById(R.id.buttonNext)
        buttonSpin = findViewById(R.id.buttonSpin)
        resultTextView = findViewById(R.id.textViewResult)
        rouletteView = findViewById(R.id.rouletteView)
        indicatorIcon = findViewById(R.id.indicatorIcon)
        buttonConfirm = findViewById(R.id.buttonConfirm)


        // SeekBar 값이 변경될 때마다 선택된 인원 수를 업데이트
        seekBarParticipantCount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                participantCount = progress + 2 // SeekBar의 값이 0~6이므로 2를 더해 2~8로 변환
                textViewSelectedCount.text = "참여 인원: ${participantCount}명"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        buttonNext.setOnClickListener {
            showNameInputFields(participantCount)
        }

        buttonSpin.setOnClickListener {
            spinRoulette()
        }

        buttonConfirm.setOnClickListener {
            finish() // 액티비티 종료
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
        indicatorIcon.visibility = View.VISIBLE

        // 디버깅: 이름 리스트 확인
        for (name in participantNames) {
            Log.d("RouletteGame", "참여자 이름: $name")
        }

        rouletteView.setNames(participantNames)
    }

    private fun spinRoulette() {
        val spinDuration = 3000L
        val totalRotation = Random.nextInt(360) + 720f // 최소 720도 이상 회전

        // 애니메이션 설정
        val animator = ObjectAnimator.ofFloat(rouletteView, "rotation", rouletteView.rotation, rouletteView.rotation + totalRotation)
        animator.duration = spinDuration
        animator.start()

        animator.doOnEnd {
            // 최종 회전 각도를 0~360도 사이로 변환
            val finalRotation = (rouletteView.rotation % 360 + 360) % 360

            // 룰렛이 반시계 방향으로 그려지므로 이를 고려하여 보정
            val correctedRotation = (finalRotation) % 360

            // 각 이름이 차지하는 각도 계산
            val sweepAngle = 360f / participantNames.size

            // 최종 회전 각도에 해당하는 면제자 인덱스 계산 (반시계 방향이므로 보정 없음)
            val index = ((correctedRotation) / sweepAngle).toInt() % participantNames.size
            val exemptedPerson = participantNames[index]

            // 면제자 정보를 출력
            resultTextView.text = "면제자: $exemptedPerson"
            resultTextView.visibility = View.VISIBLE

            // 게임이 끝난 후 확인 버튼 표시
            buttonConfirm.visibility = View.VISIBLE
        }
    }

}