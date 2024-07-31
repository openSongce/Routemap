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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendAdd.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendAdd : Fragment() {
    // TODO: Rename and change types of parameters
    private var currentId: String? = null
    private var param2: String? = null
    val db = Firebase.firestore
    val data: MutableList<Friend> = mutableListOf()
    lateinit var addAdapter: FriendAdapter

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
        val dBinding = DialogLayoutBinding.inflate(layoutInflater)
        dBinding.wButton.text = "취소" //다이어로그의 텍스트 변경
        dBinding.bButton.text = "확인"
        dBinding.content.text = "삭제하시겠습니까?"
        val dialogBuild = AlertDialog.Builder(context).setView(dBinding.root)
        val dialog = dialogBuild.show() //다이어로그 창 띄우기
        var id = currentId.toString()
        dBinding.bButton.setOnClickListener {
            //검정 버튼의 기능 구현 ↓
            db.collection("user").document(id).collection("friend").document(frid)
                .delete()
            db.collection("user").document(frid).collection("friend").document(id)
                .delete()
            refresh()
            dialog.dismiss()
        }
        dBinding.wButton.setOnClickListener {//취소버튼
            //회색 버튼의 기능 구현 ↓
            dialog.dismiss()
        }
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
     fun sendFriendRequest(){
         text = binding.searchFriendText.text.toString()
         //데이터베이터에서 해당 ID의 유저 검색
         when (text) {
             "" -> Toast.makeText(context, "빈칸입니다.", Toast.LENGTH_SHORT).show()
             currentId -> Toast.makeText(context, "본인에게 친구신청은 불가능합니다.", Toast.LENGTH_SHORT)
                 .show()
             else -> searchUser(text)
         }
     }


    fun showDialog(
        text: String,
        friendDb: CollectionReference
    ) { //다이어로그로 팝업창 구현, 아래의 searchUser()에서 사용
        //여기서 binding은 FriendAdd의 Binding, text는 신청보낼 유저 ID임
        val dBinding = DialogLayoutBinding.inflate(layoutInflater)
        dBinding.wButton.text = "취소" //다이어로그의 텍스트 변경
        dBinding.bButton.text = "신청"
        dBinding.content.text = "${text} 유저에게 친구 신청을 보내겠습니까?"
        val dialogBuild = AlertDialog.Builder(context).setView(dBinding.root)
        val dialog = dialogBuild.show() //다이어로그 창 띄우기
        dBinding.bButton.setOnClickListener {//다이어로그 기능 설정
            //검정 버튼의 기능 구현 ↓
            context?.hideKeyboard(binding.root) //키보드 내리기 -> 키보드 사용안하는 사람은 사용X
            var dc_friend = friendDb.whereEqualTo("id", currentId)
            dc_friend.get().addOnSuccessListener { document ->
                if (document.isEmpty) {//신청 보내기
                    dialog.dismiss() //다이어로그 창 끄기
                    friendDb.document(currentId.toString())
                        .set(hashMapOf("id" to currentId, "state" to "0"))//상대의 데이터에 추가
                    myDb.document(text).set(
                        hashMapOf("id" to text, "state" to "1")
                    )//내 데이터에 추가
                    refresh()
                    Toast.makeText(this.context, "신청보냄", Toast.LENGTH_SHORT).show()
                } else {//이미 데이터가 있는 경우
                    // val d_id=document.documents[0].id //문서 아이디 저장
                    dialog.dismiss()
                    val state =
                        friendDb.document(currentId.toString()).get()
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
                                        "???",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }.addOnFailureListener { exception -> //DB접근에 실패했을때
                                dialog.dismiss()
                                loadFail()
                            }
                }
                binding.searchFriendText.text = null
            }.addOnFailureListener { exception -> //DB접근에 실패했을때
                dialog.dismiss()
                loadFail()
            }
        }
        dBinding.wButton.setOnClickListener {//취소버튼
            //회색 버튼의 기능 구현 ↓
            dialog.dismiss()
        }
    }
     fun refresh(){
        data.clear()
        viewLifecycleOwner.lifecycleScope.async {
            loadData()
            addAdapter.list = data
            if (data.isEmpty()) {
                binding.friendAddText.text = "아직 신청을 보낸 기록이 없습니다."
                binding.friendAddText.visibility = View.VISIBLE
            }else{
                binding.friendAddText.visibility = View.INVISIBLE
            }
            addAdapter?.notifyDataSetChanged()
        }
    }

    fun searchUser(text: String) {
        //데이터베이터에서 해당 ID의 유저 검색
        val userData =
            db.collection("user").whereEqualTo("id", text) //필드의 id값이 text인 유저(문서)를 data에 저장
        userData.get().addOnSuccessListener { document -> //DB접근 성공하였을 때
            if (!document.isEmpty) {//해당 유저가 존재할 때
                val friendQuery =
                    db.collection("user").document(document.documents[0].id)
                        .collection("friend")
                showDialog(text, friendQuery)
            } else {
                Toast.makeText(context, "해당 유저는 존재하지 않아 신청을 보낼 수 없습니다.", Toast.LENGTH_SHORT)
                    .show()
            }
        }.addOnFailureListener { exception -> //DB접근에 실패했을때
            loadFail()
        }
    }

    fun loadFail() {
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
}
