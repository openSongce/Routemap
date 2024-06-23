package com.example.rootmap

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rootmap.databinding.ActivityFavoriteTouristSpotsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class FavoriteTouristSpotsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteTouristSpotsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var apiService: TouristApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteTouristSpotsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Auth 초기화
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Retrofit 초기화
        val retrofit = Retrofit.Builder()
            .baseUrl("https://apis.data.go.kr/B551011/KorService1/")
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()

        apiService = retrofit.create(TouristApiService::class.java)

        // 데이터베이스에서 좋아요한 관광지 가져오기
        fetchFavoriteTouristSpots()
    }

    private fun fetchFavoriteTouristSpots() {
        auth.currentUser?.uid?.let { userId ->
            database.child("userLikes").child(userId)
                .orderByValue().equalTo(true)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val likedTitles = dataSnapshot.children.mapNotNull { it.key }
                        if (likedTitles.isEmpty()) {
                            displayNoFavoritesMessage()
                        } else {
                            likedTitles.forEach { title ->
                                fetchTouristSpotByTitle(title)
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w("FavoriteTouristSpots", "fetchFavoriteTouristSpots:onCancelled", databaseError.toException())
                    }
                })
        }
    }

    private fun fetchTouristSpotByTitle(title: String) {
        lifecycleScope.launch {
            val touristItem = try {
                withContext(Dispatchers.IO) {
                    val response = apiService.searchKeyword(
                        keyword = title,
                        numOfRows = 10,
                        pageNo = 1,
                        mobileOS = "AND",
                        mobileApp = "Test",
                        serviceKey = "iIzVkyvN4jIuoBR82vVZ0iFXlV65w0gsaiuOlUboGQ45v7PnBXkVOsDoBxoqMul10rfSMk7J+X5YKBxqu2ANRQ=="
                    ).execute()
                    if (response.isSuccessful) {
                        response.body()?.body?.items?.item?.firstOrNull { it.title == title }?.apply {
                            // 좋아요 수를 Firebase에서 가져오기
                            likeCount = fetchLikeCount(title)
                            isLiked = true
                        }
                    } else {
                        Log.e("FavoriteTouristSpots", "API call failed: ${response.errorBody()?.string()}")
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e("FavoriteTouristSpots", "Network request failed", e)
                null
            }

            touristItem?.let {
                addTouristItemToUI(it)
            }
        }
    }

    private suspend fun fetchLikeCount(title: String): Int {
        return try {
            val snapshot = database.child("likes").child(title).get().await()
            snapshot.getValue(Int::class.java) ?: 0
        } catch (e: Exception) {
            Log.e("FavoriteTouristSpots", "Failed to fetch like count for $title", e)
            0
        }
    }

    private fun addTouristItemToUI(touristItem: TouristItem) {
        val currentList = (binding.recyclerView.adapter as? TouristAdapter)?.items?.toMutableList() ?: mutableListOf()
        currentList.add(touristItem)
        val adapter = TouristAdapter(currentList, database, auth) { item ->
            // Tourist item click handler (if needed)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        binding.noFavoritesText.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        binding.progressBar2.visibility=View.GONE
    }

    private fun displayNoFavoritesMessage() {
        binding.noFavoritesText.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.progressBar2.visibility=View.GONE
    }

}
