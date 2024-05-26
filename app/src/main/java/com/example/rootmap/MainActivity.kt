package com.example.rootmap

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapplication.Friend
import com.example.rootmap.databinding.ActivityMainBinding
import com.example.rootmap.databinding.HeaderBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.kakao.vectormap.KakaoMapSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    //private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    lateinit var name: String
    lateinit var nickname: String
    var fdStrage: FirebaseStorage = FirebaseStorage.getInstance()
    var fileUri: Uri? = null
    var backPressedTime:Long =0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val currentId = intent.getStringExtra("id") //intent에서 id 꺼내기
        val navigation: NavigationView = findViewById(R.id.main_navigationView)
        navigation.setNavigationItemSelectedListener(this)
        val head = navigation.getHeaderView(0)
        val userName: TextView = head.findViewById(R.id.menuUserName)
        val userNickame: TextView = head.findViewById(R.id.menuNickname)
        val userProfile: ImageView = head.findViewById(R.id.header_icon)
        KakaoMapSdk.init(this, "adbabacb6eeba95fa1b0adf991f6505c")

        CoroutineScope(Dispatchers.Main).async {
            if (currentId != null) {
                loadMyData(currentId)
                loadImg(currentId.replace(".",""))
            }
            userName.setText(name) //이름
            userNickame.setText(nickname)
            if(fileUri!=null){
                Glide.with(this@MainActivity).load(fileUri).into(userProfile)
            }

        }


        auth = FirebaseAuth.getInstance()
        //binding.emailTv.text = auth.currentUser?.email
        val contextList = listOf(
            MenuFragment.newInstance("param1_value", "param2_value"),
            MenuFragment2.newInstance("param1_value", "param2_value"),
            MenuFragment3.newInstance("param1_value", "param2_value"),
            MenuFragment4.newInstance("param1_value", "param2_value")
        )
        val adapter = HomeFragmentAdapter(this)
        adapter.fragmentList = contextList
        binding.viewPager.adapter = adapter
        binding.viewPager.setUserInputEnabled(false);

        //하단에 탭 바 구성, 클릭 시 해당 프레그먼트로 이동
        val tabTitle = listOf<String>("메인", "게시판", "지도", "마이페이지")
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitle[position]
        }.attach()
        //좌측 상단의 버튼 클릭-> 사이드메뉴 나옴
        binding.menuButton.setOnClickListener() {
            binding.mainDrawerLayout.openDrawer((GravityCompat.START))
            CoroutineScope(Dispatchers.Main).async {
                if (currentId != null) {
                    loadMyData(currentId)
                    loadImg(currentId.replace(".",""))
                }
                userName.setText(name) //이름
                userNickame.setText(nickname)
                if(fileUri!=null){
                    Glide.with(this@MainActivity).load(fileUri).into(userProfile)
                }
            }
        }
        binding.mainNavigationView.setNavigationItemSelectedListener(this) //드로우 사이드 메뉴바 리스너 등록
        var bundle = Bundle()
        bundle.putString("id", currentId)
        for (fragment in contextList) {
            fragment.arguments = bundle
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        //사이드 메뉴 항목 클릭 이벤트 구현
        when (item.itemId) {
            R.id.menuMyRoute -> {
                Toast.makeText(this, "경로 클릭", Toast.LENGTH_SHORT).show()
            }

            R.id.menuRoutePost -> {
                Toast.makeText(this, "게시판 클릭", Toast.LENGTH_SHORT).show()
            }

            R.id.menuRouteMake -> {
                Toast.makeText(this, "만들기 클릭", Toast.LENGTH_SHORT).show()
            }

            R.id.menuPlans -> {
                Toast.makeText(this, "일정 클릭", Toast.LENGTH_SHORT).show()
            }

            R.id.menuMoney -> {
                Toast.makeText(this, "가계부 클릭", Toast.LENGTH_SHORT).show()
            }

            R.id.menuFriend -> {
                val friendIntent = Intent(this, FriendActivity::class.java)
                friendIntent.putExtra("id", intent.getStringExtra("id"))
                startActivity(friendIntent)
            }

            R.id.menuLogout -> {
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
            if(backPressedTime+3000> System.currentTimeMillis()){
                super.onBackPressed()
                finish()
            }else{
                Toast.makeText(this,"한번 더 뒤로가기를 누르면 종료됩니다.",Toast.LENGTH_SHORT).show()
            }
            backPressedTime=System.currentTimeMillis()
        }
    }

    suspend fun loadMyData(id: String): Boolean {
        return try {
            val mydb = Firebase.firestore.collection("user").document(id).get().await()
            name = mydb.data?.get("name").toString()
            nickname = mydb.data?.get("nickname").toString()
            true
        } catch (e: FirebaseException) {
            Log.d("load_error", "error")
            false
        }
    }

    suspend fun loadImg(id: String): Boolean {
        return try {
            fileUri=fdStrage.reference.child("profile/${id}.png").downloadUrl.await()
            true
        } catch (e: FirebaseException) {
            Log.d("img_error", "error")
            //  photoUri=null
            false
        }
    }


}
