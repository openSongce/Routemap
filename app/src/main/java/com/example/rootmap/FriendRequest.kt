package com.example.rootmap

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Friend
import com.example.rootmap.databinding.FragmentFriendRequestBinding
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

//친구 리스트 프래그먼트-현재 자신과 친구 상태인 유저의 리스트를 출력 등의 기능을 가진 화면
// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendList.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendRequest : Fragment() {
    // TODO: Rename and change types of parameters
    private var currentId: String? = null
    private var mode: String? = null

    //프래그먼트의 binding
    val binding by lazy { FragmentFriendRequestBinding.inflate(layoutInflater) }
    val db = Firebase.firestore
    lateinit var myDb: CollectionReference
    val data: MutableList<Friend> = mutableListOf()
    lateinit var reAdapter:FriendAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentId = it.getString("id")
            mode = it.getString("mode")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //binding 지정
        //여기부터 코드 작성
        //Toast.makeText(context,currendId, Toast.LENGTH_SHORT).show()
        myDb = db.collection("user").document(currentId.toString()).collection("friend")
        reAdapter = FriendAdapter()
        return binding.root
    }

    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.async {
            loadData()
            reAdapter.list = data
            reAdapter.myid=currentId.toString()
            reAdapter.mode="Request"

            Log.d("data", data.size.toString())
            binding.recyclerList.adapter = reAdapter
            binding.recyclerList.layoutManager = LinearLayoutManager(context)
            if (data.isEmpty()) {
                binding.friendRequestText.text = "받은 요청이 없습니다."
                binding.friendRequestText.visibility = View.VISIBLE
            }
        }
        //f_binding.friendButton.text="fdsf"
        super.onViewCreated(view, savedInstanceState)
    }
    suspend fun loadData(): Boolean {
        return try {
            val fr_add = myDb.whereEqualTo("state", "0").get().await()
            for (fr in fr_add.documents) {
                var id = fr.data?.get("id").toString() //친구 id
                val fr_data = db.collection("user").document(id).get().await()
                var load = Friend(fr_data.data?.get("name").toString(), id)
                data.add(load)
            }
            Log.d("load_check_re",data.size.toString())
            true
        } catch (e: FirebaseException) {
            false
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FriendList.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FriendList().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}