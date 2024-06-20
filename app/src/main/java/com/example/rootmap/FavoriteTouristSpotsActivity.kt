package com.example.rootmap

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rootmap.databinding.ActivityFavoriteTouristBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class FavoriteTouristSpotsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteTouristBinding
    private lateinit var apiService: TouristApiService
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

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

        fetchSelectedTouristInfo()
    }

    private fun fetchSelectedTouristInfo() {
        coroutineScope.launch {
            val allItems = mutableListOf<TouristItem>()
            val deferredList = mutableListOf<Deferred<Unit>>()
            val areaCodes = listOf(1, 2, 3, 4, 5, 6, 7, 8, 31, 32) // 대표적인 지역 코드만 사용
            val contentTypeIds = listOf(12, 14, 15, 25, 28, 32, 38, 39) // 대표적인 콘텐츠 타입만 사용

            for (areaCode in areaCodes) {
                for (contentTypeId in contentTypeIds) {
                    val deferred = async(Dispatchers.IO) {
                        fetchTouristInfo(areaCode, contentTypeId) { items ->
                            synchronized(allItems) {
                                allItems.addAll(items)
                            }
                        }
                    }
                    deferredList.add(deferred)
                }
            }

            deferredList.awaitAll()
            filterLikedItems(allItems)
        }
    }

    private fun fetchTouristInfo(areaCode: Int, contentTypeId: Int, callback: (List<TouristItem>) -> Unit) {
        apiService.getTouristInfo(
            numOfRows = 10, // 한 번에 적은 수의 데이터를 가져옵니다.
            pageNo = 1, // 첫 번째 페이지의 데이터를 가져옵니다.
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
                    callback(items)
                } else {
                    Log.e("API_ERROR", "Response code: ${response.code()}")
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<TouristResponse>, t: Throwable) {
                Log.e("API_FAILURE", "Failed to fetch data", t)
                callback(emptyList())
            }
        })
    }

    private fun filterLikedItems(allItems: List<TouristItem>) {
        val userId = auth.currentUser?.uid ?: return
        database.child("userLikes").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val likedContentIds = dataSnapshot.children.filter {
                    it.getValue(Boolean::class.java) == true
                }.mapNotNull { it.key }
                val likedItems = allItems.filter { likedContentIds.contains(it.contentid) }
                displayLikedItems(likedItems)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FavoriteTouristSpots", "Failed to fetch liked items", databaseError.toException())
            }
        })
    }

    private fun displayLikedItems(items: List<TouristItem>) {
        val adapter = TouristAdapter(items, database, auth) { item ->
            // 아이템 클릭 시 동작을 정의할 수 있습니다.
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this@FavoriteTouristSpotsActivity)
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
