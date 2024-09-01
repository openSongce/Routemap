package com.example.rootmap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.widget.ViewPager2
import com.example.rootmap.databinding.ActivityFriendBinding
import com.google.android.material.tabs.TabLayoutMediator


class FriendActivity : AppCompatActivity() {
    val binding by lazy { ActivityFriendBinding.inflate(layoutInflater) }
    lateinit var adapter:HomeFragmentAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var friendContextList = listOf(FriendList(), FriendRequest(), FriendAdd())
        adapter = HomeFragmentAdapter(this)
        adapter.fragmentList = friendContextList
        binding.viewPager.adapter = adapter

        var bundle = Bundle()
        bundle.putString("id", intent.getStringExtra("id"))
        for (i in 0..2) {
            friendContextList[i].arguments = bundle
        }
        //하단에 탭 바 구성, 클릭 시 해당 프레그먼트로 이동
        val tabTitle = listOf<String>("친구 목록", "받은 요청", "친구 추가")
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitle[position]
        }.attach()
        binding.backButton.setOnClickListener { //뒤로가기 버튼
            super.onBackPressed()
        }
    }

}
