package com.example.rootmap

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rootmap.databinding.ActivityMyRouteBinding

class MyRouteActivity : AppCompatActivity() {
    val binding by lazy { ActivityMyRouteBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        binding.backButton.setOnClickListener { //뒤로가기 버튼
            super.onBackPressed()
        }
    }
}