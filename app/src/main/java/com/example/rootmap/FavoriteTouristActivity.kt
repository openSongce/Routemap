package com.example.rootmap

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rootmap.databinding.ActivityFavoriteTouristBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FavoriteTouristActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteTouristBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteTouristBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        fetchFavoriteTouristSpots()
    }

    private fun fetchFavoriteTouristSpots() {
        val userId = auth.currentUser?.uid ?: return
        database.child("userLikes").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val likedContentIds = mutableListOf<String>()
                    snapshot.children.forEach { childSnapshot ->
                        if (childSnapshot.getValue(Boolean::class.java) == true) {
                            likedContentIds.add(childSnapshot.key ?: "")
                        }
                    }
                    if (likedContentIds.isEmpty()) {
                        Toast.makeText(this@FavoriteTouristActivity, "No favorite tourist spots found", Toast.LENGTH_SHORT).show()
                    } else {
                        displayFavoriteTouristSpots(likedContentIds)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FavoriteTouristActivity", "Error fetching data", error.toException())
                }
            })
    }

    private fun displayFavoriteTouristSpots(contentIds: List<String>) {
        val textView = binding.textViewFavorites
        textView.text = contentIds.joinToString("\n")
    }
}
