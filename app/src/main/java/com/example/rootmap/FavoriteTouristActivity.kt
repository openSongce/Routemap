package com.example.rootmap

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rootmap.databinding.ActivityFavoriteTouristBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class FavoriteTouristActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteTouristBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var apiService: TouristApiService
    private lateinit var adapter: TouristAdapter
    private val likedTouristItems = mutableListOf<TouristItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteTouristBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // Retrofit 초기화
        val retrofit = Retrofit.Builder()
            .baseUrl("https://apis.data.go.kr/B551011/KorService1/")
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()

        apiService = retrofit.create(TouristApiService::class.java)

        adapter = TouristAdapter(likedTouristItems, database, auth) { item ->
            // 좋아요 버튼 클릭 시 처리
            item.isLiked = !item.isLiked
            if (item.isLiked) {
                item.likeCount++
            } else {
                item.likeCount--
                likedTouristItems.remove(item)
            }
            adapter.notifyDataSetChanged()
            item.contentid?.let { contentId ->
                database.child("likes").child(contentId).setValue(item.likeCount)
                database.child("userLikes").child(auth.currentUser?.uid!!).child(contentId)
                    .setValue(item.isLiked)
            }
        }
        binding.recyclerView.adapter = adapter

        fetchFavoriteTouristSpots()
    }

    private fun fetchFavoriteTouristSpots() {
        val userId = auth.currentUser?.uid ?: return
        Log.d("FavoriteTouristActivity", "Fetching favorite tourist spots for user: $userId")
        database.child("userLikes").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val likedContentIds = mutableListOf<String>()
                    snapshot.children.forEach { childSnapshot ->
                        if (childSnapshot.getValue(Boolean::class.java) == true) {
                            likedContentIds.add(childSnapshot.key ?: "")
                        }
                    }
                    Log.d("FavoriteTouristActivity", "Liked content IDs: $likedContentIds")
                    if (likedContentIds.isEmpty()) {
                        Toast.makeText(
                            this@FavoriteTouristActivity,
                            "No favorite tourist spots found",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        fetchTouristInfo(likedContentIds)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FavoriteTouristActivity", "Error fetching data", error.toException())
                }
            })
    }

    private fun fetchTouristInfo(contentIds: List<String>) {
        Log.d("FavoriteTouristActivity", "Fetching tourist info for content IDs: $contentIds")
        contentIds.forEach { contentId ->
            apiService.getTouristInfoById(contentId.toInt())
                .enqueue(object : Callback<TouristItemResponse> {
                    override fun onResponse(
                        call: Call<TouristItemResponse>,
                        response: Response<TouristItemResponse>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.body?.items?.item?.let { item ->
                                Log.d("FavoriteTouristActivity", "Fetched item: $item")
                                item.isLiked = true
                                likedTouristItems.add(item)
                                adapter.notifyDataSetChanged()
                            }
                        } else {
                            Log.e("API_ERROR", "Response code: ${response.code()}")
                            Log.e("API_ERROR", "Response message: ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<TouristItemResponse>, t: Throwable) {
                        Log.e("API_FAILURE", "Failed to fetch data", t)
                    }
                })
        }
    }
}