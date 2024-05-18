package com.example.rootmap

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Friend
import com.example.rootmap.databinding.FragmentFriendListBinding
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
class FriendList : Fragment() {
    // TODO: Rename and change types of parameters
    private var currentId: String? = null
    private var mode: String? = null

    //프래그먼트의 binding
    val binding by lazy { FragmentFriendListBinding.inflate(layoutInflater) }
    val db = Firebase.firestore
    lateinit var myDb: CollectionReference
    val data: MutableList<Friend> = mutableListOf()
    lateinit var listAdapter:FriendAdapter
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
        myDb = db.collection("user").document(currentId.toString()).collection("friend")
        listAdapter = FriendAdapter()
        return binding.root
        //여기부터 코드 작성
        //Toast.makeText(context,currendId, Toast.LENGTH_SHORT).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        viewLifecycleOwner.lifecycleScope.async {
            loadData()
            listAdapter.list = data
            listAdapter.mode="List"
            listAdapter.myid=currentId.toString()
            Log.d("data", data.size.toString())
            binding.recyclerList.adapter = listAdapter
            binding.recyclerList.layoutManager = LinearLayoutManager(context)
            if (data.isEmpty()) {
                binding.friendListText.text = "친구가 없습니다."
                binding.friendListText.visibility = View.VISIBLE
            }
           // var test=listAdapter.binding
          //  test.friendButton2.text="fd"
        }
        super.onViewCreated(view, savedInstanceState)
    }
    suspend fun loadData(): Boolean {
        return try {
            val fr_add = myDb.whereEqualTo("state", "2").get().await()
            for (fr in fr_add.documents) {
                var id = fr.data?.get("id").toString() //친구 id
                val fr_data = db.collection("user").document(id).get().await()
                var load = Friend(fr_data.data?.get("name").toString(), id)
                data.add(load)
            }
            Log.d("list_test", "try")
            true
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
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