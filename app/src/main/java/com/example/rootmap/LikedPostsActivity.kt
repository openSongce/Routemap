package com.example.rootmap

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rootmap.databinding.ActivityLikedPostsBinding
import com.example.rootmap.databinding.CommentLayoutBinding
import com.example.rootmap.databinding.DialogLayoutBinding
import com.example.rootmap.databinding.RecyclerviewDialogBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class LikedPostsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLikedPostsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var likedPostsAdapter: RouteListAdapter
    private val likedPosts: MutableList<RoutePost> = mutableListOf()
    private val likedPostsCopy: MutableList<RoutePost> = mutableListOf()

    private lateinit var commentList: MutableList<String>
    private lateinit var commentListAdapter: CommentListAdapter
    private lateinit var commentBinding: CommentLayoutBinding
    private lateinit var routeListDataAdapter: ListLocationAdapter
    private lateinit var user: String

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
                val clickItem = likedPostsAdapter.postList[position]
                viewRoute(clickItem.routeName, clickItem.docId, clickItem.ownerId)
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
            showLoading(true)
            loadLikedPostIdsAndFetchPosts()
            showLoading(false)
        }

        Firebase.firestore.collection("user").document(auth.currentUser?.uid ?: return).get().addOnSuccessListener {
            user = it.getString("nickname").toString()
        }

        commentList = mutableListOf()
        commentListAdapter = CommentListAdapter()
        routeListDataAdapter = ListLocationAdapter().apply {
            postView = true
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
                    showLoading(false)  // 로드가 완료되면 ProgressBar를 숨김
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("LikedPostsActivity", "Error fetching liked post IDs", databaseError.toException())
                showLoading(false) // 오류가 발생해도 ProgressBar를 숨김
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
                        timestamp = document.getLong("timestamp") ?: 0L // timestamp 설정
                    )
                    loadLikeStatus(data)
                    likedPosts.add(data)
                    likedPostsCopy.add(data)
                }
            }
            sortPostList() // 현재 정렬 순서를 기준으로 목록 정렬
            likedPostsAdapter.notifyDataSetChanged()
            return true
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
            return false
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

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.postListView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun viewRoute(routeName: String, docId: String, ownerId: String): AlertDialog { // 다이어로그로 팝업창 구현
        val dialogBinding = RecyclerviewDialogBinding.inflate(layoutInflater)
        val dialogBuild = AlertDialog.Builder(this).setView(dialogBinding.root)
        var routeData = mutableListOf<MyLocation>()
        dialogBuild.setTitle(routeName)
        lifecycleScope.launch {
            routeData = loadPostData(docId, ownerId)
            routeListDataAdapter.list = routeData
            dialogBinding.listView.adapter = routeListDataAdapter
            updateHeartButton(dialogBinding.heartClickButton2, dialogBinding.likeNum, docId)
        }
        lifecycleScope.launch(Dispatchers.IO) {
            commentdataLoading(docId)
        }
        dialogBinding.run {
            listView.layoutManager = LinearLayoutManager(this@LikedPostsActivity)
            addTripRouteText.visibility = View.INVISIBLE
            listView.addItemDecoration(DividerItemDecoration(this@LikedPostsActivity, DividerItemDecoration.VERTICAL))
            commentButton2.visibility = View.VISIBLE
            downloadButton.visibility = View.VISIBLE
            heartClickButton2.visibility = View.VISIBLE
            likeNum.visibility = View.VISIBLE
            commentButton2.setOnClickListener {
                commentDialog(docId, ownerId)
            }
            downloadButton.setOnClickListener {
                // Toast.makeText(this@MenuFragment2.context,"다운",Toast.LENGTH_SHORT).show()
                showDownloadDialog(routeName, routeData)
            }
        }
        val dialog = dialogBuild.show()
        return dialog
    }

    private suspend fun loadPostData(docId: String, ownerId: String): MutableList<MyLocation> {
        val dataList = mutableListOf<Map<String, *>>()
        val postDatas = mutableListOf<MyLocation>()
        return try {
            var data: MutableMap<*, *>
            Firebase.firestore.collection("user").document(ownerId).collection("route").document(docId).get().addOnSuccessListener { documents ->
                data = documents.data as MutableMap<*, *>
                dataList.addAll(data["routeList"] as List<Map<String, *>>)
                dataList.forEach {
                    postDatas.add(MyLocation(it["name"].toString(), it["position"] as GeoPoint, it["memo"] as String, it["spending"] as String))
                }
            }.await()
            postDatas
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
            postDatas
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun commentDialog(docId: String, ownerName: String): AlertDialog {
        commentBinding = CommentLayoutBinding.inflate(layoutInflater)
        val dialogBuild = AlertDialog.Builder(this).setView(commentBinding.root)
        // dialogBuild.setTitle("댓글")
        commentListAdapter.list = commentList
        commentBinding.commentRecyclerView.adapter = commentListAdapter
        if (commentList.isNullOrEmpty()) {
            commentBinding.noComment.visibility = View.VISIBLE
        }
        commentBinding.run {
            commentRecyclerView.layoutManager = LinearLayoutManager(this@LikedPostsActivity)
            commentRecyclerView.addItemDecoration(DividerItemDecoration(this@LikedPostsActivity, DividerItemDecoration.VERTICAL))
            commentSendButton.setOnClickListener {
                sendComment(docId)
            }
            commentWriteText.setOnEditorActionListener { _, _, _ ->
                sendComment(docId)
                true
            }
        }
        val dialog = dialogBuild.show()
        return dialog
    }

    private suspend fun commentdataLoading(docId: String) {
        commentList.clear()
        try {
            val commentListDB = Firebase.firestore.collection("route").document(docId).get().await().get("comment") as List<String>
            if (!commentListDB.isNullOrEmpty()) {
                commentListDB.forEach {
                    commentList.add(it)
                }
            }
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendComment(docId: String) {
        val text = commentBinding.commentWriteText.text.toString()
        commentList.add("$user@comment@:$text@date@:${LocalDate.now()}")
        commentListAdapter.notifyItemInserted(commentList.size)
        // val commentData=currentId+text+LocalDate.now().toString()
        Firebase.firestore.collection("route").document(docId).update("comment", commentList)
        commentBinding.noComment.visibility = View.GONE
    }

    private fun showDownloadDialog(tripname: String, list: List<MyLocation>) {
        val dBinding = DialogLayoutBinding.inflate(layoutInflater)
        dBinding.wButton.text = "아니요" // 다이어로그의 텍스트 변경
        dBinding.bButton.text = "네"
        dBinding.content.text = "해당 경로를 다운로드하겠습니까?"

        val dialogBuild = AlertDialog.Builder(this).setView(dBinding.root)
        val dialog = dialogBuild.show() // 다이어로그 창 띄우기
        dBinding.bButton.setOnClickListener {// 다이어로그 기능 설정
            downloadRoute(tripname, list)
            dialog.dismiss()
        }
        dBinding.wButton.setOnClickListener {// 취소버튼
            // 회색 버튼의 기능 구현 ↓
            dialog.dismiss()
        }
    }

    private fun downloadRoute(tripname: String, list: List<MyLocation>) {
        Firebase.firestore.collection("user").document(auth.currentUser?.uid ?: return).collection("route").document().set(hashMapOf("tripname" to tripname, "routeList" to list, "created" to auth.currentUser?.uid, "shared" to listOf<String>())).addOnSuccessListener {
            showToast("성공적으로 저장하였습니다.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateHeartButton(heartButton: View, likeNumView: TextView, docId: String) {
        val userId = auth.currentUser?.uid ?: return
        val postRef = database.child("postLikes").child(docId)
        val userLikeRef = database.child("userPostLikes").child(userId).child(docId)

        postRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val likeCount = dataSnapshot.getValue(Int::class.java) ?: 0
                likeNumView.text = likeCount.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("LikedPostsActivity", "loadLikeStatus:onCancelled", databaseError.toException())
            }
        })

        userLikeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val isLiked = dataSnapshot.getValue(Boolean::class.java) ?: false
                val heartDrawable = if (isLiked) R.drawable.heart_filled else R.drawable.heart_empty
                heartButton.setBackgroundResource(heartDrawable)

                heartButton.setOnClickListener {
                    handleHeartClickForDialog(docId, heartButton, likeNumView)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("LikedPostsActivity", "loadLikeStatus:onCancelled", databaseError.toException())
            }
        })
    }

    private fun handleHeartClickForDialog(docId: String, heartButton: View, likeNumView: TextView) {
        val userId = auth.currentUser?.uid ?: return
        val postRef = database.child("postLikes").child(docId)
        val userLikeRef = database.child("userPostLikes").child(userId).child(docId)

        userLikeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val isLiked = dataSnapshot.getValue(Boolean::class.java) ?: false
                val newIsLiked = !isLiked

                userLikeRef.setValue(newIsLiked)
                postRef.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val currentLikes = currentData.getValue(Int::class.java) ?: 0
                        currentData.value = if (newIsLiked) currentLikes + 1 else currentLikes - 1
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        databaseError: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        val newLikeCount = currentData?.getValue(Int::class.java) ?: 0
                        likeNumView.text = newLikeCount.toString()
                        val heartDrawable = if (newIsLiked) R.drawable.heart_filled else R.drawable.heart_empty
                        heartButton.setBackgroundResource(heartDrawable)
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("LikedPostsActivity", "handleHeartClick:onCancelled", databaseError.toException())
            }
        })
    }
}