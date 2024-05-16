package com.example.rootmap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.example.rootmap.databinding.ActivityMainBinding
import com.example.rootmap.databinding.HeaderBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{
    //private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
     val binding by lazy {ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val currentId=intent.getStringExtra("id") //intent에서 id 꺼내기

        val navigation:NavigationView=findViewById(R.id.main_navigationView)
        navigation.setNavigationItemSelectedListener(this)
        val head=navigation.getHeaderView(0)
        val userName:TextView=head.findViewById(R.id.menuUserName) //이름
        userName.setText(currentId)
        //닉네임

        auth = FirebaseAuth.getInstance()
        //binding.emailTv.text = auth.currentUser?.email
        val contextList= listOf(MenuFragment(),MenuFragment2(),MenuFragment3(),MenuFragment4())
        val adapter=HomeFragmentAdapter(this)
        adapter.fragmentList=contextList
        binding.viewPager.adapter=adapter

        //하단에 탭 바 구성, 클릭 시 해당 프레그먼트로 이동
        val tabTitle= listOf<String>("메인","게시판","지도","마이페이지")
        TabLayoutMediator(binding.tabLayout,binding.viewPager){ tab,position->
            tab.text=tabTitle[position]
        }.attach()
        //좌측 상단의 버튼 클릭-> 사이드메뉴 나옴
        binding.menuButton.setOnClickListener(){
            binding.mainDrawerLayout.openDrawer((GravityCompat.START))
        }

        binding.mainNavigationView.setNavigationItemSelectedListener(this) //드로우 사이드 메뉴바 리스너 등록
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        //사이드 메뉴 항목 클릭 이벤트 구현
        when(item.itemId){
            R.id.menuMyRoute ->{
                Toast.makeText(this,"경로 클릭",Toast.LENGTH_SHORT).show()
            }
            R.id.menuRoutePost ->{
                Toast.makeText(this,"게시판 클릭",Toast.LENGTH_SHORT).show()
            }
            R.id.menuRouteMake ->{
                Toast.makeText(this,"만들기 클릭",Toast.LENGTH_SHORT).show()
            }
            R.id.menuPlans ->{
                Toast.makeText(this,"일정 클릭",Toast.LENGTH_SHORT).show()
            }
            R.id.menuFriend ->{
                val friendIntent = Intent(this,FriendActivity::class.java)
                friendIntent.putExtra("id",intent.getStringExtra("id"))
                startActivity(friendIntent)
            }
            R.id.menuLogout ->{
                auth.signOut() // 로그아웃 처리
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent) // LoginActivity로 화면 전환
            }

        }
        return false
    }
    override fun onBackPressed() {
        if (binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.mainDrawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }



}
