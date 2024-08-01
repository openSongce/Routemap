package com.example.rootmap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.rootmap.databinding.ActivitySettlementGameBinding

class SettlementGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettlementGameBinding
    private var participantCount: Int = 0
    private lateinit var participantNames: ArrayList<String>
    private lateinit var settlementAmounts: ArrayList<String>
    private lateinit var nameEditTexts: ArrayList<EditText>
    private lateinit var amountEditTexts: ArrayList<EditText>
    private var selectedGame: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettlementGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonLadderGame.setOnClickListener {
            selectedGame = "LADDER"
            showParticipantCountInput()
        }

        binding.buttonRouletteGame.setOnClickListener {
            selectedGame = "ROULETTE"
            showParticipantCountInput()
        }

        binding.buttonNext.setOnClickListener {
            val countText = binding.editTextParticipantCount.text.toString()
            if (countText.isNotEmpty()) {
                participantCount = countText.toInt()
                showNameInputFields()
            }
        }
    }

    private fun showParticipantCountInput() {
        binding.buttonLadderGame.visibility = View.GONE
        binding.buttonRouletteGame.visibility = View.GONE
        binding.participantInputLayout.visibility = View.VISIBLE
    }

    private fun showNameInputFields() {
        binding.buttonNext.visibility = View.GONE
        binding.editTextParticipantCount.visibility = View.GONE

        nameEditTexts = ArrayList()
        for (i in 1..participantCount) {
            val editText = EditText(this)
            editText.hint = "참여자 이름 $i"
            binding.namesInputLayout.addView(editText)
            nameEditTexts.add(editText)
        }

        val buttonNextStep = Button(this).apply {
            text = if (selectedGame == "LADDER") "다음 단계" else "게임 시작"
            setOnClickListener {
                if (selectedGame == "LADDER") showAmountInputFields()
                else startSelectedGame()
            }
        }
        binding.namesInputLayout.addView(buttonNextStep)

        binding.namesInputLayout.visibility = View.VISIBLE
    }

    private fun showAmountInputFields() {
        binding.namesInputLayout.visibility = View.GONE

        amountEditTexts = ArrayList()
        for (i in 1..participantCount) {
            val editText = EditText(this)
            editText.hint = "정산 금액 $i"
            binding.amountsInputLayout.addView(editText)
            amountEditTexts.add(editText)
        }

        val buttonStartGame = Button(this).apply {
            text = "게임 시작"
            setOnClickListener { startSelectedGame() }
        }
        binding.amountsInputLayout.addView(buttonStartGame)

        binding.amountsInputLayout.visibility = View.VISIBLE
    }

    private fun startSelectedGame() {
        participantNames = ArrayList()
        for (editText in nameEditTexts) {
            participantNames.add(editText.text.toString())
        }

        if (selectedGame == "LADDER") {
            settlementAmounts = ArrayList()
            for (editText in amountEditTexts) {
                settlementAmounts.add(editText.text.toString())
            }
            startLadderGame()
        } else if (selectedGame == "ROULETTE") {
            startRouletteGame()
        }
    }

    private fun startLadderGame() {
        val intent = Intent(this, LadderGameActivity::class.java)
        intent.putStringArrayListExtra("participantNames", participantNames)
        intent.putStringArrayListExtra("settlementAmounts", settlementAmounts)
        startActivity(intent)
    }

    private fun startRouletteGame() {
        val intent = Intent(this, RouletteGameActivity::class.java)
        intent.putStringArrayListExtra("participantNames", participantNames)
        startActivity(intent)
    }
}
