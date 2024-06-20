package com.example.rootmap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rootmap.databinding.ActivityFavoriteTouristBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import kotlin.random.Random

class FavoriteTouristSpotsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteTouristBinding
    private lateinit var apiService: TouristApiService
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteTouristBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrofit 초기화
        val retrofit = Retrofit.Builder()
            .baseUrl("https://apis.data.go.kr/B551011/KorService1/")
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()

        apiService = retrofit.create(TouristApiService::class.java)

        // Firebase Database 초기화
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        fetchTouristInfo(1, 12) // 서울의 지역 코드는 1, 관광지는 12
    }

    private fun fetchTouristInfo(areaCode: Int, contentTypeId: Int) {
        apiService.getTouristInfo(
            numOfRows = 10,
            pageNo = Random.nextInt(1, 100),
            mobileOS = "AND",
            mobileApp = "MobileApp",
            contentTypeId = contentTypeId,
            areaCode = areaCode,
            serviceKey = "iIzVkyvN4jIuoBR82vVZ0iFXlV65w0gsaiuOlUboGQ45v7PnBXkVOsDoBxoqMul10rfSMk7J+X5YKBxqu2ANRQ=="
        ).enqueue(object : Callback<TouristResponse> {
            override fun onResponse(
                call: Call<TouristResponse>,
                response: Response<TouristResponse>
            ) {
                if (response.isSuccessful) {
                    val items = response.body()?.body?.items?.item ?: emptyList()
                    val adapter = TouristAdapter(items, database, auth) { item ->
                        // 아이템 클릭 시 동작을 정의할 수 있습니다.
                    }
                    binding.recyclerView.layoutManager = LinearLayoutManager(this@FavoriteTouristSpotsActivity)
                    binding.recyclerView.adapter = adapter
                }
            }

            override fun onFailure(call: Call<TouristResponse>, t: Throwable) {
                // 에러 처리
            }
        })
    }
}
