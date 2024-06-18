package com.example.rootmap

import android.app.AlertDialog
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rootmap.databinding.ActivityMyRouteBinding
import com.example.rootmap.databinding.DialogLayoutBinding
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MyRouteActivity : AppCompatActivity() {
    val binding by lazy { ActivityMyRouteBinding.inflate(layoutInflater) }
    lateinit var routelistAdapter: MyDocumentAdapter
    lateinit var routeList: MutableList<MyRouteDocument>
    lateinit var myDb: CollectionReference
    private lateinit var auth: FirebaseAuth
    lateinit var currentId:String
    var searchText=""
    var searchCheck=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        currentId = auth.currentUser?.email.toString()
        myDb = Firebase.firestore.collection("user").document(currentId.toString()).collection("route")
        routeList = mutableListOf<MyRouteDocument>()

        routelistAdapter= MyDocumentAdapter()
        routelistAdapter.mode="MyRoute"

        CoroutineScope(Dispatchers.Main).launch {
            if(loadMyRouteList()==false){
                Toast.makeText(this@MyRouteActivity, "데이터 로드에 실패했습니다.",Toast.LENGTH_SHORT).show()
            }
            routelistAdapter.list=routeList
            val swipeHelperCallback = SwapeManageAdapter(routelistAdapter).apply {
                // 스와이프한 뒤 고정시킬 위치 지정
                setClamp(resources.displayMetrics.widthPixels.toFloat()/4)
            }
            ItemTouchHelper(swipeHelperCallback).attachToRecyclerView(binding.recyclerList)

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
                Toast.makeText(this@MyRouteActivity, "버튼 클릭",Toast.LENGTH_SHORT).show()
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
            val myList = myDb.get().await()
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
            val myList = myDb.whereEqualTo("tripname",text).get().await()
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
            myDb.document(docId).delete()
            routelistAdapter.list.removeAt(position)
            routelistAdapter.notifyItemRemoved(position)
            dialog.dismiss()
        }
        dBinding.wButton.setOnClickListener {//취소버튼
            //회색 버튼의 기능 구현 ↓
            dialog.dismiss()
        }
    }
}