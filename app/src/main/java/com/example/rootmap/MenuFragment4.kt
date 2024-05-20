package com.example.rootmap

import android.R
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rootmap.databinding.DialogLayooutBinding
import com.example.rootmap.databinding.FragmentMenu4Binding
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MenuFragment4.newInstance] factory method to
 * create an instance of this fragment.
 */
class MenuFragment4 : Fragment() {
    // TODO: Rename and change types of parameters
    private var id: String? = null
    private var param2: String? = null
    private lateinit var auth: FirebaseAuth

    //프래그먼트의 binding
    lateinit var binding: FragmentMenu4Binding
    lateinit var name: String
    lateinit var nickname: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getString("id")
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //여기부터 코드 작성
        binding = FragmentMenu4Binding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        binding.userId.text = id
        viewLifecycleOwner.lifecycleScope.async {
            loadMyData(id.toString())
            binding.userName.text = name
            binding.userNickname.text = nickname
        }
        binding.MyRouteButton.setOnClickListener {
            Toast.makeText(this.context, "루트 이동 구현하기", Toast.LENGTH_SHORT).show()
        }
        binding.logoutButton.setOnClickListener {
            showDialog("logout")
        }
        binding.secessionButton.setOnClickListener {
            showDialog("secession")
        }

        //
        return binding.root
    }

    suspend fun loadMyData(id: String): Boolean {
        return try {
            val mydb = Firebase.firestore.collection("user").document(id).get().await()
            name = mydb.data?.get("name").toString()
            nickname = mydb.data?.get("nickname").toString()
            true
        } catch (e: FirebaseException) {
            Log.d("load_error", "error")
            false
        }
    }

    fun showDialog(mode: String) {
        val dBinding = DialogLayooutBinding.inflate(layoutInflater)
        dBinding.wButton.text = "아니요" //다이어로그의 텍스트 변경
        dBinding.bButton.text = "네"
        if (mode == "logout") {
            dBinding.content.text = "로그아웃하시겠습니까?"
        } else {
            dBinding.content.text = "정말로 탈퇴하시겠습니까?"
            dBinding.content.setTypeface(Typeface.DEFAULT_BOLD)
            dBinding.content.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.holo_red_dark
                )
            )
            dBinding.addContext.text = "되돌릴 수 없습니다."
            dBinding.addContext.visibility = View.VISIBLE
        }
        val dialogBuild = AlertDialog.Builder(context).setView(dBinding.root)
        val dialog = dialogBuild.show() //다이어로그 창 띄우기
        dBinding.bButton.setOnClickListener {//다이어로그 기능 설정
            if (mode == "logout") {
                auth.signOut() // 로그아웃 처리
                val intent = Intent(this.context, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent) // LoginActivity로 화면 전환
            } else {
                secession(id.toString()) //탈퇴 후 로그인 화면으로 이동
                val intent = Intent(this.context, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            dialog.dismiss()
        }
        dBinding.wButton.setOnClickListener {//취소버튼
            //회색 버튼의 기능 구현 ↓
            dialog.dismiss()
        }
    }

    fun secession(id: String) {
        val db = Firebase.firestore.collection("user")
        auth = FirebaseAuth.getInstance()
        //파이어베이스 구조 상 하위의 문서를 전부 삭제해야함
        //친구정보 삭제
        db.document(id).collection("friend").get().addOnSuccessListener {
            for (user in it) {
                db.document(user.id).collection("friend").document(id).delete()
                db.document(id).collection("friend").document(user.id).delete()
            }
        }.addOnFailureListener {
            Log.d("error", "secession error")
        }
        //해당 유저의 문서 삭제
        db.document(id).delete()
        //유저 정보 auth에서 삭제
        var user = auth.currentUser;
        if (user != null) {
            user.delete()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MenuFragment4.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MenuFragment4().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}