package com.example.rootmap

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rootmap.databinding.ActivityLikedPostsBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LikedPostsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLikedPostsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var likedPostsAdapter: RouteListAdapter
    private val likedPosts: MutableList<RoutePost> = mutableListOf()
    private val likedPostsCopy: MutableList<RoutePost> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLikedPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        likedPostsAdapter = RouteListAdapter().apply {
            postMode = true
            postList = likedPosts
        }

        binding.postListView.apply {
            layoutManager = LinearLayoutManager(this@LikedPostsActivity)
            adapter = likedPostsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
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

        lifecycleScope.launch {
            loadLikedPostIdsAndFetchPosts()
        }
    }

    private suspend fun loadLikedPostIdsAndFetchPosts() {
        val userId = auth.currentUser?.uid ?: return
        val likedPostIds = mutableListOf<String>()

        val userLikesRef = database.child("userPostLikes").child(userId)
        userLikesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    if (postSnapshot.getValue(Boolean::class.java) == true) {
                        likedPostIds.add(postSnapshot.key.toString())
                    }
                }
                lifecycleScope.launch {
                    loadLikedPosts(likedPostIds)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("LikedPostsActivity", "Error fetching liked post IDs", databaseError.toException())
            }
        })
    }

    private suspend fun loadLikedPosts(likedPostIds: List<String>): Boolean {
        likedPosts.clear()
        likedPostsCopy.clear()
        return try {
            if (likedPostIds.isEmpty()) {
                likedPostsAdapter.notifyDataSetChanged()
                return true
            }

            val postListData = Firebase.firestore.collection("route")
                .whereIn(FieldPath.documentId(), likedPostIds)
                .get()
                .await()

            if (!postListData.isEmpty) {
                postListData.forEach { document ->
                    val user = document.data["owner"].toString()
                    val data = RoutePost(
                        document.data["tripname"].toString(),
                        0,
                        document.id,
                        user,
                        loadUserName(user),
                        document.data["option"] as List<String>,
                        timestamp = document.getLong("timestamp") ?: 0L // Load the timestamp field
                    )
                    loadLikeStatus(data)
                    likedPosts.add(data)
                    likedPostsCopy.add(data)
                }
            }
            // Sort the list based on the current sorting order
            sortPostList()
            likedPostsAdapter.notifyDataSetChanged()
            true
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
            false
        }
    }

    private suspend fun loadUserName(id: String): String {
        return try {
            val nickname = Firebase.firestore.collection("user").document(id).get().await().get("nickname").toString()
            nickname
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
            "error"
        }
    }

    private fun loadLikeStatus(post: RoutePost) {
        val userId = auth.currentUser?.uid ?: return
        val postRef = database.child("postLikes").child(post.docId)
        postRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                post.like = dataSnapshot.getValue(Int::class.java) ?: 0
                likedPostsAdapter.notifyItemChanged(likedPosts.indexOf(post))
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("LikedPostsActivity", "loadLikeStatus:onCancelled", databaseError.toException())
            }
        })

        val userLikeRef = database.child("userPostLikes").child(userId).child(post.docId)
        userLikeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                post.isLiked = dataSnapshot.getValue(Boolean::class.java) ?: false
                likedPostsAdapter.notifyItemChanged(likedPosts.indexOf(post))
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("LikedPostsActivity", "loadLikeStatus:onCancelled", databaseError.toException())
            }
        })
    }

    private fun sortPostList() {
        likedPosts.sortByDescending { it.like }
        likedPostsAdapter.notifyDataSetChanged()
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
