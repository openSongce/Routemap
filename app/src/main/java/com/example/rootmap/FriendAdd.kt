package com.example.rootmap

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Friend
import com.example.rootmap.databinding.DialogLayoutBinding
import com.example.rootmap.databinding.FragmentFriendAddBinding
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.DocumentSnapshot
import okhttp3.internal.concat

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendAdd.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendAdd : Fragment(), ConfirmDialogInterface {
    // TODO: Rename and change types of parameters
    private var currentId: String? = null
    private var param2: String? = null
    val db = Firebase.firestore
    val data: MutableList<Friend> = mutableListOf()
    val searchData:MutableList<Friend> = mutableListOf()
    lateinit var addAdapter: FriendAdapter
    private var dialog_ver=0
    lateinit var sendCheckDialog: ConfirmDialog

    init {
        instance = this
    }

    //프래그먼트의 binding
    val binding by lazy { FragmentFriendAddBinding.inflate(layoutInflater) }
    lateinit var myDb: CollectionReference
    lateinit var text: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentId = it.getString("id")
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myDb = db.collection("user").document(currentId.toString()).collection("friend")
        binding.searchFriendText.addTextChangedListener {
            //텍스트창의 입력 바뀔때

        }
        binding.sendButton.setOnClickListener {//보내기 버튼 클릭 시
            sendFriendRequest()
        }
        binding.searchFriendText.setOnEditorActionListener { v, actionId, event //키보드 엔터 사용시
            ->
            sendFriendRequest()
            true
        }
        addAdapter = FriendAdapter()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.async {
            refresh()
            addAdapter.list = data
            addAdapter.myid = currentId.toString()
            addAdapter.mode = "Add"
            binding.recyclerList.adapter = addAdapter
            binding.recyclerList.layoutManager = LinearLayoutManager(context)
        }
        super.onViewCreated(view, savedInstanceState)
    }
    suspend fun loadData(): Boolean {
        return try {
            val fr_add = myDb.whereEqualTo("state", "1").get().await()
            for (fr in fr_add.documents) {
                var id = fr.data?.get("id").toString() //친구 id
                val fr_data = db.collection("user").document(id).get().await()
                var load = Friend(fr_data.data?.get("nickname").toString(), id)
                data.add(load)
            }
            Log.d("load_check_add", data.size.toString())
            true
        } catch (e: FirebaseException) {
            false
        }
    }
    fun showCancle(frid: String) {
        dialog_ver=0
        val deletDialog=ConfirmDialog(this,"해당 유저에게 보낸 신청을 취소하겠습니까?",frid)
        deletDialog.isCancelable = false
        deletDialog.show(activity?.supportFragmentManager!!, "ConfirmDialog")
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
    private fun sendFriendRequest(){
        text = binding.searchFriendText.text.toString()
        //데이터베이터에서 해당 ID의 유저 검색
        when (text) {
            "" -> Toast.makeText(context, "빈칸입니다.", Toast.LENGTH_SHORT).show()
            currentId -> Toast.makeText(context, "본인에게 친구신청은 불가능합니다.", Toast.LENGTH_SHORT)
                .show()
            else -> searchUser(text)
        }
    }

    private fun showDialog(
        frid: String
        //friendDb: CollectionReference
    ) { //다이어로그로 팝업창 구현, 아래의 searchUser()에서 사용
        //여기서 binding은 FriendAdd의 Binding, text는 신청보낼 유저 ID임
        dialog_ver=1
        sendCheckDialog=ConfirmDialog(this,"${frid} 에게 친구 신청을 보내겠습니까?",frid)
        sendCheckDialog.isCancelable = false
        sendCheckDialog.show(activity?.supportFragmentManager!!, "ConfirmDialog")

    }
    private fun refresh(){
        //수정 필요
        data.clear()
        viewLifecycleOwner.lifecycleScope.async {
            loadData()
            if (data.isEmpty()) {
                binding.friendAddText.text = "아직 신청을 보낸 기록이 없습니다."
                binding.friendAddText.visibility = View.VISIBLE
            }else{
                binding.friendAddText.visibility = View.INVISIBLE
            }
            addAdapter?.notifyDataSetChanged()
        }
    }

    private fun searchUser(text: String) {
        // 데이터베이스에서 해당 ID 또는 닉네임의 유저 검색
        val userDataById = db.collection("user").whereEqualTo("id", text)
        val userDataByNickname = db.collection("user").whereEqualTo("nickname", text)

        // 두 쿼리 모두 수행
        runBlocking {
            try {
                val resultsById = userDataById.get().await()
                val resultsByNickname = userDataByNickname.get().await()

                val combinedResults = mutableSetOf<DocumentSnapshot>().apply {
                    addAll(resultsById.documents)
                    addAll(resultsByNickname.documents)
                }

                if (combinedResults.isNotEmpty()) {
                    if (resultsByNickname.documents.size>1) {
                        //text가 닉네임일 때
                        showUserSelectionDialog(resultsByNickname.documents)
                    } else {
                        //id일 때
                        showDialog(text)
                    }

                } else {
                    Toast.makeText(context, "해당 유저는 존재하지 않아 신청을 보낼 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: FirebaseException) {
                loadFail()
            }
        }
    }

    private fun showUserSelectionDialog(users: List<DocumentSnapshot>) {
        val userNames = users.map { "${it.getString("nickname")} (${it.getString("id")})" }.toTypedArray()
        val userIds = users.map { it.getString("id") }
        AlertDialog.Builder(context)
            .setTitle("사용자를 선택하세요")
            .setItems(userNames) { _, which ->
                val selectedUserId = userIds[which].toString()
                showDialog(selectedUserId)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun loadFail() {
        Toast.makeText(context, "데이터 로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddFriend.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FriendAdd().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        private var instance: FriendAdd? = null
        fun getInstance(): FriendAdd? {
            return instance
        }


    }

    override fun onYesButtonClick(id: String) {
        var myid = currentId.toString()
        if (dialog_ver == 0) {
            db.collection("user").document(id).collection("friend").document(myid)
                .delete()
            db.collection("user").document(myid).collection("friend").document(id)
                .delete()
            refresh()
        } else { //1인 경우

            context?.hideKeyboard(binding.root) //키보드 내리기 -> 키보드 사용안하는 사람은 사용X
            var friendDb=db.collection("user").document(id)
                .collection("friend")
            var dc_friend = db.collection("user").document(id)
                .collection("friend").whereEqualTo("id", myid)
            dc_friend.get().addOnSuccessListener { document ->
                if (document.isEmpty) {//신청 보내기
                    sendCheckDialog.dismiss() //다이어로그 창 끄기
                    friendDb.document(myid)
                        .set(hashMapOf("id" to myid, "state" to "0"))//상대의 데이터에 추가
                    myDb.document(id).set(
                        hashMapOf("id" to id, "state" to "1")
                    )//내 데이터에 추가
                    refresh()
                    Toast.makeText(this.context, "친구 신청을 보냈습니다.", Toast.LENGTH_SHORT).show()
                } else {//이미 데이터가 있는 경우
                    // val d_id=document.documents[0].id //문서 아이디 저장
                    sendCheckDialog.dismiss()
                    val state =
                        friendDb.document(myid).get()
                            .addOnSuccessListener { dc ->
                                val stateValue = dc.data?.get("state")
                                when (stateValue.toString()) {
                                    "0" -> Toast.makeText(
                                        this.context,
                                        "이미 신청을 보낸 상태입니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    "1" -> Toast.makeText(
                                        this.context,
                                        "이미 요청을 받았습니다. '받은 요청'탭에서 수락하세요.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    "2" -> Toast.makeText(
                                        this.context,
                                        "이미 친구입니다.",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()

                                    else -> Toast.makeText(
                                        this.context,
                                        "알 수 없는 값",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }.addOnFailureListener { exception -> //DB접근에 실패했을때
                                sendCheckDialog.dismiss()
                                loadFail()
                            }
                }
                binding.searchFriendText.text = null
            }.addOnFailureListener { exception -> //DB접근에 실패했을때
                sendCheckDialog.dismiss()
                loadFail()
            }

        }

    }

}