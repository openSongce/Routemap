package com.example.rootmap

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Friend
import com.example.rootmap.databinding.FragmentFriendRequestBinding
import com.google.firebase.ktx.Firebase

//받은 요청 프래그먼트-내가 받은 친구요청 확인, 수락 or 거절 화면
// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendRequest.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendRequest: Fragment() {
    // TODO: Rename and change types of parameters
    private var currentId: String? = null
    private var param2: String? = null
    //프래그먼트의 binding
    lateinit var binding: FragmentFriendRequestBinding
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
        //binding 지정
        binding= FragmentFriendRequestBinding.inflate(inflater, container, false)

        var adapter=FriendAdapter("Request",currentId.toString())
        binding.recyclerList.adapter=adapter
        binding.recyclerList.layoutManager= LinearLayoutManager(activity)

        return binding.root
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FriendRequest.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FriendRequest().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}