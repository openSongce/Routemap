package com.example.rootmap

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rootmap.databinding.ActivityLikedPostsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath

class LikedPostsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLikedPostsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var likedPostsAdapter: RouteListAdapter
    private val likedPosts: MutableList<RoutePost> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLikedPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("LikedPostsActivity", "onCreate called")

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        likedPostsAdapter = RouteListAdapter().apply {
            postMode = true
            postList = likedPosts
        }

        binding.likedPostsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@LikedPostsActivity)
            adapter = likedPostsAdapter
        }

        likedPostsAdapter.setItemClickListener(object : RouteListAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                // Item click handling
            }

            override fun onButtonClick(v: View, position: Int) {
                // Button click handling
            }

            override fun heartClick(v: View, position: Int) {
                val post = likedPostsAdapter.postList[position]
                handleHeartClick(post)
            }
        })

        loadLikedPosts()
    }

    private fun loadLikedPosts() {
        Log.d("LikedPostsActivity", "loadLikedPosts called")
        val userId = auth.currentUser?.uid ?: return
        val userLikesRef = database.child("userPostLikes").child(userId)

        userLikesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val likedPostIds = mutableListOf<String>()
                for (postSnapshot in dataSnapshot.children) {
                    if (postSnapshot.getValue(Boolean::class.java) == true) {
                        likedPostIds.add(postSnapshot.key.toString())
                    }
                }
                Log.d("LikedPostsActivity", "Liked post IDs: $likedPostIds")
                fetchLikedPosts(likedPostIds)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("LikedPostsActivity", "Error fetching liked post IDs", databaseError.toException())
            }
        })
    }

    private fun fetchLikedPosts(likedPostIds: List<String>) {
        if (likedPostIds.isEmpty()) {
            Log.d("LikedPostsActivity", "No liked posts found")
            return
        }
        Log.d("LikedPostsActivity", "Fetching liked posts from Firestore")
        FirebaseFirestore.getInstance().collection("route")
            .whereIn(FieldPath.documentId(), likedPostIds)
            .get()
            .addOnSuccessListener { snapshot ->
                likedPosts.clear()
                for (document in snapshot.documents) {
                    val post = document.toObject(RoutePost::class.java)
                    if (post != null) {
                        post.docId = document.id
                        likedPosts.add(post)
                    } else {
                        Log.e("LikedPostsActivity", "Error parsing document: ${document.id}")
                    }
                }
                Log.d("LikedPostsActivity", "Fetched liked posts: $likedPosts")
                fetchLikeCounts(likedPosts)
            }
            .addOnFailureListener { exception ->
                Log.e("LikedPostsActivity", "Error fetching liked posts", exception)
            }
    }

    private fun fetchLikeCounts(posts: List<RoutePost>) {
        Log.d("LikedPostsActivity", "Fetching like counts for posts")
        for (post in posts) {
            val postRef = database.child("postLikes").child(post.docId)
            postRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    post.like = dataSnapshot.getValue(Int::class.java) ?: 0
                    likedPostsAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("LikedPostsActivity", "Error fetching like count", databaseError.toException())
                }
            })

            val userId = auth.currentUser?.uid ?: continue
            val userLikeRef = database.child("userPostLikes").child(userId).child(post.docId)
            userLikeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    post.isLiked = dataSnapshot.getValue(Boolean::class.java) ?: false
                    likedPostsAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("LikedPostsActivity", "Error fetching user like status", databaseError.toException())
                }
            })
        }
    }

    private fun handleHeartClick(post: RoutePost) {
        val userId = auth.currentUser?.uid ?: return
        val postRef = database.child("postLikes").child(post.docId)
        val userLikeRef = database.child("userPostLikes").child(userId).child(post.docId)

        post.isLiked = !post.isLiked
        if (post.isLiked) {
            post.like += 1
        } else {
            post.like -= 1
        }
        likedPostsAdapter.notifyItemChanged(likedPosts.indexOf(post))

        userLikeRef.setValue(post.isLiked).addOnCompleteListener {
            postRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val currentLikes = currentData.getValue(Int::class.java) ?: 0
                    currentData.value = if (post.isLiked) currentLikes + 1 else currentLikes - 1
                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    databaseError: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (databaseError != null) {
                        Log.e("LikedPostsActivity", "Failed to update likes", databaseError.toException())
                    }
                }
            })
        }
    }
}
