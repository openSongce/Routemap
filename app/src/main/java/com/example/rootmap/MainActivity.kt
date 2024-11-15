package com.example.rootmap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.example.rootmap.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.kakao.vectormap.KakaoMapSdk

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var auth: FirebaseAuth
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    lateinit var name: String
    lateinit var nickname: String
    private val manager = supportFragmentManager
    var backPressedTime: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val currentId = intent.getStringExtra("id") //intent에서 id 꺼내기
        KakaoMapSdk.init(this, "adbabacb6eeba95fa1b0adf991f6505c")
        auth = FirebaseAuth.getInstance()
        val contextList = listOf(
            MenuFragment.newInstance("param1_value", "param2_value"),
            MenuFragment2.newInstance("param1_value", "param2_value"),
            MenuFragment3.newInstance("param1_value", "param2_value"),
            MenuFragment4.newInstance("param1_value", "param2_value")
        )
        val adapter = HomeFragmentAdapter(this)
        adapter.fragmentList = contextList
        binding.run {
            mainFrm.adapter = adapter
            mainFrm.setUserInputEnabled(false)
            pageName.text = "메인"
        }
        binding.mainFrm.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    binding.mainBottomNav.menu.getItem(position).isChecked = true
                }
            }
        )
        binding.mainBottomNav.setOnNavigationItemSelectedListener(this)
        //하단에 탭 바 구성, 클릭 시 해당 프레그먼트로 이동
        binding.mainBottomNav.itemIconTintList = null
        binding.mainBottomNav.background = null
        binding.mainBottomNav.menu.getItem(2).isEnabled = false
        binding.mainBottomNav.setOnItemReselectedListener { }
        var bundle = Bundle()
        bundle.putString("id", currentId)
        for (fragment in contextList) {
            fragment.arguments = bundle
        }

        binding.mainFloatingAddBtn.setOnClickListener{
            //Toast.makeText(this, "클릭", Toast.LENGTH_SHORT).show()


        }

    }

    override fun onBackPressed() {
        if (backPressedTime + 3000 > System.currentTimeMillis()) {
            super.onBackPressed()
            finish()
        } else {
            Toast.makeText(this, "한번 더 뒤로가기를 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_tourist -> {
                binding.mainFrm.currentItem = 0
                binding.pageName.text = "관광지 추천"
                return true
            }
            R.id.menu_post -> {
                binding.mainFrm.currentItem = 1
                binding.pageName.text = "게시판"
                return true
            }
            R.id.menu_map -> {
                binding.mainFrm.currentItem = 2
                binding.pageName.text = "지도"
                return true
            }
            R.id.menu_myPage -> {
                binding.mainFrm.currentItem = 3
                binding.pageName.text = "마이페이지"
                return true
            }
            else -> return false
        }
    }

    // MainActivity.kt
    fun navigateToMenuFragment3WithTitle(title: String, addr1: String? = null, addr2: String) {
        binding.mainFrm.currentItem = 2
        val fragment = supportFragmentManager.findFragmentByTag("f2") as? MenuFragment3
        fragment?.let {
            Log.d("navigateToMenuFragment3WithTitle", "Title: $title, Addr1: $addr1, Addr2: $addr2")
            it.setTitleAndSearch(title, addr1, addr2)
        }
    }
}