package com.example.rootmap

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rootmap.databinding.CommentLayoutBinding
import com.example.rootmap.databinding.DialogLayoutBinding
import com.example.rootmap.databinding.FragmentMenu2Binding
import com.example.rootmap.databinding.PopupFilterBinding
import com.example.rootmap.databinding.RecyclerviewDialogBinding
import com.example.rootmap.databinding.RouteaddLayoutBinding
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
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

private const val ARG_PARAM1 = "param1_board"
private const val ARG_PARAM2 = "param2_board"
private const val PREFS_NAME = "FilterPrefs"

class MenuFragment2 : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentMenu2Binding

    private val selectedLocations = mutableListOf<String>()
    private val selectedDurations = mutableListOf<String>()
    private val selectedThemes = mutableListOf<String>()

    lateinit var routeDialog: AlertDialog
    lateinit var routelistAdapter: MyDocumentAdapter
    lateinit var routeList: MutableList<MyRouteDocument>
    lateinit var currentId:String
    lateinit var docId:String
    lateinit var docName:String
    lateinit var docOwner:String

    lateinit var postlistAdapter: RouteListAdapter
    lateinit var postLists: MutableList<RoutePost>
    lateinit var postListCopy: MutableList<RoutePost>

    lateinit var routeListDataAdapter:ListLocationAdapter

    lateinit var commentList:MutableList<String>
    lateinit var commentListAdapter: CommentListAdapter
    lateinit var commentBinding: CommentLayoutBinding

    lateinit var selectedOptions:List<String>
    lateinit var user:String

    lateinit var database: DatabaseReference
    lateinit var auth: FirebaseAuth

    private lateinit var sortButton: Button
    private var isSortedByLikes = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentId = it.getString("id").toString()
            param2 = it.getString(ARG_PARAM2)
        }
        routeList = mutableListOf<MyRouteDocument>()
        routelistAdapter= MyDocumentAdapter()
        routelistAdapter.mode="makePost"
        routelistAdapter.userId=currentId

        postLists= mutableListOf()
        postListCopy= mutableListOf()
        postlistAdapter=RouteListAdapter()
        postlistAdapter.postMode=true

        selectedOptions= listOf()

        routeListDataAdapter=ListLocationAdapter()
        routeListDataAdapter.postView=true

        commentList= mutableListOf()
        commentListAdapter= CommentListAdapter()

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMenu2Binding.inflate(inflater, container, false)

        // 정렬 버튼 초기화
        sortButton = binding.sortButton
        sortButton.setOnClickListener {
            toggleSortOrder()
        }

        // 필터 버튼 클릭 이벤트
        binding.filterButton.setOnClickListener {
            showFilterPopup("filter")
        }
        binding.postMyRouteButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                loadMyList()
                routeDialog=makeMyPost()
            }
        }
        binding.postListView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        CoroutineScope(Dispatchers.Main).launch {
            postlistAdapter.postList=postLists
            binding.postListView.adapter=postlistAdapter
            binding.postListView.layoutManager=LinearLayoutManager(this@MenuFragment2.context)
        }

        routelistAdapter.setItemClickListener(object: MyDocumentAdapter.OnItemClickListener{
            //리스트의 버튼 클릭시 동작
            override fun onClick(v: View, position: Int) {
                //버튼 눌렀을때의 코드
                docId = routelistAdapter.list[position].docId
                docName = routelistAdapter.list[position].docName
                docOwner = routelistAdapter.list[position].owner
                showFilterPopup("post")
            }
            override fun onListClick(v: View, position: Int) {
            }
            override fun deleteDoc(v: View, position: Int) {
            }
            override fun shareDoc(v: View, position: Int) {
            }
        })
        postlistAdapter.setItemClickListener(object: RouteListAdapter.OnItemClickListener{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onClick(v: View, position: Int) {
                //해당 경로 내의 여행지 리스트 보기
                var clickItem=postlistAdapter.postList[position]
                viewRoute(clickItem.routeName,clickItem.docId,clickItem.ownerId)
            }
            override fun onButtonClick(v: View, position: Int) {
            }
            override fun heartClick(v: View, position: Int) {
                val post = postlistAdapter.postList[position]
                handleHeartClick(post)
            }
        })
        if (container != null) {
            routeListDataAdapter.parent=container
        }
        Firebase.firestore.collection("user").document(currentId).get().addOnSuccessListener {
            user=it.get("nickname").toString()
        }
        binding.run {
            searchPostText.setOnEditorActionListener{ _,_,_ ->
                searchPost()
                true
            }
            postSearchButton.setOnClickListener {
                searchPost()
            }
        }
        return binding.root
    }

    private fun toggleSortOrder() {
        isSortedByLikes = !isSortedByLikes
        sortButton.text = if (isSortedByLikes) "최신 순 정렬" else "좋아요 순 정렬"
        sortPostList()
    }

    private fun sortPostList() {
        if (isSortedByLikes) {
            postLists.sortByDescending { it.like }
        } else {
            postLists.sortByDescending { it.timestamp } // Sort by timestamp for the latest posts
        }
        postlistAdapter.notifyDataSetChanged()
    }

    override fun onStart() {
        CoroutineScope(Dispatchers.Main).launch {
            loadPostList()
            if(!selectedOptions.isEmpty()){
                filter()
            }
            postlistAdapter.notifyDataSetChanged()
            binding.postProgressBar.visibility=View.GONE
        }
        super.onStart()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun viewRoute(routeName:String, docId:String, ownerId:String):AlertDialog { //다이어로그로 팝업창 구현
        val dialogBinding = RecyclerviewDialogBinding.inflate(layoutInflater)
        val dialogBuild =AlertDialog.Builder(context).setView(dialogBinding.root)
        var routeData= mutableListOf<MyLocation>()
        dialogBuild.setTitle(routeName)
        CoroutineScope(Dispatchers.Main).launch {
            routeData=loadPostData(docId,ownerId)
            routeListDataAdapter.list=routeData
            dialogBinding.listView.adapter = routeListDataAdapter
        }
        CoroutineScope(Dispatchers.IO).launch {
            commentdataLoading(docId)
        }
        dialogBinding.run{
            listView.layoutManager = LinearLayoutManager(context)
            addTripRouteText.visibility=View.INVISIBLE
            listView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            commentButton2.visibility=View.VISIBLE
            downloadButton.visibility=View.VISIBLE
            heartClickButton2.visibility=View.VISIBLE
            likeNum.visibility=View.VISIBLE
            commentButton2.setOnClickListener {
                commentDialog(docId,currentId)
            }
            downloadButton.setOnClickListener {
                //  Toast.makeText(this@MenuFragment2.context,"다운",Toast.LENGTH_SHORT).show()
                showDownloadDialog(routeName,routeData)
            }
        }
        val dialog = dialogBuild.show()
        return dialog
    }
    private suspend fun loadPostData(docId:String,ownerId:String):MutableList<MyLocation>{
        var dataList= mutableListOf<Map<String,*>>()
        var postDatas= mutableListOf<MyLocation>()
        return try {
            var data: MutableMap<*, *>
            Firebase.firestore.collection("user").document(ownerId).collection("route").document(docId).get().addOnSuccessListener { documents->
                data=documents.data as MutableMap<*,*>
                dataList.addAll(data["routeList"] as List<Map<String,*>>)
                dataList.forEach{
                    postDatas.add(MyLocation(it["name"].toString(),it["position"] as GeoPoint,it["memo"] as String,it["spending"] as String))
                }
            }.await()
            postDatas
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
            postDatas
        }
    }

    private fun showFilterPopup(mode:String) {
        val popupBinding = PopupFilterBinding.inflate(LayoutInflater.from(context))
        val locations = mutableListOf<String>()
        val duration=mutableListOf<String>()
        val themes = mutableListOf<String>()
        if(mode=="filter"){
            // 여행지, 여행일, 테마 체크박스 동적 생성
            addCheckBoxes(R.array.locations_array, popupBinding.locationsContainer, "locations")
            addCheckBoxes(R.array.durations_array, popupBinding.durationsContainer, "durations")
            addCheckBoxes(R.array.themes_array, popupBinding.themesContainer, "themes")
        }else{
            //게시글 만들기 ver
            addCheckBoxesPostVer(R.array.locations_array, popupBinding.locationsContainer, "locations")
            addCheckBoxesPostVer(R.array.durations_array, popupBinding.durationsContainer, "durations")
            addCheckBoxesPostVer(R.array.themes_array, popupBinding.themesContainer, "themes")
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(popupBinding.root)
            .setPositiveButton("확인") { _, _ ->
                if(mode=="filter"){
                    //필터버튼 사용시
                    applyFilters(popupBinding)
                }
                else{//게시글 만들때
                    checkForPost(popupBinding.locationsContainer,locations)
                    checkForPost(popupBinding.durationsContainer,duration)
                    checkForPost(popupBinding.themesContainer,themes)
                    postMyRouteDb(locations,duration,themes)
                    routeDialog.dismiss()
                }
            }
            .setNegativeButton("취소", null)
            .setNeutralButton("초기화") { _, _ ->
                if(mode=="filter"){
                    resetFilters(popupBinding)
                    applyFilters(popupBinding)
                }
            }
            .create()
        dialog.show()
    }

    private fun applyFilters(popupBinding: PopupFilterBinding) {
        selectedLocations.clear()
        selectedDurations.clear()
        selectedThemes.clear()
        // 여행지 선택 확인
        checkAndAddAll(popupBinding.locationsContainer, selectedLocations, "locations")
        checkAndAddAll(popupBinding.durationsContainer, selectedDurations, "durations")
        checkAndAddAll(popupBinding.themesContainer, selectedThemes, "themes")

        updateSelectedOptions()
    }

    private fun resetFilters(popupBinding: PopupFilterBinding) {
        // 모든 체크박스 초기화
        clearAllCheckBoxes(popupBinding.locationsContainer, "locations")
        clearAllCheckBoxes(popupBinding.durationsContainer, "durations")
        clearAllCheckBoxes(popupBinding.themesContainer, "themes")
    }

    private fun postMyRouteDb(list: MutableList<String>, list2: MutableList<String>, list3: MutableList<String>) {
        val emptyList = listOf<String>()
        val uploadList = list + list2 + list3
        val currentTimestamp = System.currentTimeMillis() // Get the current timestamp

        Firebase.firestore.collection("route").document(docId).set(hashMapOf(
            "owner" to docOwner,
            "tripname" to docName,
            "comment" to emptyList,
            "option" to uploadList,
            "timestamp" to currentTimestamp // Save the timestamp
        )).addOnSuccessListener {
            Toast.makeText(this.context, "성공적으로 업로드하였습니다.", Toast.LENGTH_SHORT).show()
            CoroutineScope(Dispatchers.Main).launch {
                loadPostList()
                postlistAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun checkForPost(container: ViewGroup, list: MutableList<String>){
        for (i in 0 until container.childCount) {
            val checkBox = container.getChildAt(i) as CheckBox
            checkAndAdd(checkBox, list)
        }
    }

    private fun clearAllCheckBoxes(container: ViewGroup, keyPrefix: String) {
        for (i in 0 until container.childCount) {
            val checkBox = container.getChildAt(i) as CheckBox
            checkBox.isChecked = false
            saveCheckboxState("$keyPrefix$i", false)
        }
    }

    private fun checkAndAddAll(container: ViewGroup, list: MutableList<String>, keyPrefix: String) {
        for (i in 0 until container.childCount) {
            val checkBox = container.getChildAt(i) as CheckBox
            checkAndAdd(checkBox, list)
            saveCheckboxState("$keyPrefix$i", checkBox.isChecked)
        }
    }

    private fun checkAndAdd(checkBox: CheckBox, list: MutableList<String>) {
        if (checkBox.isChecked) {
            list.add(checkBox.text.toString())
        }
    }

    private fun updateSelectedOptions() {
        val selectedOptions = "여행지: ${selectedLocations.joinToString(", ")}\n여행일: ${selectedDurations.joinToString(", ")}\n테마: ${selectedThemes.joinToString(", ")}"
        binding.selectedOptionsTextView.text = selectedOptions
        filter()
    }

    private fun filter(){
        var filterResult= listOf<RoutePost>()
        selectedOptions=selectedLocations+selectedDurations+selectedThemes

        if(selectedOptions.isEmpty()){
            //초기화
            postLists.clear()
            postLists.addAll(postListCopy)
            postlistAdapter.notifyDataSetChanged()
        }else{
            //필터 적용
            postLists.clear()
            filterResult=postListCopy.filter {
                it.option.containsAll(selectedOptions)
            }
            postLists.addAll(filterResult)
            postlistAdapter.notifyDataSetChanged()
        }
        Log.d("filter_test",postListCopy.size.toString())
    }

    private fun addCheckBoxes(arrayResId: Int, container: ViewGroup, keyPrefix: String) {
        val items = resources.getStringArray(arrayResId)
        val sharedPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        for (i in items.indices) {
            val checkBox = CheckBox(context).apply {
                text = items[i]
                isChecked = sharedPrefs.getBoolean("$keyPrefix$i", false)
            }
            container.addView(checkBox)
        }
    }

    private fun addCheckBoxesPostVer(arrayResId: Int, container: ViewGroup, keyPrefix: String) {
        val items = resources.getStringArray(arrayResId)
        val sharedPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        for (i in items.indices) {
            val checkBox = CheckBox(context).apply {
                text = items[i]
            }
            container.addView(checkBox)
        }
    }


    private fun saveCheckboxState(key: String, state: Boolean) {
        val sharedPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putBoolean(key, state)
            apply()
        }
    }
    private fun makeMyPost():AlertDialog { //다이어로그로 팝업창 구현
        val dialogBinding = RecyclerviewDialogBinding.inflate(layoutInflater)
        val dialogBuild = android.app.AlertDialog.Builder(context).setView(dialogBinding.root)
        dialogBuild.setTitle("내가 만든 여행 리스트")
        routelistAdapter.list=routeList
        dialogBinding.listView.adapter = routelistAdapter
        dialogBinding.listView.layoutManager = LinearLayoutManager(context)
        if(routelistAdapter.list.isNullOrEmpty()){
            dialogBinding.checkText.apply {
                text="아직 경로가 없습니다. 새로운 경로를 만들어주세요."
                visibility=View.VISIBLE
            }
        }
        dialogBinding.addTripRouteText.visibility=View.GONE
        routeDialog = dialogBuild.show()
        return routeDialog
    }
    private suspend fun loadMyList(): Boolean {
        routeList.clear()
        return try {
            val myList = MenuFragment3.getInstance()!!.returnDb(currentId).get().await()
            if(!myList.isEmpty){
                myList.forEach {
                    routeList.add(MyRouteDocument(it.data?.get("tripname").toString(),it.id,currentId))
                }
            }
            true
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
            false
        }
    }

    private suspend fun loadPostList(): Boolean {
        postLists.clear()
        postListCopy.clear()
        return try {
            val postListData = Firebase.firestore.collection("route").get().await()
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
                    postLists.add(data)
                    postListCopy.add(data)
                }
            }
            // Sort the list based on the current sorting order
            sortPostList()
            true
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
            false
        }
    }


    private fun loadLikeStatus(post: RoutePost) {
        val userId = auth.currentUser?.uid ?: return
        val postRef = database.child("postLikes").child(post.docId)
        postRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                post.like = dataSnapshot.getValue(Int::class.java) ?: 0
             //   postlistAdapter.notifyItemChanged(postLists.indexOf(post))
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("MenuFragment2", "loadLikeStatus:onCancelled", databaseError.toException())
            }
        })

        val userLikeRef = database.child("userPostLikes").child(userId).child(post.docId)
        userLikeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                post.isLiked = dataSnapshot.getValue(Boolean::class.java) ?: false
              //  postlistAdapter.notifyItemChanged(postLists.indexOf(post))
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("MenuFragment2", "loadLikeStatus:onCancelled", databaseError.toException())
            }
        })
    }

    private suspend fun loadUserName(id:String):String{
        return try {
            val nickname=Firebase.firestore.collection("user").document(id).get().await().get("nickname").toString()
            return nickname
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
            return "error"
        }
    }
    private fun loadLike(){
        //일단 보류
        postLists.forEach { item ->
            item.docId?.let { id ->
                database.child("postLike").child(id)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val likeCount = dataSnapshot.getValue(Int::class.java) ?: 0
                            item.like = likeCount
                            postlistAdapter.notifyDataSetChanged()
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.w("MenuFragment", "loadLikeCount:onCancelled", databaseError.toException())
                        }
                    })
            }
        }
    }
    private fun showDownloadDialog(tripname:String,list:List<MyLocation>){
        val dBinding = DialogLayoutBinding.inflate(layoutInflater)
        dBinding.wButton.text = "아니요" //다이어로그의 텍스트 변경
        dBinding.bButton.text = "네"
        dBinding.content.text = "해당 경로를 다운로드하겠습니까?"

        val dialogBuild = AlertDialog.Builder(context).setView(dBinding.root)
        val dialog = dialogBuild.show() //다이어로그 창 띄우기
        dBinding.bButton.setOnClickListener {//다이어로그 기능 설정
            downloadRoute(tripname,list)
            dialog.dismiss()
        }
        dBinding.wButton.setOnClickListener {//취소버튼
            //회색 버튼의 기능 구현 ↓
            dialog.dismiss()
        }
    }

    private fun downloadRoute(tripname:String,list:List<MyLocation>){
        Firebase.firestore.collection("user").document(currentId).collection("route").document().set(hashMapOf("tripname" to tripname,"routeList" to list,"created" to currentId,"shared" to listOf<String>())).addOnSuccessListener {
            Toast.makeText(context,"성공적으로 저장하였습니다.",Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun commentDialog(docId:String, ownerName:String):AlertDialog{
        commentBinding=CommentLayoutBinding.inflate(layoutInflater)
        val dialogBuild = AlertDialog.Builder(context).setView(commentBinding.root)
        //dialogBuild.setTitle("댓글")
        commentListAdapter.list=commentList
        commentBinding.commentRecyclerView.adapter = commentListAdapter
        if(commentList.isNullOrEmpty()){
            commentBinding.noComment.visibility=View.VISIBLE
        }
        commentBinding.run {
            commentRecyclerView.layoutManager = LinearLayoutManager(context)
            commentRecyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            commentSendButton.setOnClickListener {
                sendComment(docId)
            }
            commentWriteText.setOnEditorActionListener{ _,_,_ ->
                sendComment(docId)
                true
            }
        }
        val dialog = dialogBuild.show()
        return dialog
    }

    private suspend fun commentdataLoading(docId:String){
        commentList.clear()
        try {
            val commentListDB=Firebase.firestore.collection("route").document(docId).get().await().get("comment") as List<String>
            if(!commentListDB.isNullOrEmpty()){
                commentListDB.forEach {
                    commentList.add(it)
                }
            }
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendComment(docId: String){
        var text=commentBinding.commentWriteText.text.toString()
        commentList.add(user+"@comment@:"+text+"@date@:"+LocalDate.now().toString())
        commentListAdapter.notifyItemInserted(commentList.size)
        //  val commentData=currentId+text+LocalDate.now().toString()
        Firebase.firestore.collection("route").document(docId).update("comment",commentList)
        commentBinding.noComment.visibility=View.GONE
    }

    private fun searchPost(){
        postLists.clear()
        var searchText=binding.searchPostText.text
        postListCopy.forEach {
            if(it.routeName.contains(searchText)||it.ownerName.contains(searchText)) postLists.add(it)
        }
        postlistAdapter.notifyDataSetChanged()
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
        postlistAdapter.notifyItemChanged(postLists.indexOf(post))

        userLikeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val liked = dataSnapshot.getValue(Boolean::class.java) ?: false
                if (liked) {
                    userLikeRef.setValue(false)
                    postRef.runTransaction(object : Transaction.Handler {
                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                            val currentLikes = currentData.getValue(Int::class.java) ?: 0
                            currentData.value = currentLikes - 1
                            return Transaction.success(currentData)
                        }

                        override fun onComplete(
                            databaseError: DatabaseError?,
                            committed: Boolean,
                            currentData: DataSnapshot?
                        ) {
                        }
                    })
                } else {
                    userLikeRef.setValue(true)
                    postRef.runTransaction(object : Transaction.Handler {
                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                            val currentLikes = currentData.getValue(Int::class.java) ?: 0
                            currentData.value = currentLikes + 1
                            return Transaction.success(currentData)
                        }

                        override fun onComplete(
                            databaseError: DatabaseError?,
                            committed: Boolean,
                            currentData: DataSnapshot?
                        ) {
                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("MenuFragment2", "handleHeartClick:onCancelled", databaseError.toException())
            }
        })
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MenuFragment2().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}