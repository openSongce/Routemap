package com.example.rootmap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.rootmap.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    //private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
     val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        //binding.emailTv.text = auth.currentUser?.email


        val contextList= listOf(MenuFragment(),MenuFragment2(),MenuFragment3(),MenuFragment4())
        val adapter=HomeFragmentAdapter(this)
        adapter.fragmentList=contextList
        binding.viewPager.adapter=adapter

        //하단에 탭 바 구성, 클릭 시 해당 프레그먼트로 이동
        val tabTitle= listOf<String>("메뉴1","메뉴2","메뉴3","기타")
        TabLayoutMediator(binding.tabLayout,binding.viewPager){ tab,position->
            tab.text=tabTitle[position]
        }.attach()


        binding.logoutBtn.setOnClickListener {
            auth.signOut() // 로그아웃 처리
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent) // LoginActivity로 화면 전환
        }
       // */

    }
}
