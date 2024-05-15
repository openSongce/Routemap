package com.example.rootmap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.rootmap.databinding.ActivityFriendBinding
import com.google.android.material.tabs.TabLayoutMediator

class FriendActivity : AppCompatActivity() {
    val binding by lazy {ActivityFriendBinding.inflate(layoutInflater)}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val friendContextList= listOf(FriendList<Any>(),FriendRequest(),FriendAdd())
        val adapter=HomeFragmentAdapter(this)
        adapter.fragmentList=friendContextList
        binding.viewPager.adapter=adapter

        //하단에 탭 바 구성, 클릭 시 해당 프레그먼트로 이동
        val tabTitle= listOf<String>("친구 목록","받은 요청","친구 추가")
        TabLayoutMediator(binding.tabLayout,binding.viewPager){
                tab,position-> tab.text=tabTitle[position]
        }.attach()

        binding.button2.setOnClickListener{ //뒤로가기 버튼
            super.onBackPressed()
        }


    }
}