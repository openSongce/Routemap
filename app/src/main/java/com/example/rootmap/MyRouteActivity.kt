package com.example.rootmap

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Friend
import com.example.rootmap.databinding.ActivityMyRouteBinding
import com.example.rootmap.databinding.DialogLayoutBinding
import com.example.rootmap.databinding.FragmentFriendListBinding
import com.example.rootmap.databinding.RecyclerviewDialogBinding
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MyRouteActivity : AppCompatActivity() {
    val binding by lazy { ActivityMyRouteBinding.inflate(layoutInflater) }
    lateinit var routelistAdapter: MyDocumentAdapter
    lateinit var listLocationAdapter:ListLocationAdapter
    lateinit var routeList: MutableList<MyRouteDocument>
    lateinit var routeDataList:MutableList<MyLocation>
    lateinit var myDb: DocumentReference
    private lateinit var auth: FirebaseAuth
    lateinit var currentId:String
    lateinit var swipeHelperCallback: SwapeManageAdapter
    var searchText=""
    var searchCheck=false
    var routeName=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        currentId = auth.currentUser?.email.toString()
        myDb = Firebase.firestore.collection("user").document(currentId.toString())
        routeList = mutableListOf<MyRouteDocument>()
        routeDataList= mutableListOf<MyLocation>()

        routelistAdapter= MyDocumentAdapter()
        routelistAdapter.mode="MyRoute"
        routelistAdapter.userId=currentId
        listLocationAdapter= ListLocationAdapter()

        swipeHelperCallback = SwapeManageAdapter(routelistAdapter).apply {
            // 스와이프한 뒤 고정시킬 위치 지정
            setClamp(resources.displayMetrics.widthPixels.toFloat()/4)
        }
        ItemTouchHelper(swipeHelperCallback).attachToRecyclerView(binding.recyclerList)
        val swipeHelperCallbackData = DragManageAdapter(listLocationAdapter).apply {
            // 스와이프한 뒤 고정시킬 위치 지정
            setClamp(resources.displayMetrics.widthPixels.toFloat()/4)
        }
        ItemTouchHelper(swipeHelperCallbackData).attachToRecyclerView(binding.routeDataListView)
        // 구분선 추가
        binding.routeDataListView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.routeDataListView.setOnTouchListener { _, _ ->
            swipeHelperCallbackData.removePreviousClamp(binding.routeDataListView)
            false
        }
        CoroutineScope(Dispatchers.Main).launch {
            if(loadMyRouteList()==false){
                Toast.makeText(this@MyRouteActivity, "데이터 로드에 실패했습니다.",Toast.LENGTH_SHORT).show()
            }
            routelistAdapter.list=routeList

            binding.recyclerList.run {
                adapter=routelistAdapter
                layoutManager=LinearLayoutManager(this@MyRouteActivity)
                setOnTouchListener { _, _ ->
                    swipeHelperCallback.removePreviousClamp(this)
                    false
                }
                //구분선
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            }
        }
        binding.backButton.setOnClickListener { //뒤로가기 버튼
            super.onBackPressed()
        }
        binding.mRSearchButton.setOnClickListener { //검색 버튼
            clickOrEnterSearch()
        }
        binding.searchMyRouteText.setOnEditorActionListener { v, actionId, event //키보드 엔터 사용시
            ->
            clickOrEnterSearch()
            true
        }

        routelistAdapter.setItemClickListener(object: MyDocumentAdapter.OnItemClickListener{
            //리스트의 버튼 클릭시 동작
            override fun onClick(v: View, position: Int) {
                //버튼 눌렀을때의 코드
                var docId=routelistAdapter.list[position].docId
                var docName=routelistAdapter.list[position].docName
                var docOwner=routelistAdapter.list[position].owner
                val popup = PopupMenu(this@MyRouteActivity,v )
                popup.inflate(R.menu.myroute_list_layout)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_menu1 -> { //보기 클릭
                            binding.run {
                                recyclerList.visibility=View.INVISIBLE
                                routeDataListView.visibility=View.VISIBLE
                                routeCloseButton.visibility=View.VISIBLE
                                routeSaveButton2.visibility=View.VISIBLE
                                routeNameText2.visibility=View.VISIBLE
                                routeNameText2.setText(docName)
                                routeCloseButton.setOnClickListener {
                                    routeDataListView.visibility=View.GONE
                                    routeCloseButton.visibility=View.GONE
                                    routeSaveButton2.visibility=View.GONE
                                    recyclerList.visibility=View.VISIBLE
                                    routeNameText2.visibility=View.GONE
                                }
                                routeSaveButton2.setOnClickListener {
                                    //저장버튼을 눌렀을 때
                                    var text=binding.routeNameText2.text.toString()
                                    val originalData=MenuFragment3.getInstance()!!.returnDb(docOwner).document(docId)
                                    originalData.update(hashMapOf("tripname" to text,"routeList" to routeDataList)).addOnSuccessListener {
                                        Toast.makeText(this@MyRouteActivity,"성공적으로 저장하였습니다.",Toast.LENGTH_SHORT).show()
                                    }
                                    this@MyRouteActivity.hideKeyboard(binding.root)
                                    //공유 경로인 경우, 경로 이름 변경시 sharedList의 docName 전부 변경
                                    if(text!=docName){
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val frList=originalData.get().await().data?.get("shared") as List<String>
                                            frList.forEach {
                                                Firebase.firestore.collection("user").document(it).collection("sharedList").document(docId).update("docName",text)
                                            }
                                        }
                                        routelistAdapter.list[position].docName=text
                                        routelistAdapter.notifyItemChanged(position)
                                    }
                                }
                            }
                            CoroutineScope(Dispatchers.IO).launch {
                                routeDataList.clear()
                                loadMyRouteData(docId,docOwner)
                                listLocationAdapter.list=routeDataList
                                withContext(Dispatchers.Main){
                                    binding.routeDataListView.run{
                                        adapter=listLocationAdapter
                                        layoutManager=LinearLayoutManager(this@MyRouteActivity)
                                    }
                                }
                            }
                        }
                        R.id.action_menu2->{
                            CoroutineScope(Dispatchers.Main).launch{
                                var data=loadFriendData()
                                showFriendDialog(data,docId,docName)
                            }
                        }
                        else -> { //게시글로 만들기 클릭
                            if(routelistAdapter.list[position].owner==currentId){
                            // Toast.makeText(this@MyRouteActivity, "게시글", Toast.LENGTH_SHORT).show()
                               // MenuFragment2.getInstance()!!.showFilterPopup()
                            }
                            else{
                                swipeHelperCallback.removeClamp(binding.recyclerList)
                                Toast.makeText(this@MyRouteActivity, "경로 생성자만이 사용할 수 있는 기능입니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    true
                }
                popup.show()
            }
            override fun onListClick(v: View, position: Int) {

            }
            override fun deleteDoc(v: View, position: Int) {
                //삭제버튼 코드
                if(routelistAdapter.list[position].owner==currentId){
                    showDeleteDialog(position)
                    Firebase.firestore.collection("route").document(routelistAdapter.list[position].docId).delete()
                }
                else{
                    swipeHelperCallback.removeClamp(binding.recyclerList)
                    Toast.makeText(this@MyRouteActivity, "경로 생성자만이 삭제할 수 있습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun shareDoc(v: View, position: Int) {
                CoroutineScope(Dispatchers.Main).launch {
                    val data = loadFriendData()
                    val docId = routelistAdapter.list[position].docId
                    val docName = routelistAdapter.list[position].docName

                    // 경로 생성자 확인
                    if (routelistAdapter.list[position].owner == currentId) {
                        showFriendDialog(data, docId, docName)
                    } else {
                        swipeHelperCallback.removeClamp(binding.recyclerList)
                        Toast.makeText(this@MyRouteActivity, "경로 생성자만이 공유할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }


        })
    }
    suspend fun loadMyRouteList():Boolean{
        //내 루트리스트를 가져오는 함수
        routeList.clear()
        return try {
            val myList = myDb.collection("route").get().await()
            if(!myList.isEmpty){
                myList.forEach {
                    routeList.add(MyRouteDocument(it.data?.get("tripname").toString(),it.id,currentId))
                }
            }
            //공유리스트 추가
            val sharedList=myDb.collection("sharedList").get().await()
            if(!sharedList.isEmpty){
                sharedList.forEach {
                    routeList.add(MyRouteDocument(it.data?.get("docName").toString(),it.data?.get("docId").toString(),it.data?.get("created").toString()))
                }
            }
            true
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
            false
        }
    }
    private suspend fun loadMyRouteData(id:String,owner:String): Boolean {
        //경로내의 여행지 리스트들을 가져오는 함수
        var dataList= mutableListOf<Map<String,*>>()
        return try {
            var data: MutableMap<*, *>
            Firebase.firestore.collection("user").document(owner).collection("route").document(id).get().addOnSuccessListener { documents->
                routeName=documents.data?.get("tripname").toString()
                data=documents.data as MutableMap<*,*>
                dataList.addAll(data["routeList"] as List<Map<String,*>>)
                dataList.forEach{
                    routeDataList.add(MyLocation(it["name"].toString(),it["position"] as GeoPoint,it["memo"] as String,it["spending"] as String))
                }
            }.await()
            true
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
            true
        }
    }

    private fun clickOrEnterSearch(){
        searchText=binding.searchMyRouteText.text.toString()
        CoroutineScope(Dispatchers.Main).launch{
            if(searchText!=""){
                //검색결과
                routelistAdapter.list=searchRoute(searchText)
                routelistAdapter.notifyDataSetChanged()
                searchCheck=true
            }else if(searchCheck==true){
                //전체 결과
                if(loadMyRouteList()==false){
                    Toast.makeText(this@MyRouteActivity, "데이터 로드에 실패했습니다.",Toast.LENGTH_SHORT).show()
                }else{
                    routelistAdapter.list=routeList
                    routelistAdapter.notifyDataSetChanged()
                    searchCheck=false
                }
            }
        }
       this.hideKeyboard(binding.root)
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
    private fun searchRoute(text:String):MutableList<MyRouteDocument>{
        var searchList= mutableListOf<MyRouteDocument>()
        if(routeList.isNullOrEmpty()){
            Toast.makeText(this@MyRouteActivity, "내가 만든 여행경로가 없어 검색이 불가능합니다.",Toast.LENGTH_SHORT).show()
        }
        routeList.forEach {
            if (it.docName.contains(text)){
                searchList.add(it)
            }
        }
        return searchList
    }

    fun showDeleteDialog(position: Int){
        val dBinding = DialogLayoutBinding.inflate(layoutInflater)
        dBinding.wButton.text = "취소" //다이어로그의 텍스트 변경
        dBinding.bButton.text = "확인"
        dBinding.content.text = "해당 경로를 삭제하시겠습니까?"
        val dialogBuild = AlertDialog.Builder(this).setView(dBinding.root)
        val dialog = dialogBuild.show() //다이어로그 창 띄우기
        dBinding.bButton.setOnClickListener {
            //검정 버튼의 기능 구현 ↓
            var docId=routelistAdapter.list[position].docId
            myDb.collection("route").document(docId).delete()
            routelistAdapter.list.removeAt(position)
            routelistAdapter.notifyItemRemoved(position)
            dialog.dismiss()
        }
        dBinding.wButton.setOnClickListener {//취소버튼
            //회색 버튼의 기능 구현 ↓
            //슬라이드한 항목을 원래 위치로
            swipeHelperCallback.removeClamp(binding.recyclerList)
            dialog.dismiss()
        }
    }

     private fun showFriendDialog(friendList:MutableList<Friend>,docId:String,docName:String):AlertDialog{ //다이어로그로 팝업창 구현
        val dBinding = RecyclerviewDialogBinding.inflate(layoutInflater)
        val dialogBuild = AlertDialog.Builder(this).setView(dBinding.root)
        dialogBuild.setTitle("공유할 친구 목록")
        var myFriendAdapter=FriendAdapter()
        myFriendAdapter.run {
            mode="RouteShare"
            myid=currentId
            list=friendList
        }
        dBinding.run {
            listView.adapter = myFriendAdapter
            listView.layoutManager = LinearLayoutManager(this@MyRouteActivity)
            addTripRouteText.text="공유하기"
        }
        val dialog = dialogBuild.show()
        dBinding.addTripRouteText.setOnClickListener{
            //체크된 친구와 여행경로 공유
            var checkFriends=myFriendAdapter.mChecked.toList()
            //체크된 친구를 shared에 저장(자신의 DB데이터에)
            myDb.collection("route").document(docId).update("shared",checkFriends).addOnSuccessListener {
                Toast.makeText(this@MyRouteActivity, "공유", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            //체크된 친구의 sharedList에 추가
            checkFriends.forEach {
                Firebase.firestore.collection("user").document(it).collection("sharedList").document(docId).set(
                    hashMapOf("created" to currentId,"docId" to docId,"docName" to docName)
                )
            }
        }
        return dialog
    }

    suspend fun loadFriendData(): MutableList<Friend> {
        var friendList= mutableListOf<Friend>()
        return try {
            val fr_add = myDb.collection("friend").whereEqualTo("state", "2").get().await()
            for (fr in fr_add.documents) {
                var id = fr.data?.get("id").toString() //친구 id
                val fr_data = Firebase.firestore.collection("user").document(id).get().await()
                var load = Friend(fr_data.data?.get("nickname").toString(), id)
                friendList.add(load)
            }
            Log.d("list_test", "try")
            friendList
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
            friendList
        }
    }


}