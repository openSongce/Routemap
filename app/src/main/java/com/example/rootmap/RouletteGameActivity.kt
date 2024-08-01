package com.example.rootmap

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import kotlin.random.Random

class RouletteGameActivity : AppCompatActivity() {

    private lateinit var participantNames: ArrayList<String>
    private lateinit var rouletteView: RouletteView
    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roulette_game)

        participantNames = intent.getStringArrayListExtra("participantNames") ?: ArrayList()
        rouletteView = findViewById(R.id.rouletteView)
        resultTextView = findViewById(R.id.textViewResult)

        rouletteView.setNames(participantNames)

        val spinButton: Button = findViewById(R.id.buttonSpin)
        spinButton.setOnClickListener {
            spinRoulette()
        }
    }

    private fun spinRoulette() {
        val spinDuration = 3000L
        val randomAngle = Random.nextInt(360) + 720

        val animator = ObjectAnimator.ofFloat(rouletteView, "rotation", 0f, randomAngle.toFloat())
        animator.duration = spinDuration
        animator.start()

        animator.doOnEnd {
            val index = ((randomAngle % 360) / (360f / participantNames.size)).toInt()
            val exemptedPerson = participantNames[index]
            resultTextView.text = "면제자: $exemptedPerson"
            resultTextView.visibility = View.VISIBLE
        }
    }
}
