package com.example.rootmap

import android.R
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rootmap.databinding.ActivityRouteMapViewBinding
import com.example.rootmap.databinding.CommentLayoutBinding
import com.example.rootmap.databinding.DialogLayoutBinding
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextStyle
import com.kakao.vectormap.route.RouteLine
import com.kakao.vectormap.route.RouteLineLayer
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLinePattern
import com.kakao.vectormap.route.RouteLineSegment
import com.kakao.vectormap.route.RouteLineStyle
import com.kakao.vectormap.shape.MapPoints
import com.kakao.vectormap.shape.PolylineOptions
import com.kakao.vectormap.shape.PolylineStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class RouteMapViewActivity : AppCompatActivity() {
    val db = Firebase.firestore.collection("user")
    var kakaomap: KakaoMap? = null
    lateinit var myDb: CollectionReference
    private val duration = 500
    var cnt: Int = 0
    var byLevelLine: RouteLine? = null
    var routeLineLayer: RouteLineLayer? = null
    lateinit var auth: FirebaseAuth
    lateinit var database: DatabaseReference
    lateinit var user: String

    //ownerID: intent.getStringExtra("ownerId").toString()
    //currentUserID: intent.getStringExtra("currId").toString()
    //docID: intent.getStringExtra("routeId").toString()
    //routeTitle: intent.getStringExtra("routeTitle").toString()

    private var locationData: MutableList<MyLocation> = mutableListOf()
    private lateinit var routeListAdapter: RouteViewOnMapAdapter
    private var commentList: MutableList<String> = mutableListOf()
    private var commentListAdapter: CommentListAdapter = CommentListAdapter()
    lateinit var commentBinding: CommentLayoutBinding

    private val readyCallback = object : KakaoMapReadyCallback() {
        override fun onMapReady(kakaoMap: KakaoMap) {
            kakaomap = kakaoMap
            val shapeManager = kakaoMap.shapeManager
            var list: MutableList<LatLng> = mutableListOf()
            var dataList = mutableListOf<Map<String, *>>()
            var data: MutableMap<*, *>
            var glist = mutableListOf<GeoPoint>()
            routeLineLayer = kakaoMap.routeLineManager?.getLayer()

            myDb = db.document(intent.getStringExtra("ownerId").toString())
                .collection("route")
            myDb.document(intent.getStringExtra("routeId").toString())
                .get()
                .addOnSuccessListener { result ->
                    list.clear()
                    data = result.data as MutableMap<*, *>
                    dataList.addAll(data["routeList"] as List<Map<String, *>>)
                    dataList.forEach {
                        glist.add(it["position"] as GeoPoint)
                    }
                    for (doc in glist) {
                        list.add(LatLng.from(doc.latitude, doc.longitude))
                        val style: LabelStyle =
                            LabelStyle.from(com.example.rootmap.R.drawable.clicklocation)
                                .setTextStyles(
                                    LabelTextStyle.from(50, Color.parseColor("#0A3711"))
                                )

                        val options = LabelOptions.from(LatLng.from(doc.latitude, doc.longitude))
                            .setStyles(style)
                            .setTexts((cnt + 1).toString())


                        val layer = kakaoMap.labelManager!!.layer

                        val label: Label = layer!!.addLabel(options)
                        cnt++
                    }
                    val styles =
                        RouteLineStyle.from(20f, Color.rgb(255,193,204), 1f, Color.WHITE).setZoomLevel(15)
                            .setPattern(
                                RouteLinePattern.from(
                                    baseContext,
                                    com.example.rootmap.R.style.GreenRouteArrowLineStyle
                                )
                            )
                    val options: RouteLineOptions =
                        RouteLineOptions.from(RouteLineSegment.from(list, styles))
                    byLevelLine = routeLineLayer?.addRouteLine(options)
                    if (cnt != 0) {
                        kakaoMap.moveCamera(
                            CameraUpdateFactory.newCenterPosition(
                                list[0], 15
                            ),
                            CameraAnimation.from(duration)
                        )
                    } else {
                        kakaoMap.moveCamera(
                            CameraUpdateFactory.newCenterPosition(
                                LatLng.from(37.5642135, 127.0016985), 15
                            ),
                            CameraAnimation.from(duration)
                        )
                        Toast.makeText(this@RouteMapViewActivity, "경로가 없습니다.", Toast.LENGTH_SHORT)
                            .show()
                    }

                }
            kakaomap!!.setPadding(0,0,0,500)
            binding.routeListDownButton.setOnClickListener {
                if(binding.locationListView.visibility == View.VISIBLE){
                    binding.routeListDownButton.setImageResource(com.example.rootmap.R.drawable.search_list_up)
                    kakaomap!!.setPadding(0,0,0,0)
                    binding.locationListView.visibility = View.GONE
                }
                else{
                    binding.routeListDownButton.setImageResource(com.example.rootmap.R.drawable.search_list_down)
                    kakaomap!!.setPadding(0,0,0,500)
                    binding.locationListView.visibility = View.VISIBLE
                }
            }


        }
    }

    private val lifecycleCallback = object : MapLifeCycleCallback() {
        override fun onMapDestroy() {
            //Toast.makeText(this@RouteMapViewActivity, "뒤로가기", Toast.LENGTH_SHORT).show()
        }

        override fun onMapError(p0: Exception?) {
            Toast.makeText(this@RouteMapViewActivity, "map error", Toast.LENGTH_SHORT).show()
        }

    }


    private lateinit var binding: ActivityRouteMapViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouteMapViewBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        Firebase.firestore.collection("user").document(intent.getStringExtra("currId").toString()).get().addOnSuccessListener {
            user = it.get("nickname").toString()
        }
        val view = binding.root
        setContentView(view)

        binding.mapViewId.start(lifecycleCallback, readyCallback)
        binding.routeTitle.setText("")
        binding.routeTitle.setText(intent.getStringExtra("routeTitle").toString())

        binding.locationListView.layoutManager = LinearLayoutManager(this)
        binding.locationListView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        lifecycleScope.async {
            routeListAdapter = RouteViewOnMapAdapter()
            locationData.clear()
            loadRoute(intent.getStringExtra("ownerId").toString(),
                intent.getStringExtra("routeId").toString())
            routeListAdapter.docId = intent.getStringExtra("routeId").toString()
            routeListAdapter.list = locationData
            Log.d("routeList", routeListAdapter.list.toString())
            binding.locationListView.adapter = routeListAdapter
            routeListAdapter.notifyDataSetChanged()
        }
        CoroutineScope(Dispatchers.Main).launch{
            updateHeartButton(binding.heartClickButton2, binding.likeNum, intent.getStringExtra("routeId").toString())
        }
        CoroutineScope(Dispatchers.IO).launch {
            commentdataLoading(intent.getStringExtra("routeId").toString())
        }
        binding.commentButton2.setOnClickListener {
            commentDialog(intent.getStringExtra("routeId").toString(), intent.getStringExtra("currId").toString())
        }
        binding.downloadButton.setOnClickListener {
            showDownloadDialog(intent.getStringExtra("routeTitle").toString(), locationData)
        }
        routeListAdapter.setItemClickListener(object: RouteViewOnMapAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                Log.d("RecyclerItemclick", "clicked")
                var clickItem = routeListAdapter.list[position]
                var pos: LatLng = LatLng.from(clickItem.position.latitude, clickItem.position.longitude)
                kakaomap!!.moveCamera(CameraUpdateFactory.newCenterPosition(pos, 15), CameraAnimation.from(duration))
            }
        })

    }


    private suspend fun loadRoute(userID: String, docID: String): Boolean{
        var dataList = mutableListOf<Map<String, *>>()
        return try{
            var data: MutableMap<*, *>
            db.document(userID).collection("route").document(docID).get().addOnSuccessListener { documents->
                data=documents.data as MutableMap<*,*>
                dataList.addAll(data["routeList"] as List<Map<String,*>>)
                dataList.forEach{
                    locationData.add(MyLocation(it["name"].toString(),it["position"] as GeoPoint,it["memo"] as String,it["spending"] as String))
                }
                Log.d("loadRoutetest", dataList.toString())
            }.await()
            true
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
            true
        }
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
                Log.w("MenuFragment2", "loadLikeStatus:onCancelled", databaseError.toException())
            }
        })

        userLikeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val isLiked = dataSnapshot.getValue(Boolean::class.java) ?: false
                val heartDrawable = if (isLiked) com.example.rootmap.R.drawable.heart_filled else com.example.rootmap.R.drawable.heart_empty
                heartButton.setBackgroundResource(heartDrawable)

                heartButton.setOnClickListener {
                    handleHeartClickForDialog(docId, heartButton, likeNumView)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("MenuFragment2", "loadLikeStatus:onCancelled", databaseError.toException())
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
                        currentData: DataSnapshot?,
                    ) {
                        val newLikeCount = currentData?.getValue(Int::class.java) ?: 0
                        likeNumView.text = newLikeCount.toString()
                        val heartDrawable = if (newIsLiked) com.example.rootmap.R.drawable.heart_filled else com.example.rootmap.R.drawable.heart_empty
                        heartButton.setBackgroundResource(heartDrawable)
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("MenuFragment2", "handleHeartClick:onCancelled", databaseError.toException())
            }
        })
    }

    private suspend fun commentdataLoading(docId: String) {
        commentList.clear()
        try {
            val commentListDB = com.google.firebase.ktx.Firebase.firestore.collection("route").document(docId).get().await().get("comment") as List<String>
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
    private fun commentDialog(docId: String, ownerName: String): AlertDialog {
        commentBinding = CommentLayoutBinding.inflate(layoutInflater)
        val dialogBuild = AlertDialog.Builder(this).setView(commentBinding.root)
        //dialogBuild.setTitle("댓글")
        commentListAdapter.list = commentList
        commentBinding.commentRecyclerView.adapter = commentListAdapter
        if (commentList.isNullOrEmpty()) {
            commentBinding.noComment.visibility = View.VISIBLE
        }
        commentBinding.run {
            commentRecyclerView.layoutManager = LinearLayoutManager(baseContext)
            commentRecyclerView.addItemDecoration(DividerItemDecoration(baseContext, DividerItemDecoration.VERTICAL))
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendComment(docId: String) {
        val text = commentBinding.commentWriteText.text.toString()
        if (text.isNotBlank()) {
            commentList.add("$user@comment@:$text@date@:${LocalDate.now()}")
            commentListAdapter.notifyItemInserted(commentList.size)
            commentBinding.commentWriteText.text.clear()  // 입력 창 비우기
            com.google.firebase.ktx.Firebase.firestore.collection("route").document(docId).update("comment", commentList)
            commentBinding.noComment.visibility = View.GONE
        } else {
            Toast.makeText(this, "댓글을 입력하세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDownloadDialog(tripname: String, list: List<MyLocation>) {
        val dBinding = DialogLayoutBinding.inflate(layoutInflater)
        dBinding.wButton.text = "아니요" // 다이어로그의 텍스트 변경
        dBinding.bButton.text = "네"
        dBinding.content.text = "해당 경로를 다운로드하겠습니까?"

        val dialogBuild = AlertDialog.Builder(this).setView(dBinding.root)
        val dialog = dialogBuild.show() // 다이어로그 창 띄우기
        dBinding.bButton.setOnClickListener { // 다이어로그 기능 설정
            downloadRoute(tripname, list)
            dialog.dismiss()
        }
        dBinding.wButton.setOnClickListener { // 취소버튼
            // 회색 버튼의 기능 구현 ↓
            dialog.dismiss()
        }
    }

    private fun downloadRoute(tripname: String, list: List<MyLocation>) {
        com.google.firebase.ktx.Firebase.firestore.collection("user").
            document(intent.getStringExtra("currId").toString()).collection("route").
            document().set(hashMapOf("tripname" to tripname, "routeList" to list, "created" to intent.getStringExtra("currId").toString(),
                "shared" to listOf<String>())).addOnSuccessListener {
            Toast.makeText(this, "성공적으로 저장하였습니다.", Toast.LENGTH_SHORT).show()
        }
    }

}





