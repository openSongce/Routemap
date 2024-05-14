package com.example.rootmap

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Friend
import com.example.rootmap.databinding.FragmentFriendRequestBinding

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
class FriendRequest : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    //프래그먼트의 binding
    lateinit var binding: FragmentFriendRequestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
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

        //여기부터 코드 작성
        //버튼 수정
        //FriendLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false).

        val data:MutableList<Friend>?=loadData()
        var adapter=FriendAdapter()
        //데이터의 null 체크
        if(data!=null){
            //어댑터에 데이터 반환
            adapter.list= data
        }else{  
            //텍스트 뷰 보이게 설정
            binding.friendRequestText.text="받은 요청 없음"
            binding.friendRequestText.visibility=View.VISIBLE
        }


        binding.recyclerList.adapter=adapter
        binding.recyclerList.layoutManager= LinearLayoutManager(this.activity)
        //
        

        return binding.root
    }

    fun loadData():MutableList<Friend>{
        //파이어베이스로부터 친구 데이터 가져오기-> 상태값이 0인 데이터 선별
        //현재는 테스트 데이터 입력
        val data= mutableListOf<Friend>()
        for(no in 1..10){
            val name="받은요청"
            val id="예시ID"
            var load= Friend(name,id)
            data.add(load)
        }
        return data
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