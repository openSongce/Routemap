package com.example.rootmap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.rootmap.databinding.ActivityFriendBinding
import com.google.android.material.tabs.TabLayoutMediator

class FriendActivity : AppCompatActivity() {
    val binding by lazy {ActivityFriendBinding.inflate(layoutInflater)}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val friendContextList= listOf(FriendList(),FriendRequest(),FriendAdd())
        val adapter=HomeFragmentAdapter(this)
        adapter.fragmentList=friendContextList
        binding.viewPager.adapter=adapter
/*
       var bundleList= listOf(Bundle(),Bundle(),Bundle())
       bundleList[0].putString("mode","List")
       bundleList[1].putString("mode","Request")
       for(i in 0..2){
           bundleList[i].putString("id",intent.getStringExtra("id"))
           friendContextList[i].arguments=bundleList[i]
       }
*/

    var bundle=Bundle()
       bundle.putString("id",intent.getStringExtra("id"))
       for(i in 0..2){
           friendContextList[i].arguments=bundle
       }


      // Toast.makeText(this,"친구 ${intent.getStringExtra("id")}", Toast.LENGTH_SHORT).show()//intent 확인용 , 후에 삭제

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