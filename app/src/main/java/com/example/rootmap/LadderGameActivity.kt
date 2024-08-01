package com.example.rootmap

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LadderGameActivity : AppCompatActivity() {

    private lateinit var participantNames: ArrayList<String>
    private lateinit var settlementAmounts: ArrayList<String>
    private lateinit var results: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ladder_game)

        participantNames = intent.getStringArrayListExtra("participantNames") ?: ArrayList()
        settlementAmounts = intent.getStringArrayListExtra("settlementAmounts") ?: ArrayList()
        results = playLadderGame(participantNames, settlementAmounts)

        val textView = findViewById<TextView>(R.id.textViewResult)
        textView.text = results.joinToString(separator = "\n")
    }

    private fun playLadderGame(names: ArrayList<String>, amounts: ArrayList<String>): ArrayList<String> {
        val shuffledAmounts = amounts.shuffled()
        val results = ArrayList<String>()

        for (i in names.indices) {
            val result = "${names[i]} -> ${shuffledAmounts[i]}원"
            results.add(result)
        }

        return results
    }
}
