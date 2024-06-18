package com.example.rootmap

import android.app.AlertDialog
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MyRouteActivity : AppCompatActivity() {
    val binding by lazy { ActivityMyRouteBinding.inflate(layoutInflater) }
    lateinit var routelistAdapter: MyDocumentAdapter
    lateinit var routeList: MutableList<MyRouteDocument>
    lateinit var myDb: DocumentReference
    private lateinit var auth: FirebaseAuth
    lateinit var currentId:String
    lateinit var swipeHelperCallback: SwapeManageAdapter
    var searchText=""
    var searchCheck=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        currentId = auth.currentUser?.email.toString()
        myDb = Firebase.firestore.collection("user").document(currentId.toString())
        routeList = mutableListOf<MyRouteDocument>()

        routelistAdapter= MyDocumentAdapter()
        routelistAdapter.mode="MyRoute"
        swipeHelperCallback = SwapeManageAdapter(routelistAdapter).apply {
            // 스와이프한 뒤 고정시킬 위치 지정
            setClamp(resources.displayMetrics.widthPixels.toFloat()/4)
        }
        ItemTouchHelper(swipeHelperCallback).attachToRecyclerView(binding.recyclerList)

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

        }

        routelistAdapter.setItemClickListener(object: MyDocumentAdapter.OnItemClickListener{
            //리스트의 버튼 클릭시 동작
            override fun onClick(v: View, position: Int) {
                //버튼 눌렀을때의 코드
                val popup = PopupMenu(this@MyRouteActivity,v )
                popup.inflate(R.menu.myroute_list_layout)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_menu1 -> { //보기 클릭
                            Toast.makeText(this@MyRouteActivity, "보기", Toast.LENGTH_SHORT).show()
                        }
                        R.id.action_menu2->{
                            CoroutineScope(Dispatchers.Main).launch{
                                var data=loadData()
                                showDialog(data)
                            }

                        }
                        else -> { //게시글로 만들기 클릭
                            Toast.makeText(this@MyRouteActivity, "게시글", Toast.LENGTH_SHORT).show()
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
                showDeleteDialog(position)
            }

        })
    }

    suspend fun loadMyRouteList():Boolean{
        //내 루트리스트를 가져오는 함수
        routeList.clear()
        return try {
            val myList = myDb.collection("route").get().await()
            if(!myList.isEmpty){
                for (doc in myList.documents) {
                    routeList.add(MyRouteDocument(doc.data?.get("tripname").toString(),doc.id))
                }
            }
            true
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
            false
        }
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
    suspend fun loadSearchRouteList(text:String):MutableList<MyRouteDocument>{
        //검색한 루트리스트를 가져오는 함수
        //텍스트일부만으로 검색이 안되어서 이 함수는 보류
        var list= mutableListOf<MyRouteDocument>()
        return try {
            val myList = myDb.collection("route").whereEqualTo("tripname",text).get().await()
            if(!myList.isEmpty){
                for (doc in myList.documents) {
                    list.add(MyRouteDocument(doc.data?.get("tripname").toString(),doc.id))
                }
            }
            list
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
            list
        }
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

    private fun showDialog(friendList:MutableList<Friend>):AlertDialog{ //다이어로그로 팝업창 구현
        val dBinding = RecyclerviewDialogBinding.inflate(layoutInflater)
        val dialogBuild = AlertDialog.Builder(this).setView(dBinding.root)
        dialogBuild.setTitle("My friends")
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
            addTripRouteText.setOnClickListener{
                //체크된 친구와 여행경로 공유
                Toast.makeText(this@MyRouteActivity, "공유", Toast.LENGTH_SHORT).show()
            }
        }
        val dialog = dialogBuild.show()
        return dialog
    }

    suspend fun loadData(): MutableList<Friend> {
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